package Tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntW;
import coppelia.IntWA;
import coppelia.remoteApi;
import evolHAEA.EmP;
import evolHAEA.HEmP;
import maze.Maze;
import maze.SelectableMaze;
import simvrep.ShortChallengeSettings;
import simvrep.Simulation;
import simvrep.SimulationSettings;
import unalcol.optimization.OptimizationFunction;
import util.ChromoConversion;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

public class GeneralizationTest {

	// public float alpha = 0.7f;// Look into the Run simulation method to set
	// alpha
	static int numberofindividuals = 2;
	static int individuallength = 234;
	static double maxrandomval = 10;
	static double minrandomval = -10;

	public static void main(String[] args) {
		
		String path = "/home/rodr/Desktop/Results/Morpho1/Incremental/lrbCombined/10Percent";
		String fileHeader = "IncrIsolFlrb0";
		String fileName = "GenResultFitHAEA.txt";
		String fileNameCsv = "TableHAEAEnvOrder.csv";
		double[] result = new double[6];
	
		Simulation sim = new Simulation(0);
		// Retry if there is a simulator crash
		for (int i = 0; i < 5; i++) {
			if (sim.Connect()) {
				break;
			} else {
				// No connection could be established
				System.out.println("Failed connecting to remote API server");
				System.out.println("Trying again for the " + i + " time in " + 0);
			}
		}
		
		char[][] structures = new char[][]{
			{'s','l','b','r'},
			{'s','l','r','b'},
			{'s','r','b','l'},
			{'s','r','l','b'},
			{'s','b','l','r'},
			{'s','b','r','l'}
		};	
		
	SimulationSettings settings = new SimulationSettings(5,"defaultmhs.ttt",180,false,false);
	SelectableMaze maze = new SelectableMaze(structures, 0, 0.4f, 0.088f);
	String morpho = "[(0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,1.0 , 3.0, 1.0, 3.0, 1.0, 3.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]";
	double[] morphology = ChromoConversion.str2double(morpho);		

		 //double [][] indiv = ReadTXTFiles(path,fileHeader,individuallength,10); 
		double [][] indiv=ReadJsonFiles(path,fileHeader,individuallength,10,"Evol");
		 //double [][] indiv = GenerateRandomIndividuals(numberofindividuals,ierndividuallength, maxrandomval,minrandomval);
			for (int i = 0; i < indiv.length; i++) {
		 
		 			result = RunTest(indiv[i],morphology, sim,settings,maze);
		 			WResultsFile(indiv[i], result, path+fileName);
		 			for (int j = 0;j<6;j++){
		 				System.out.println(result[j]);
		 			}
		 			System.out.println("+++++++++++++++++++++++++++++++++++++");
		 
		 		}
			
			sim.Disconnect();
			
			Data process = new Data(fileName,fileNameCsv,numberofindividuals,path,",");
			
			process.GenerateCSV("HAEA");

	}

	private static void WResultsFile(double[] indiv, double[] result,String filename) {
		FileWriter file = null;
		PrintWriter pw = null;
		try {
			file = new FileWriter(filename, true);
			pw = new PrintWriter(file);
			
			for (int i=0;i<indiv.length;i++){
				if (i==indiv.length-1){
					pw.print(indiv[i]+";  ");
				}else{
					pw.print(indiv[i]+",");
				}
				
			}
			
			pw.println(result[0]+","+result[1]+","+result[2]+","+result[3]+","+result[4]+","+result[5]);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != file)
					file.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}
	
	private static double[][] ReadJsonFiles(String Folderpath, String fileheader, int indivlength, int numberOfReplicas,String test) {

		double[][] individuals = new double[numberOfReplicas][indivlength];
		JSONArray ja = new JSONArray();
		String number;
		
			for (int l = 0;l<numberOfReplicas;l++){
				try {
					 Object obj = new JSONParser().parse(new FileReader(Folderpath+"/"+fileheader+l+".json"));
					 JSONObject jo = (JSONObject) obj;
					 
					 switch (test) {
						
						case "Evol":
							JSONObject best = (JSONObject)jo.get("solution");
							//double fitness = (double) bestO.get("best_fitness");
							//System.out.println(fitness); 
							ja = (JSONArray) best.get("best_individual");
							break;
						case "ShortSep":
							JSONObject lastChallenge = (JSONObject)jo.get("challenge29");
							number = lastChallenge.get("fitnessEvol").toString();
							if(!number.equals("-1")) {
								ja = (JSONArray) lastChallenge.get("lastBestEvol");
							}else {
								ja = (JSONArray) lastChallenge.get("lastBest");
							}
							break;
						case "ShortComb":
							lastChallenge = (JSONObject)jo.get("challenge33");
							number = lastChallenge.get("fitnessEvol").toString();
							if(!number.equals("-1")) {
								ja = (JSONArray) lastChallenge.get("lastBestEvol");
							}else {
								ja = (JSONArray) lastChallenge.get("lastBest");
							}
							break;
						case "ShortBump":
							lastChallenge = (JSONObject)jo.get("challenge9");
							number = lastChallenge.get("fitnessEvol").toString();
							if(!number.equals("-1")) {
								ja = (JSONArray) lastChallenge.get("lastBestEvol");
							}else {
								ja = (JSONArray) lastChallenge.get("lastBest");
							}
							break;
						case "ShortDeactivated":
							lastChallenge = (JSONObject)jo.get("challenge211");
							number = lastChallenge.get("fitnessEvol").toString();
							if(!number.equals("-1")) {
								ja = (JSONArray) lastChallenge.get("lastBestEvol");
							}else {
								ja = (JSONArray) lastChallenge.get("lastBest");
							}							
							break;
						default:
							System.out.println("Type of json test not recognized");
							break;					 
					 }
					 
					 Iterator itr = ja.iterator();
					 
					 int j=0;
					 while (itr.hasNext()) 
				        {
						 		individuals[l][j] = (double)itr.next();
						 		//System.out.println(individuals[i][j]);
						 		j++;
				        }
					 
					// System.out.println(individuals[i].length);
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}	
		
		//System.out.println(individuals.length);
		return individuals;
		
	}

private static double[][] ReadTXTFiles(String Folderpath, String fileheader, int indivlength, int numberOfReplicas) {
		
		List<String> list = new ArrayList<String>();
		
		for (int l = 0;l<numberOfReplicas;l++){
			
			try {
				BufferedReader in = new BufferedReader(new FileReader(Folderpath+"/"+fileheader+l+".txt"));
				String str;

				while ((str = in.readLine()) != null) {
					list.add(str);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println(list.get(i));
		}
		
		
		
		double[][] individuals = new double[list.size()][indivlength];
		
		for (int i = 0;i<list.size();i++){
			//System.out.println(list.get(i));
			String[] sep = list.get(i).split(",");
			for (int j=0;j<indivlength;j++){
				individuals[i][j] = Double.parseDouble(sep[j]); //How to ensure that the last (blank) element is not used?
				//System.out.print(individuals[i][j]+",");
			}
			//System.out.println(individuals[i].length);
		}

		return individuals;
	}
	
	

	static double[] RunTest(double[] indv, double[] morphology, Simulation sim, SimulationSettings settings, SelectableMaze maze) {
			
		double[] fitnessD = new double[6];		
		EmP function;
		
		for (int i = 0;i<6;i++){
			maze.selectMaze(i);
			function = new EmP(sim,morphology,maze,settings);
			fitnessD[i] = function.apply(indv);
		}
		
		return fitnessD;
	}
	
	static double[][] GenerateRandomIndividuals(int number,int lenght,double maxvalue, double minvalue){
		double[][] individuals  = new double[number][lenght];
		
		for (int i=0;i<number;i++){
			individuals[i] = GenerateRandomIndiv(lenght,maxvalue,minvalue);
		}
		
		return individuals;
	}
	
	static double[] GenerateRandomIndiv(int lenght, double maxvalue, double minvalue){
		double[] individual = new double[lenght];
		
		// NewValue = (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax -
		// OldMin)) + NewMin
		
		for (int i=0;i<lenght;i++){
			individual[i] =(Math.random()*(maxvalue - minvalue))+minvalue;
		}		
		
		return individual;	
	}
	
	

	

	

}
