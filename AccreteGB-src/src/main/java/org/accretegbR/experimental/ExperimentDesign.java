package org.accretegbR.experimental;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.accretegbR.main.AccreteGBRserve;
import org.apache.commons.lang3.StringUtils;
import org.rosuda.REngine.RList;

public abstract class ExperimentDesign {
    
    private ArrayList<List<String>> treatments;
    private Integer reps;
    private Integer blockSize;


	private String methodName;
    private Integer seeds;
    private String[][] rcbdOutput;

    public String convertListToString(List<String> treatments) {
        String str = "";
        List<String> lst = new ArrayList<String>();
        for (String treatment : treatments) {
            lst.add("\"" + treatment + "\"");
        }
        str = StringUtils.join(lst, ",");
       
        return str;
    }
    
    public abstract StringBuffer buildRCode();
    
    public void applyDesign(String rPath, String designName) {
        try {
        	
        	AccreteGBRserve rserve= new AccreteGBRserve();
        	rserve.setRcode(buildRCode().toString());
            RList results = rserve.runAndReturnResult();
            if(results == null){
            	return;
            }
            if(designName.equalsIgnoreCase("Complete Randomized Block") || designName.equalsIgnoreCase("Randomized Complete Block")){
            	 String plots[] = results.at("my.plots").asStrings();
                 String treatments[] = results.at("my.trts").asStrings();
                 String blocks[] = results.at("my.blocks").asStrings();
	            if (plots.length == treatments.length && treatments.length == blocks.length) {
	                String output[][] = new String[3][plots.length];
	                for (int rowCounter = 0; rowCounter < plots.length; rowCounter++) {
	                    output[0][rowCounter] = plots[rowCounter].replace(".0", "");
	                    output[1][rowCounter] = treatments[rowCounter];
	                    output[2][rowCounter] = blocks[rowCounter].replace(".0", "");
	                }
	                setRcbdOutput(output);
	            }
            }else if(designName.equalsIgnoreCase("Alpha Design")){
            	String plots[] = results.at("my.plots").asStrings();
            	String cols[] = results.at("my.cols").asStrings();
 	            String blocks[] = results.at("my.blocks").asStrings();
 	            String treatments[] = results.at("my.trts").asStrings();
 	            String replications[] = results.at("my.reps").asStrings();
 	            if (plots.length == treatments.length && treatments.length == blocks.length) {
 	                String output[][] = new String[5][plots.length];
 	                for (int rowCounter = 0; rowCounter < plots.length; rowCounter++) {
 	                    output[0][rowCounter] = plots[rowCounter].replace(".0", "");
 	                    output[1][rowCounter] = treatments[rowCounter];
 	                    output[2][rowCounter] = cols[rowCounter].replace(".0", "");
 	                    output[3][rowCounter] = blocks[rowCounter].replace(".0", "");
 	                    output[4][rowCounter] = replications[rowCounter];
 	                }
 	                setRcbdOutput(output);
 	            }
            	
            }else if(designName.equalsIgnoreCase("Split Design")){
            	
            	String plots[] = results.at("my.plots").asStrings();
            	String splots[] =results.at("my.splots").asStrings();
 	            String reps[] = results.at("my.reps").asStrings();
 	            String trt1[] = results.at("my.trt1").asStrings();
 	            String trt2[] = results.at("my.trt2").asStrings();
 	            if (plots.length == trt1.length && trt1.length == trt2.length) {
 	                String output[][] = new String[5][plots.length];
 	                for (int rowCounter = 0; rowCounter < plots.length; rowCounter++) {
 	                    output[0][rowCounter] = plots[rowCounter].replace(".0", "");;
 	                    output[1][rowCounter] = splots[rowCounter].replace(".0", "");;
 	                    output[2][rowCounter] = reps[rowCounter].replace(".0", "");;
 	                    output[3][rowCounter] = trt1[rowCounter];
 	                    output[4][rowCounter] = trt2[rowCounter];
 	                }
 	                setRcbdOutput(output);
 	            }
            	
            }
           
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public ArrayList<List<String>> getTreatments() {
        return treatments;
    }

    public void setTreatments(ArrayList<List<String>> treatments) {
        this.treatments = treatments;
    }

    public Integer getReps() {
        return reps;
    }

    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
    public Integer getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(Integer blockSize) {
		this.blockSize = blockSize;
	}

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getSeeds() {
        return seeds;
    }

    public void setSeeds(Integer seeds) {
        this.seeds = seeds;
    }

    public String[][] getRcbdOutput() {
        return rcbdOutput;
    }

    public void setRcbdOutput(String[][] rcbdOutput) {
        this.rcbdOutput = rcbdOutput;
    }

    
} 
