package show;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONObject;

import evolHAEA.EmP;
import maze.Maze;
import simvrep.ShortChallengeSettings;
import simvrep.Simulation;
import util.ChromoConversion;

public class ShowIncremental {
	
	public static List<Simulation> simulators;
	

	public static void main(String[] args) {
		double fitness;
		String morpho = "[(0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,1.0 , 3.0, 1.0, 3.0, 1.0, 3.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]";
		double[] morphology = ChromoConversion.str2double(morpho);
		
		
		float[] times = new float[] { 2.5f,5.23f,7.5f,8.9f,12.22f,15.1f,16.6f,20.9f,21.5f,23,25,30};
		float[] envFractions = new float[] {0.05f,0.17f,0.25f,0.29f,0.45f,0.5f,0.55f,0.67f,0.71f,0.75f,0.78f,1};
		
		ShortChallengeSettings settings = new ShortChallengeSettings(times, envFractions, 0, 5, "defaultmhs.ttt", false,false);
		Maze maze = new Maze(new char[] { 'b' }, 0.4f, 0.088f, 4);
		
		double[] lastBest = new double[]{7.708928028555393,-9.56345410473616,-8.255917456896656,3.04233752423839,9.083529290068405,-4.1161272340037005,-0.6091142304364943,0.5935274697542182,-6.277635327890714,7.578927095870533,5.885462031792938,-7.7753437468086215,8.174853039808111,-5.436497283267827,-5.619729867488823,-7.392037072192826,1.130879715361647,8.120355677526595,7.985484584836511,-8.181701358290045,2.4081974718640144,8.893228207306493,4.2106032471321955,-0.6930706615882528,-3.7748817932674825,9.867123557873962,-5.786636147822303,-0.7471723929016364,3.546631504241584,-9.185796964205155,9.778558016922915,8.002586796496056,9.149026011027651,3.4730313437970644,1.376582253453936,6.9728531485812635,-5.089521750968057,-6.752485230328557,-3.3836632811219305,-7.7634209765548245,1.8168012031134837,3.1637530526147466,7.819805255537969,-6.692669421463636,3.5154727599145454,-3.826743743747427,8.320422779602866,2.887791441372534,-8.644685250067468,4.975873483979284,-0.5974525663724919,7.579658147790049,9.421471735006847,5.840397702521542,-9.838747402561093,3.9633000881350493,0.8850044377750622,6.08472167561977,-4.247075181742098,-3.7619144233841184,-4.92162706858894,2.0107328060303757,7.529658583716668,-5.919773991578652,0.39209652067510786,-9.427178107947823,1.9556878442528984,3.570527499940555,-4.387535041681742,-9.16854011856713,-0.38998958391683497,4.52684592208532,-0.0996588556086872,-0.23654243632258667,-8.19891132993842,5.718371375249869,3.2093303418423575,-0.23263762126888485,5.989131141205957,-9.251344129147611,-0.1140058499284408,2.3140865941495377,-1.3589386966785872,5.216689978463424,-7.42689158748457,-2.6779854039109474,9.123175433316915,-1.2242045743915162,-6.304722344882048,-9.671602158815436,4.71375720933824,-7.380753911987987,-7.231252943618327,-2.7122053777883277,6.467058701483071,0.6902192261238721,4.050919344070905,-7.983719398169905,-1.13356300090755,3.257304733276699,8.775336704011977,-6.363879243585144,6.560844517645102,4.256309040332459,5.069414293730283,-3.3243410341439286,5.69791726982442,6.312042955457657,-7.840930618143567,6.5221736936226415,6.983448031126352,7.467721577442877,-7.107830476438293,3.950565682867234,-2.3858284862386294,8.15788081768467,-7.871596477846214,-2.7942655123119335,1.074533920680065,-0.603773662657331,9.158679525372765,-7.060929388985656,-5.287479640602105,9.759108537758975,7.499898706368577,3.359637661409684,2.965732343350368,4.676940244686986,-7.89922177046355,9.83264771961957,3.760528565294272,0.9187857046335056,-3.6496790146904976,1.4349867687765536,6.774923840447233,-2.9915579120615736,4.684528296441332,-2.8976520446934533,-2.615619738133502,9.636461268213077,5.453994731496955,-0.7989850773212297,-1.4845536326849689,0.3914351614549332,-5.712017049266469,-2.038330766217917,-8.70124780038742,5.020097099262085,-3.8132638771489633,-2.538123417573166,7.832214611450922,6.5461276235615715,-8.214938006503846,5.276670074894344,-9.724476823235108,7.590287120064936,4.937112019494383,2.6780011069896115,6.276749275119028,-9.226229725193098,-0.8145337742790377,-4.076099808665764,-5.036010071805488,3.146204655341807,0.997167874691605,-5.950505248360252,-7.807110352502159,3.684503936530417,-9.462736895919608,0.29561713893102337,8.78215394696343,-7.5984019314072855,-6.339284925334199,-7.288461556839404,-2.2238643174972403,-6.65951103844405,-6.619610419723367,6.953256790255967,2.085724414763869,0.7345372610204807,-2.862273179024239,-5.305421646384657,5.220796726450172,6.341821883869715,-7.106212965111937,5.466847288311901,6.219565091803131,-8.257419996734772,-1.4459643114526295,3.7148916820439926,2.2384580258389057,7.092027325918661,-6.687901241336123,1.8848509206984418,-8.78819465346293,-5.18006214688433,6.354424318402283,5.092004158035652,-7.397374425637232,0.6525851836893923,-8.13246771388468,-9.386855110417502,-2.1394847268223183,3.8077628849033105,-3.708830832954124,3.4758804228714038,-2.516362398247224,6.285809811812246,7.387105190119538,1.794497234080102,-7.533383878222982,6.197686689210677,-4.279284229898172,6.772579472824994,1.467926379222195,-8.429821798396016,5.088320389455198,-0.21839397881873776,-9.343153674023176,9.851565108819651,1.2239890442326542,-1.5461715578133644,3.808689376460347,9.99698716349952,-0.6298336246628072,-3.5157886569192964,5.270731341641761,-3.0475388006529087,-3.03388200229114,-2.9565389025899265,-5.084663978190617,4.01063720450574,-3.252887301875985,-2.358988471520711};
		
		for (int i = 8; i < 10; i++) {
			
			settings.selectChallenge(i);

			JSONObject challengeStep = new JSONObject();

			if (lastBest != null) {
				simulators = new ArrayList<Simulation>();
				connectToSimulator(0);
				EmP function = new EmP(simulators ,1, morphology, maze, settings);
				fitness = function.apply(lastBest);
				simulators.get(0).Disconnect();
				challengeStep.put("lastBest", lastBest);
				challengeStep.put("fitness", fitness);
			} else {
				fitness = 1.0;
				challengeStep.put("lastBest", -1);
				challengeStep.put("fitness", fitness);
			}

			System.out.println("Fitness = " + fitness + ", Challenge: " + settings.getSelection());

		}


	}
	
	static void connectToSimulator(int simulatorNumber) {
		Simulation sim = new Simulation(simulatorNumber);
		// Retry if there is a simulator crash
		for (int i = 0; i < 5; i++) {
			if (sim.Connect()) {
				simulators.add(sim);
			} else {
				// No connection could be established
				System.out.println("Failed connecting to remote API server");
				System.out.println("Trying again for the " + i + " time in " + simulatorNumber);
				continue;
			}
			break;
		}
	}


}