package simvrep;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import represent.Robot;

public class Simulation {
	
	boolean DEBUG = false;
	private int attempt = 0;

	/**
	 * Simulator server number to connect to
	 */
	public int simnumber;

	/**
	 * Maximum simulation time allowed
	 */
	public float MaxTime = 10;

	/**
	 * Client Id assigned by the simulator server
	 */
	public int clientID;

	public int getClientID() {
		return clientID;
	}

	/**
	 * Object that implements all v-rep's remote api
	 */
	public remoteApi vrep;

	/**
	 * Signals to set in the simulator
	 */
	public CharWA strCP;
	public CharWA strNO;
	public CharWA strSeq;

	/**
	 * Constructor of the SimulationTest Class
	 * 
	 * @param simnumber
	 * @param MaxTime
	 * @param robot
	 */

	public Simulation(int simnumber) {
		if (DEBUG){
			System.out.println("Building Simulation with simnumber: "+simnumber+" MaxTime: "+MaxTime);
		}
		this.simnumber = simnumber;	

	}
	
	/**
	 * Prepares signals to be sent to the simulator from parameters stored in the robot 
	 * representation
	 * @param robot
	 */
	

	/**
	 * Connects to the simulator server specified by simnumber
	 * 
	 * @return whether the simulator returns a valid clientID
	 */
	public boolean Connect() {
		
		vrep = new remoteApi(); // Create simulator control object
		//vrep.simxFinish(-1); // just in case, close all opened connections
		// Connect with the corresponding simulator remote server
		clientID = vrep.simxStart("127.0.0.1", 19997 - simnumber, true, true, 5000, 5);
		
		if (DEBUG){
			System.out.println("Using Connect() in "+simnumber+" clientID: "+clientID);
		}
		
		
		return clientID != -1;
	}

	/**
	 * Closes the connection with the simulator
	 */
	public void Disconnect() {
		if (DEBUG){
			System.out.println("Using Disconnect() in "+simnumber);
		}
		// Before closing the connection to V-REP, make sure that the last
		// command sent out had time to arrive. You can guarantee this with (for
		// example):
		IntW pingTime = new IntW(0);
		vrep.simxGetPingTime(clientID, pingTime);
		
		// Close connection with the simulator
		vrep.simxFinish(clientID);
	}

	/**
	 * Sets the maze sequence signal in the simulator
	 * 
	 * @param sequence
	 *            a char array containing the maze sequence
	 * @param width
	 * 			the width of the maze
	 * @param dToGoal
	 * 			if true the robot distance to the goal will be measured, if false distance will be measured from the starting point to the robot in Manhattan distance 
	 * @param dToGoalByPart
	 * 			if true the distance to the goal will be measured for each part of the maze and aggregated in Manhattan distance, if false the distance will be measured in pure Manhattan distance 
	 */
	public void SendMaze(char[] sequence, float width, boolean dToGoal, boolean dToGoalByPart){
		
		int dToGoalint = dToGoal ? 1:0;
		int dToGoalByPartint = dToGoalByPart ? 1:0;
		strSeq = new CharWA(sequence.length);
		System.arraycopy(sequence,0,strSeq.getArray(),0,sequence.length);

		int result = vrep.simxSetStringSignal(clientID, "Maze", strSeq, vrep.simx_opmode_oneshot);
		
		result = vrep.simxSetFloatSignal(clientID, "MazeW", width, vrep.simx_opmode_oneshot);
			
		result = vrep.simxSetIntegerSignal(clientID, "DToGoal", dToGoalint , vrep.simx_opmode_oneshot);
		
		result = vrep.simxSetIntegerSignal(clientID, "DToGoalBP", dToGoalByPartint , vrep.simx_opmode_oneshot);
		
		if (DEBUG){
			System.out.println("Using SendMaze() in "+simnumber+" result: "+result);
		}
				
		
	}
	
	public void SendMaze(char[] sequence, float width, boolean dToGoal, boolean dToGoalByPart, float height){
		
		SendMaze(sequence,width,dToGoal,dToGoalByPart);
		int result = vrep.simxSetFloatSignal(clientID, "MazeBH", height, vrep.simx_opmode_oneshot);
		if (DEBUG){
			System.out.println("Using SendMaze() in "+simnumber+" result: "+result);
		}
		
	}
	
	public void SendMaze(char[] sequence, float width, boolean dToGoal, boolean dToGoalByPart, float height, int nBSteps){
		SendMaze(sequence,width,dToGoal,dToGoalByPart,height);
		int result = vrep.simxSetIntegerSignal(clientID, "MazeNS", nBSteps, vrep.simx_opmode_oneshot);
		if (DEBUG){
			System.out.println("Using SendMaze() in "+simnumber+" result: "+result);
		}
	}
	
	public void SendMaze(char[] sequence, float width, boolean dToGoal, boolean dToGoalByPart, float height, int nBSteps, float environmentFraction){
		SendMaze(sequence,width,dToGoal,dToGoalByPart,height,nBSteps);
		int result = vrep.simxSetFloatSignal(clientID, "MazeSC", environmentFraction, vrep.simx_opmode_oneshot);
		if (DEBUG){
			System.out.println("Using SendMaze() in "+simnumber+" result: "+result);
		}
	}
	
	public void SendMaxTime(float maxTime){
		this.MaxTime = maxTime; 
		int result = vrep.simxSetFloatSignal(clientID, "MaxTime", MaxTime, vrep.simx_opmode_oneshot);
		if (DEBUG){
			System.out.println("Using SendMaxTime() in "+simnumber+" result: "+result);
		}
	}

	public void SendSignals() {
		
		// Set Simulator signal values
		int result1 = vrep.simxSetStringSignal(clientID, "NumberandOri", strNO, vrep.simx_opmode_oneshot);
		int result2 = vrep.simxSetStringSignal(clientID, "ControlParam", strCP, vrep.simx_opmode_oneshot);
		
		//Send the server number
		int result3 = vrep.simxSetIntegerSignal(clientID,"Server",simnumber,vrep.simx_opmode_oneshot);
		
		if (DEBUG){
			System.out.println("Using SendSignals() in "+simnumber+" signal 1: "+result1+" signal 2: "+result2+" signal 3: "+result3);
		}
		
	}

	/**
	 * Runs a simulation in a previously opened scene in V-Rep
	 * 
	 * @param alpha
	 *            the weight of the distance in the fitness calculation
	 * @return an array with whether the simulation was successful, the fitness
	 *         and whether the robot could get out of the maze
	 * @throws InterruptedException
	 */
	public float[] RunSimulation() throws InterruptedException {
		if (DEBUG){
			System.out.println("Using RunSimulation() in "+simnumber);
		}


		long startTime = System.currentTimeMillis();
		long stopTime = 0;
		long elapsedTime = 0;
		// Create all output variables
		IntW out = new IntW(0);
		CharWA datastring = new CharWA(1);
		FloatWA out2 = new FloatWA(3);
		float[] fitnessout = new float[4];

		// Start Simulation
		int ret = vrep.simxStartSimulation(clientID, vrep.simx_opmode_blocking);
		System.out.println("Start: " + ret + " sim: " + simnumber);
		if (ret == 1){
			System.out.println("Failed to start simulation");
			fitnessout[0] = -1; //The simulator should be restarted
			fitnessout[1] = 1000;
			return fitnessout;
		}
		// Setting up and waiting for finished flag
		int ret1 = vrep.simxGetIntegerSignal(clientID, "finished", out, vrep.simx_opmode_streaming);
		if (DEBUG){
			System.out.println("Setting up finished flag in "+simnumber+" result: "+ret1);
		}
		out.setValue(0);

		while (out.getValue() == 0) {

			Thread.sleep(100);
			
			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;

			if (elapsedTime > 600000) {
				System.out.println("Too much time has passed attempting to restart simulator in "+simnumber);
				fitnessout[0] = -1; // This signals that the output is not
				// ok and the simulator should be
				// restarted
				fitnessout[1] = 1000;
				return fitnessout;
			}

			if (vrep.simxGetIntegerSignal(clientID, "finished", out, vrep.simx_opmode_buffer) == vrep.simx_return_ok) {
				// We received a fitness signal and everything is ok
				// System.out.println("Retrieved Signal:
				// "+Integer.toString(out.getValue()));
			} else {
				// We have not received the fitness message and out may contain
				// garbage so we set it up to 0 again
				out.setValue(0);	
			}
		}

		// Stop listening to the finished signal
		int ret2 = vrep.simxGetIntegerSignal(clientID, "finished", out, vrep.simx_opmode_discontinue);
		if (DEBUG){
			System.out.println("Stopped listening to finished flag in "+simnumber+" result: "+ret2);
		}

		// Stop simulation
		int ret3 = vrep.simxStopSimulation(clientID, vrep.simx_opmode_blocking);
		if (DEBUG){
			System.out.println("Stopped simulation in "+simnumber+" result: "+ret3);
		}

		// Read simulation results
		ret = vrep.simxGetStringSignal(clientID, "Position", datastring, vrep.simx_opmode_blocking);
		if (ret == vrep.simx_return_novalue_flag) {
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
		//for (int i=0;i<out2.getLength();i++){
			//System.out.println("Out2["+i+"] = "+out2.getArray()[i]);
		//}
		float[] fitness = new float[2];
		fitness = getResults(out2);
		fitnessout[1] = fitness[0];
		fitnessout[2] = fitness[1];
		fitnessout[3] = fitness[2];
		
		// Before closing the connection to V-REP, make sure that the last
		// command sent out had time to arrive. You can guarantee this with (for
		// example):
		IntW pingTime = new IntW(0);
		vrep.simxGetPingTime(clientID, pingTime);
		//if(DEBUG){
			System.out.println("Ping to "+simnumber+": "+pingTime.getValue());
		//}

		// Return the fitness of the run
		return fitnessout;

	}

	public int getSimnumber() {
		return simnumber;
	}

	public void setSimnumber(int simnumber) {
		this.simnumber = simnumber;
	}
	
	public remoteApi getVrepApi(){
		return vrep;
	}

	public void setMaxTime(float maxTime) {
		MaxTime = maxTime;
	}

	/**
	 * Restarts the simulator by killing the process and re-launching it
	 * 
	 * @param j
	 *            the attempt number
	 */
	public void RestartSim(String scene) {
		if (DEBUG){
			System.out.println("Using RestartSim() in "+simnumber);
		}

		//int simnumber = this.simnumber;

		// Create the command to open the corresponding simulator
		String vrepcommand = new String("./vrep" + simnumber + ".sh");

		System.out.println("Restarting simulator and trying again for the " + attempt + " time in " + simnumber);
		attempt++;
		try {
			// Command to kill corresponding simulator
			ProcessBuilder qq = new ProcessBuilder("killall", "vrep" + simnumber);
			// Specify output file for command line messages
			File log = new File("Simout/log");
			qq.redirectErrorStream(true);
			qq.redirectOutput(Redirect.appendTo(log));
			// Start command process
			Process p = qq.start();
			// Wait for process to finish
			int exitVal = p.waitFor();
			System.out.println("Terminated vrep" + simnumber + " with error code " + exitVal);

			// Command to open a simulator with no window
			// qq = new ProcessBuilder(vrepcommand,"-h");
			//qq = new ProcessBuilder(vrepcommand,"-h", "scenes/Maze/"+scene);
			qq = new ProcessBuilder("xvfb-run","-a",vrepcommand, "-h",  "scenes/Maze/"+scene); 
			// qq = new
			// ProcessBuilder("xvfb-run","--auto-servernum","--server-num=1",vrepcommand,
			// "-h");
			// Open the simulator from its own directory
			qq.directory(new File("/home/rodrigo/V-REP/Vrep" + simnumber + "/"));
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

	/**
	 * Returns the simulation results in a float array
	 * 
	 * @param output
	 *            the output coming from the simulator
	 * 
	 * @return an array with the distance, time and whether the robot could
	 *         get out of the maze
	 */
	private float[] getResults(FloatWA output) {
		if (DEBUG){
			System.out.println("Using Calcfitness() in "+simnumber);
		}

		float[] results = new float[3];

		if (output.getArray()[2] == 1) {
			// The robot could get out of the maze so the simulator returns the time spent normalized
			results[0] = 1; //The robot gets out of the maze
			results[1] = 0; //Distance is the distance of the maze (to be handled in EvaluatorMT)
			results[2] = output.getArray()[1]/MaxTime; //Time normalized
		} else if (output.getArray()[2] == 0) {
			// The robot could not get out of the maze so the simulator returns the distance traveled
			results[0] = 0; //The robot doesn't get out of the maze
			results[1] = output.getArray()[0]; // The distance normalized
			results[2] = 1; //Time: The max time 
		}

		return results;

	}

}
