package org.accretegb.modules.germplasm.harvesting;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class PedigreeGenerationAutomation {
	
	private String femalePedigree = null;
	private String malePedigree = null;
	private String matingType = null;
	private boolean madeSelection = false;
	public String childPedigree = null;
	public String childGeneration = null; 
	private String femaleGeneration = null;
	
	public PedigreeGenerationAutomation(String femalePedigree, String malePedigree, String femaleAccession, String maleAccession,
			String matingType, String femaleGeneration, boolean madeSelection){
		this.femalePedigree = femalePedigree.equals("NA") && !String.valueOf(femaleAccession).equalsIgnoreCase("null")
				? femaleAccession : femalePedigree ;
		this.malePedigree = malePedigree.equals("NA") && !String.valueOf(maleAccession).equalsIgnoreCase("null")
				? maleAccession : malePedigree;
		this.matingType = matingType;
		this.madeSelection = madeSelection;
		this.femaleGeneration = femaleGeneration;
		this.childPedigree = gerneratePedigree();
		this.childGeneration = generateGeneration();
		
		//System.out.println(this.femalePedigree+", "+ this.malePedigree+", "+ 
		//this.matingType+", " + this.femaleGeneration + ", "+this.madeSelection+", "+this.childPedigree +","+ this.childGeneration);
		
	}
	
	public String generateGeneration(){
		String generatedGeneration = "";
		if(String.valueOf(this.femaleGeneration).equalsIgnoreCase("null") || this.femaleGeneration.equals("NA")){
			if(this.matingType.equals("BC")){
				return "BC1F0:1";
			}
			return "F0:1";
		}
		String first_num = this.femaleGeneration.split(":")[0];
		first_num = first_num.split("F")[1];
		String second_num = this.femaleGeneration.split(":")[1];
		int f = 0;
		int s = 0;
		try{
			f = Integer.parseInt(first_num);
			s = Integer.parseInt(second_num);
			if (this.madeSelection){
				f = s;
			}
			s = s + 1;
		}catch(Exception e){
			return "ERROR in Female Parent Generation";
		}
		String bcnum = "1";
		if(this.matingType.equals("BC") || this.femaleGeneration.contains("BC")){
			int indexOfRepeat = this.childPedigree.indexOf("*")+1;
			if (indexOfRepeat > 0){
				bcnum = String.valueOf(this.childPedigree.charAt(indexOfRepeat));
			}
			generatedGeneration = "BC"+bcnum+"F"+String.valueOf(f) + ":" + String.valueOf(s);
		}
		else{
			generatedGeneration = "F"+String.valueOf(f) + ":" + String.valueOf(s);
		}
			
		return generatedGeneration;
	}
	
	public String gerneratePedigree(){
		String generatedPedigree = "";
		if(String.valueOf(this.femalePedigree).equals("null") || this.femalePedigree.equals("NA")){
			return this.femalePedigree;
		}
		int lenOfSlashInFemale = findLongestslash(this.femalePedigree);
		int lenOfSlashInMale = findLongestslash(this.malePedigree);
		
		lenOfSlashInFemale = lenOfSlashInFemale >= lenOfSlashInMale ? lenOfSlashInFemale :lenOfSlashInMale;
		String slashes = "";		
		for(int i = 0; i < lenOfSlashInFemale; i++){
			slashes = slashes + "/";
		}
		String lastPart ;
		String[] femaleParts = {this.femalePedigree};
		if(lenOfSlashInFemale >= 1)
		{
			femaleParts = this.femalePedigree.split(slashes);
			lastPart = femaleParts[femaleParts.length-1];
		}else{
			lastPart = this.femalePedigree;
		}
		if(this.matingType.equals("CR") || this.matingType.equals("BC")){
			String revisedLastPart=null;
			// only use * when its "BC"
			if (this.matingType.equals("BC")){
				// already has * -> repeatNum + 1
				if (lastPart.contains("*")){
					String[] repeatPartNum = lastPart.split("\\*");
					String repeatNum = repeatPartNum[repeatPartNum.length-1];
					int num = 0;
					try{
						num = Integer.parseInt(repeatNum);					
					}catch(Exception e){
						System.out.println("ERROR");
					}
					num = num + 1;
					revisedLastPart = lastPart.replace(repeatNum, String.valueOf(num));
				}
				// does not has * -> check if parent is already the second generation. 
	            else if (lastPart.replaceAll("\\s","").equals(this.malePedigree.replaceAll("\\s","")) 
	            		&& lenOfSlashInFemale == 2){
					revisedLastPart = lastPart + "*2";
				}
			}
			if(revisedLastPart != null){
				generatedPedigree = replaceLast(this.femalePedigree, lastPart, revisedLastPart);
			}
			else{
				// situation 2:  F = 2369, M = 2369 / CML10, C = 2369 // 2369 / CML10
				slashes = slashes + "/";
				generatedPedigree = this.femalePedigree + " " + slashes + " " + this.malePedigree;
			}
	
		}
		if(this.matingType.equals("SF") || this.matingType.equals("SB") ||this.matingType.equals("PP") ){
		    String[] generations = lastPart.split("-");
		    String last_generation = generations[generations.length-1];
		    generatedPedigree = "";
		    List addDirectlyList = Arrays.asList("s0","s1","p1","p0","h1","h0");
		    boolean flag = true;
    		if(last_generation.contains("*")){
    			String last_generation_first_part = last_generation.trim().split("\\*")[0];
    			if (addDirectlyList.contains(last_generation_first_part)){
    				flag = false;
    			}
    		}else if(addDirectlyList.contains(last_generation.trim())){
    			flag = false;
    		}
		    
		    if(this.matingType.equals("SF")){
		    	if (!last_generation.trim().contains("s0") && !last_generation.trim().contains("s1") ){
		    		
		    		if (!last_generation.contains(")") && flag){
		    			this.femalePedigree = "(" + this.femalePedigree + ")";
		    		}
		    		if(this.madeSelection)
					{
		    			generatedPedigree = this.femalePedigree + "-s1";
					}else{
						generatedPedigree = this.femalePedigree + "-s0"; 
					}
		    	}else{
		    		if(this.madeSelection)
					{
			    		last_generation = addSelf(last_generation,"s1");
					}else{
						last_generation = addSelf(last_generation,"s0");
					}
		    	}
		    	
		    }
		    if(this.matingType.equals("SB")){
		    	if (!last_generation.trim().contains("h0") && 
		    			!last_generation.trim().contains("h1") ){
		    		if (!last_generation.contains(")") && flag){
		    			this.femalePedigree = "(" + this.femalePedigree + ")";
		    		}
		    		if(this.madeSelection)
					{
		    			generatedPedigree = this.femalePedigree + "-h1";
					}else{
						generatedPedigree = this.femalePedigree + "-h0"; 
					}
		    	}else{
		    		if(this.madeSelection)
					{
			    		last_generation = addSelf(last_generation,"h1");
					}else{
						last_generation = addSelf(last_generation,"h0");
					}
		    	}
		    	
		    }
		    if(this.matingType.equals("PP")){
		    	if (!last_generation.trim().contains("p0") && 
		    			!last_generation.trim().contains("p1") ){
		    		if (!last_generation.contains(")") && flag){
		    			this.femalePedigree = "(" + this.femalePedigree + ")";
		    		}
		    		if(this.madeSelection)
					{
		    			generatedPedigree = this.femalePedigree + "-p1";
					}else{
						generatedPedigree = this.femalePedigree + "-p0"; 
					}
		    	}else{
		    		if(this.madeSelection)
					{
			    		last_generation = addSelf(last_generation,"p1");
					}else{
						last_generation = addSelf(last_generation,"p0");
					}
		    	}
		    	
		    }
		    if (generatedPedigree == "")
		    {
		    	generations[generations.length-1] = last_generation;
		    	lastPart = StringUtils.join(generations, "-"); 	
			    femaleParts[femaleParts.length-1] =lastPart;
				generatedPedigree = StringUtils.join(femaleParts, slashes);
		    }
		   
		}
		return generatedPedigree;
	}
	
	public String addSelf(String last_generation, String repeat){

		if(last_generation.trim().equals(repeat)){
			last_generation = last_generation + "*2";
		}
		else if (last_generation.contains("*") && last_generation.split("\\*")[0].equals(repeat))
		{
			String repeatNum = last_generation.split("\\*")[1];
			int num = 0;
			try{
				num = Integer.parseInt(repeatNum);					
			}catch(Exception e){
				System.out.println("ERROR");
			}
			num = num + 1;
			last_generation = last_generation.replace(repeatNum,String.valueOf(num));
		}else{
			last_generation = last_generation + "-"+repeat;
		}	
	    return last_generation;			
	}
	
	public String replaceLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos > -1) {
	        return string.substring(0, pos)
	             + replacement
	             + string.substring(pos + toReplace.length(), string.length());
	    } else {
	        return string;
	    }
	}
	
	public int findLongestslash(String str){
		int len = 0;
		int max = 0;
		for(int i=0; i<str.length(); ++i){
			if (str.charAt(i) == '/'){
				len++;
				if(len > max){
					max = len;
				}
			}else{
				len = 0;
			}
		}
		return max;
	}
	
/*
	public static void main(String [] args)
	{
		String F = "Oh7B / LH132";
		String M = "Oh7B / LH132 // LH132";
		String MATE = "CR";
		String GEN = "F0:1";
		boolean MADE=false;
		PedigreeGenerationAutomation p = new PedigreeGenerationAutomation(F,M,MATE,GEN,MADE);
		System.out.println(p.childPedigree + " - " + p.childGeneration);
				
	}
*/
}
