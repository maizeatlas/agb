package org.accretegbR.main;
import javax.swing.JOptionPane;

import org.apache.commons.lang.SystemUtils;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

public class AccreteGBRserve {
	protected String rcode;	
	protected String RscriptExecutable;
	protected static StartRserve startRserve;
	
	public AccreteGBRserve(){
		startRserve = new StartRserve();
	}
	
	public RList runAndReturnResult()
	{ 	
		if(startRserve.checkLocalRserve()){
			try{
				 RConnection c = new RConnection();	
				 System.out.println(getRcode());
				 RList results = c.eval(getRcode()).asList();
			     c.close();
			     return results;
			 }catch(Exception e){
			    System.out.println("Unable to get result:" + e.getMessage());
			 }	
		}
		 return null; 	 	    
	}
	
	public String getRcode() {
		return rcode;
	}

	public void setRcode(String rcode) {
		this.rcode = rcode;
	}
	
	
	
}
		

