package evolutionJEAFParallelRemote;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.lang.ProcessBuilder.Redirect;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import unalcol.random.integer.IntUniform;
import mpi.MPI;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import es.udc.gii.common.eaf.problem.objective.ObjectiveFunction;
import evolutionJEAFParallel.EvolJEAFMaze;

public class CFMSRand3 extends ObjectiveFunction {

	public double evaluate(double[] values) {

		// Create objects to save information into a txt file
		FileWriter fichero = null;
		PrintWriter pw = null;

		// Retrieve process number from mpj
		int myRank = MPI.COMM_WORLD.Rank();
		myRank = myRank + EvolJEAFMazeR.startNumber;
		// Start Measuring evaluation time
		long startTime = System.currentTimeMillis();

		// CPG Parameters
		float ampli = (float) values[0];
		float offset = (float) values[1];
		float phase = (float) (values[2]) * (float) Math.PI;

		// Pack Floats into one String data signal
		FloatWA ControlParam = new FloatWA(3);
		float[] CP = new float[3];
		CP[0] = ampli;
		CP[1] = offset;
		CP[2] = phase;
		ControlParam.setArray(CP);
		CharWA strCP = new CharWA(1);
		strCP.setArray(ControlParam.getCharArrayFromArray());

		// Morphology Parameters
		int Numberofmodules = 8;
		int[] orientation = new int[] { 1, 0, 1, 0, 1, 0, 1, 0 };

		// Simulation Parameters
		int MaxTime = 40;

		// Pack Integers into one String data signal
		IntWA NumberandOri = new IntWA(Numberofmodules + 3);
		int[] NO = new int[Numberofmodules + 3];
		NO[0] = Numberofmodules;
		NO[1] = MaxTime;
		NO[2] = myRank;
		for (int i = 3; i < Numberofmodules + 3; i++) {
			NO[i] = orientation[i - 3];
		}
		NumberandOri.setArray(NO);
		CharWA strNO = new CharWA(1);
		strNO.setArray(NumberandOri.getCharArrayFromArray());

		// 3 Sub-environment maze parameters
//		char[][] subenv = new char[][] { { 's', 'l', 's' }, { 'b' },
//				{ 's', 'r', 's' } };

		char[][] subenvperm = new char[][] {
			{ 's', 'l', 's', 'b', 's', 'r', 's' },
			{ 's', 'l', 's', 's', 'r', 's', 'b' },
			{ 'b', 's', 'l', 's', 's', 'r', 's' },
			{ 'b', 's', 'r', 's', 's', 'l', 's' },
			{ 's', 'r', 's', 's', 'l', 's', 'b' },
			{ 's', 'r', 's', 'b', 's', 'l', 's' } };
			
		// Generate random 3 sub-environment sequence (by Brute Force)
//		IntUniform r = new IntUniform(3);
//		int[] seq = r.generate(3);
//		while (seq[1] == seq[0]) {
//			seq[1] = r.next();
//		}
//		seq[2] = 5 - ((seq[0] + 1) + (seq[1] + 1));

		// Maze Parameters (Already a string)
		char[] mazeseq = new char[] { 's' }; // Default Maze Sequence
		// char[] mazeseq = new char[]{'s','l','s','s','l','s','l','s','l','l'};
		CharWA strSeq = new CharWA(1);
		strSeq.setArray(mazeseq);

		// Array that receives fitness from the simulator or signals a crash
		float[] rfitness = new float[3];

		// Solved environment flag
		float[] pass = new float[] { 0, 0, 0 };

		// Fitness to be returned to Evolutionary Algorithm
		float[] fitness = new float[] { 1, 1, 1 };

		// Number of retries in case of simulator crash
		int maxTries = 5;

		// Retry if there is a simulator crash
		for (int j = 0; j < maxTries; j++) {

			// Simulator interaction start
			remoteApi vrep = new remoteApi(); // Create simulator control object
			vrep.simxFinish(-1); // just in case, close all opened connections
			// Connect with the corresponding simulator remote server
			int clientID = vrep.simxStart("127.0.0.1", 19997 - myRank, true,
					true, 5000, 5);

			if (clientID != -1) {
				// Set Simulator signal values
				vrep.simxSetStringSignal(clientID, "NumberandOri", strNO,
						vrep.simx_opmode_oneshot_wait);
				vrep.simxSetStringSignal(clientID, "ControlParam", strCP,
						vrep.simx_opmode_oneshot_wait);
				vrep.simxSetStringSignal(clientID, "Maze", strSeq,
						vrep.simx_opmode_oneshot_wait);

				try {
					// *******************************************************************************************************************************

					// New Maze Parameters (Already a string)
					mazeseq = subenvperm[currentEnv];
					strSeq.setArray(mazeseq);
					vrep.simxSetStringSignal(clientID, "Maze", strSeq,
							vrep.simx_opmode_oneshot_wait);

					// Run Scene in the simulator
					rfitness = RunSimulation(vrep, clientID, MaxTime, myRank);

					if (rfitness[0] == -1) {
						RestartSim(myRank, j);
						continue;
					}

					// Retrieve the fitness if there is no crash
					fitness[0] = rfitness[1];
					pass[0] = rfitness[2];
					
					// *******************************************************************************************************************************
				} catch (InterruptedException e) {
					System.err.println("InterruptedException: "
							+ e.getMessage());
				}
				// Close connection with the simulator
				vrep.simxFinish(clientID);

			} else {
				// No connection could be established
				System.out.println("Failed connecting to remote API server");
				System.out.println("Trying again for the " + j + " time");
				continue;
			}

			break;
		}

		// Calculate global fitness and convert it from float to Double
		// double fitnessd = (fitness[0]+fitness[1]+fitness[2]+fitness[3])/4;

		double fitnessd;

//		if (pass[0] == 1) {
//			if (pass[1] == 1) {
//				if (pass[2] == 1) {
//					fitnessd = ((fitness[0] + fitness[1] + fitness[2]) / 3) - 4f;
//				} else {
//					fitnessd = fitness[2] - 4f;
//				}
//			} else {
//				fitnessd = fitness[1] - 2f;
//			}
//		} else {
//			fitnessd = fitness[0];
//		}
		
//		fitnessd = (fitness[0] + fitness[1] + fitness[2]) / 3;
		fitnessd = fitness[0];
		

		try {
			fichero = new FileWriter("Testout/Indiv" + myRank + ".txt", true);
			pw = new PrintWriter(fichero);
			// Discovering Generation number based on the output file

			// int numgen = generation("Testout/TestS2430.txt")+1;

			int numgen = 0;

			// if (myRank<10){
			// numgen = generation("Testout/TestS2430.txt")+1;
			// }else if (myRank<20){
			// numgen = generation("Testout/TestS24310.txt")+1;
			// }else {
			// numgen = generation("Testout/TestS24320.txt")+1;
			// }

			if (myRank < 8) {
				numgen = generation("Testout/TestSRand30.txt") + 1;
			} else {
				numgen = generation("Testout/TestSRand38.txt") + 1;
			}

			pw.println(numgen + "," + seq[0] + "," + seq[1] + "," + seq[2]
					+ "," + pass[0] + "," + pass[1] + "," + pass[2] + ","
					+ fitness[0] + "," + fitness[1] + "," + fitness[2] + ","
					+ fitnessd);
			// pw.println(fitnessd + "-" + reportDate);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fichero)
					fichero.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		// Calculate evaluation time and print it
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println(elapsedTime);

		// System.out.println("Total Fitness = "+fitnessd+" myrank "+myRank);

		return fitnessd;

	}

	public void reset() {

	}

	void RestartSim(int myRank, int j) {

		// Create the command to open the corresponding simulator
		String vrepcommand = new String("./vrep" + myRank + ".sh");

		System.out.println("Restarting simulator and trying again for the " + j
				+ " time in " + myRank);

		try {
			// Command to kill corresponding simulator
			ProcessBuilder qq = new ProcessBuilder("killall", "vrep" + myRank);
			// Specify output file for command line messages
			File log = new File("Simout/log");
			qq.redirectErrorStream(true);
			qq.redirectOutput(Redirect.appendTo(log));
			// Start command process
			Process p = qq.start();
			// Wait for process to finish
			int exitVal = p.waitFor();
			System.out.println("Terminated vrep" + myRank + " with error code "
					+ exitVal);

			// Command to open a simulator with no window
			// qq = new ProcessBuilder(vrepcommand,"-h");
			qq = new ProcessBuilder(vrepcommand, "-h",
					"/home/rodr/EvolWork/Modular/Maze/MazeBuilderR01.ttt");
			// qq = new
			// ProcessBuilder("xvfb-run","--auto-servernum","--server-num=1",vrepcommand,
			// "-h");
			// Open the simulator from its own directory
			qq.directory(new File("/home/rodr/V-REP/Vrep" + myRank + "/"));
			// Specify output file for command line messages of the simulator
			qq.redirectErrorStream(true);
			qq.redirectOutput(Redirect.appendTo(log));
			// Issue command
			qq.start();
			// Wait for the simulator to open
			Thread.sleep(5000);
			System.out.println("Trying again now");
		} catch (Exception e) {
			// Could not reopen the simulator
			System.out.println(e.toString());
			e.printStackTrace();
		}

	}

	public static float maximum(float[] inputset) {

		float maxValue = 0;
		for (int i = 0; i < inputset.length; i++) {
			if (inputset[i] >= maxValue) {
				maxValue = inputset[i];
			}
		}

		return maxValue;
	}

	float[] RunSimulation(remoteApi vrep, int clientID, int MaxTime,
			int threadnum) throws InterruptedException {

		long startTime = System.currentTimeMillis();
		long stopTime = 0;
		long elapsedTime = 0;
		// Create all output variables
		IntW out = new IntW(0);
		CharWA datastring = new CharWA(1);
		FloatWA out2 = new FloatWA(3);
		float[] fitnessout = new float[3];

		// Start Simulation
		int ret = vrep.simxStartSimulation(clientID,
				vrep.simx_opmode_oneshot_wait);
		System.out.println("Start: " + ret + " sim: " + threadnum);
		// Setting up and waiting for finished flag
		vrep.simxGetIntegerSignal(clientID, "finished", out,
				vrep.simx_opmode_streaming);
		out.setValue(0);

		while (out.getValue() == 0) {

			Thread.sleep(10);

			if (vrep.simxGetIntegerSignal(clientID, "finished", out,
					vrep.simx_opmode_buffer) == vrep.simx_return_ok) {
				// We received a fitness signal and everything is ok
				// System.out.println("Retrieved Signal: "+Integer.toString(out.getValue()));
			} else {
				// We have not received the fitness message and out may contain
				// garbage so we set it up to 0 again
				out.setValue(0);
				stopTime = System.currentTimeMillis();
				elapsedTime = stopTime - startTime;

				if (elapsedTime > 300000) {
					System.out
							.println("Too much time has passed attempting to restart simulator");
					fitnessout[0] = -1; // This signals that the output is not
										// ok and the simulator should be
										// restarted
					fitnessout[1] = 1000;
					return fitnessout;
				}
			}
		}

		// Stop listening to the finished signal
		vrep.simxGetIntegerSignal(clientID, "finished", out,
				vrep.simx_opmode_discontinue);

		// Stop simulation
		vrep.simxStopSimulation(clientID, vrep.simx_opmode_oneshot_wait);

		// Read simulation results
		ret = vrep.simxGetStringSignal(clientID, "Position", datastring,
				vrep.simx_opmode_oneshot_wait);
		if (ret != vrep.simx_return_ok) {
			System.out.println("Position Signal not received");
		}

		out2.initArrayFromCharArray(datastring.getArray());

		if (out2.getArray().length == 0) {
			// If out2 is empty the simulator is not working properly and should
			// be restarted
			System.out.println("out2 is empty");
			fitnessout[0] = -1; // This signals that the output is not ok and
								// the simulator should be restarted
			fitnessout[1] = 1000;
			return fitnessout;
		}

		fitnessout[0] = 0;

		float alpha = 0.7f;
		float beta = 1 - alpha;

		if (out2.getArray()[0] == 0) {
			// The robot could get out of the maze so the fitness is the time it
			// spent normalized
			fitnessout[1] = beta * out2.getArray()[1] / MaxTime;
			fitnessout[2] = 1;
		} else if (out2.getArray()[1] == 0) {
			// The robot could not get out of the maze so the fitness is the
			// distance to goal + the maximum time allowed
			fitnessout[1] = alpha * out2.getArray()[0] + beta * 1.0f;
			fitnessout[2] = 0;
		}

		// Return the fitness of the run
		return fitnessout;

	}

	public int generation(String string) {
		RandomAccessFile fileHandler = null;
		try {
			fileHandler = new RandomAccessFile(string, "r");
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();

			for (long filePointer = fileLength; filePointer != -1; filePointer--) {
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();

				if (readByte == 0xA) {
					if (filePointer == fileLength) {
						continue;
					}
					break;

				} else if (readByte == 0xD) {
					if (filePointer == fileLength - 1) {
						continue;
					}
					break;
				}

				sb.append((char) readByte);

			}

			if (fileLength <= 0) {
				return -1;
			} else {
				String lastLine = sb.reverse().toString();
				String number = lastLine.substring(0, lastLine.indexOf(" "));
				// System.out.println(lastLine.indexOf(" "));
				return Integer.parseInt(number);
			}

		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (java.io.IOException e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (fileHandler != null)
				try {
					fileHandler.close();
				} catch (IOException e) {
					/* ignore */
				}
		}
	}

}
