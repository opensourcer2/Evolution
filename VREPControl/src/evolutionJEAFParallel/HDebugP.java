package evolutionJEAFParallel;

import es.udc.gii.common.eaf.problem.objective.ObjectiveFunction;
import mpi.MPI;
import represent.Robot;
import simvrep.Simulation;

/**
 * For use with MDebug.ttt and MRun.ttt
 * 
 * @author rodr
 *
 */

public class HDebugP extends ObjectiveFunction {

	public void reset() {

	}

	public double evaluate(double[] values) {

		// Use simulator instance 0 or the default remoteAPI server
		// configuration
		int simulator;

		// Retrieve process number from mpj for the simulator number
		simulator = MPI.COMM_WORLD.Rank();
		simulator = simulator + EvJEAFP.startNumber;

		// Morphology Parameters
		int Numberofmodules = 8;
		int[] orientation = new int[] { 1, 0, 1, 0, 1, 0, 1, 0 };

		// Simulation Parameters
		int MaxTime = 30;

		// Control Parameters
		float[] CP = new float[42];
		
		//CPG set parameters for each hormone
		for (int i = 0; i < 21; i++) {
			CP[i] = (float) values[i];
		}
		//CPG step parameters for each hormone
		for (int i = 21; i < 42;i++){
			CP[i] = (float) values[i]*0.01f;
		}

		// Array that receives fitness from the simulator or signals a crash
		float[] rfitness = new float[3];

		char[][] subenvperm = new char[][] {
			{ 's', 'l', 's', 'b', 's', 'r', 's' },
			{ 's', 'l', 's', 's', 'r', 's', 'b' },
			{ 'b', 's', 'l', 's', 's', 'r', 's' },
			{ 'b', 's', 'r', 's', 's', 'l', 's' },
			{ 's', 'r', 's', 's', 'l', 's', 'b' },
			{ 's', 'r', 's', 'b', 's', 'l', 's' },
			{'s','s'}};

		Robot robot = new Robot(Numberofmodules, orientation, CP);

		Simulation sim = new Simulation(simulator, MaxTime);

		// Number of retries in case of simulator crash
		int maxTries = 5;

		// Retry if there is a simulator crash
		for (int j = 0; j < maxTries; j++) {

			if (sim.Connect()) {
				sim.prepareSignals(robot);
				sim.SendSignals();
				sim.SendMaze(subenvperm[6], 0.8f, true , true);

				try {
					rfitness = sim.RunSimulation();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				sim.Disconnect();

				if (rfitness[0] == -1) {
					sim.RestartSim(j, "MRun.ttt");
					continue;
				}

			} else {
				// No connection could be established
				System.out.println("Failed connecting to remote API server");
				System.out.println("Trying again for the " + j + " time in " + simulator);
				continue;
			}
			break;
		}

		return rfitness[2];

	}

}
