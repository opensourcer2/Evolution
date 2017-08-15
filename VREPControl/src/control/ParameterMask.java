package control;

import simvrep.SimulationConfiguration;

public class ParameterMask {

	protected float[] parameters;
	protected float[] extraparam;
	protected float[] maskedparameters;
	protected float[] maskextraparam;
	protected String controltype;
	protected int numberofParameters;
	protected int extrap = -1;

	public ParameterMask(int extrap) {
		this.extrap = extrap;
		controltype = SimulationConfiguration.getController();
		this.numberofParameters = SimulationConfiguration.getControllerparamnumber();
	}

	public void growParam(int numberofModules) {
		if (parameters == null) {
			System.err.println("ParameterMask");
			System.err.println("Parameters have not been set yet");
			System.exit(-1);
		}
		//float[] grownparam = new float[numberofParameters * numberofModules];
		float[] grownparam = parameters;
		maskedparameters = adjustParam(grownparam);

	}
	
	public void setandsepParam(float[] fullparam){
		float[] extraparam = new float[extrap];
		float[] parameters = new float[fullparam.length-extrap];
		
		for(int i=0;i<extrap;i++){
			extraparam[i]=fullparam[i];
			//System.out.println(i+" "+extrap[i]);
		}
		//System.out.println("extraparam: " + extrap.length);
		for(int i=extrap;i<fullparam.length;i++){
			parameters[i-extrap]= fullparam[i]; 
			//System.out.println(i-extraparam+" "+param[i-extraparam]);
		}
		//System.out.println("param: " + param.length);
		
		//System.exit(0);
		this.extraparam = extraparam;
		this.parameters = parameters;
	}

	public void growextraParam() {
		if (extraparam == null) {
			System.err.println("ParameterMask");
			System.err.println("Extraparam have not been set yet");
			System.exit(-1);
		}
		//float[] grownextra = new float[extrap];
		float[] grownextra = extraparam;
		maskextraparam =adjustextraParam(grownextra);
	}

	public float[] getExtraparam() {
		return extraparam;
	}

	public int getExtrap() {
		return extrap;
	}

	public void setExtraparam(float[] extraparam) {
		this.extraparam = extraparam;
		this.extrap = extraparam.length;
	}

	public float[] getMaskedparameters() {
		if (maskedparameters == null) {
			System.err.println("ParameterMask");
			System.err.println("Parameters have not been grown yet when attemping to return maskedparameters");
			System.exit(-1);
		}
		return maskedparameters;
	}

	public float[] getMaskextraparam() {
		if (maskextraparam == null) {
			System.err.println("ParameterMask");
			System.err.println("Parameters have not been grown yet when attemping to return maskextraparam");
			System.exit(-1);
		}
		return maskextraparam;
	}

	public void setParameters(float[] parameters) {
		this.parameters = parameters;
	}
	
	protected float[] adjustextraParam(float[] extraparam) {
		float[] grownextra = new float[extraparam.length];
		for (int i = 0; i < extraparam.length; i++) {
			grownextra[i] = ((extraparam[i] + 1) / 2);
		}
		return grownextra;
	}

	protected float[] adjustParam(float[] parameters) {
		float[] grownparam = new float[parameters.length];
		if (controltype.contentEquals("CPG")) {
			float maxPhase = (float) SimulationConfiguration.getMaxPhase();
			float minPhase = (float) SimulationConfiguration.getMinPhase();
			float maxAmplitude = (float) SimulationConfiguration.getMaxAmplitude();
			float minAmplitude = (float) SimulationConfiguration.getMinAmplitude();
			float maxOffset = (float) SimulationConfiguration.getMaxOffset();
			float minOffset = (float) SimulationConfiguration.getMinOffset();
			// Assuming min raw parameter is -1 and max is 1
						// NewValue = (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax -
						// OldMin)) + NewMin
						// NewValue = (((OldValue - (-1)) * (NewMax - NewMin)) / (1 - (-1)))
						// + NewMin
			for (int i = 0; i < parameters.length; i = i + numberofParameters) {	
					grownparam[i] = (((parameters[i] + 1) * (maxAmplitude - minAmplitude)) / 2) + minAmplitude;
					grownparam[i+1] = (((parameters[i+1] + 1) * (maxOffset - minOffset)) / 2) + minOffset;
				for (int j = 0; j < 4; j++) {
					grownparam[i + j + 2] = (((parameters[i + j + 2] + 1) * (maxPhase - minPhase)) / 2) + minPhase;
				}
			}
			
		} else if(controltype.contentEquals("CPGH")||controltype.contentEquals("CPGHF")||controltype.contentEquals("CPGHLog")||controltype.contentEquals("CPGHFLog")) {
			float maxPhase = (float) SimulationConfiguration.getMaxPhase();
			float minPhase = (float) SimulationConfiguration.getMinPhase();
			float maxAmplitude = (float) SimulationConfiguration.getMaxAmplitude();
			float minAmplitude = (float) SimulationConfiguration.getMinAmplitude();
			float maxOffset = (float) SimulationConfiguration.getMaxOffset();
			float minOffset = (float) SimulationConfiguration.getMinOffset();
			float maxFreq = (float) SimulationConfiguration.getMaxAngularFreq();
			float minFreq = (float) SimulationConfiguration.getMinAngularFreq();

			// Assuming min raw parameter is -1 and max is 1
			// NewValue = (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax -
			// OldMin)) + NewMin
			// NewValue = (((OldValue - (-1)) * (NewMax - NewMin)) / (1 - (-1)))
			// + NewMin

			for (int i = 0; i < parameters.length; i = i + numberofParameters) {
				for (int j = 0; j < 5; j++) {
					grownparam[i + j] = (((parameters[i + j] + 1) * (maxAmplitude - minAmplitude)) / 2) + minAmplitude;
					grownparam[i + j + 5] = (((parameters[i + j + 5] + 1) * (maxOffset - minOffset)) / 2) + minOffset;
					grownparam[i + j + 30] = (((parameters[i + j + 30] + 1) * (maxFreq - minFreq)) / 2) + minFreq;
				}
				for (int j = 0; j < 20; j++) {
					grownparam[i + j + 10] = (((parameters[i + j + 10] + 1) * (maxPhase - minPhase)) / 2) + minPhase;
				}
			}
		}
		return grownparam;
	}

}
