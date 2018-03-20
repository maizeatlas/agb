package org.accretegb.modules.menu;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.util.ThreadPool;
import org.apache.commons.lang.SystemUtils;

/**
 *@author Ningjing
 */
public class BackupDBMenuItem extends MenuItem{

	public BackupDBMenuItem(String label) {
		super(label);
		this.addActionListener(new BackupDBActionListener());
	}
	public static class progressThread extends Thread{
	    JFrame frame;
	    public progressThread(JFrame frame){
	    	this.frame = frame;
	    }
	    public void run() {
			JProgressBar progressBar = new JProgressBar();
			progressBar.setVisible(true);
	        progressBar.setIndeterminate(true);
   		    frame.add(progressBar);
   		    frame.setPreferredSize(new Dimension(400,50));
   		    frame.pack();
   	        frame.setVisible(true);
   	        frame.setLocationRelativeTo(null);
		}			
	}

	private static final long serialVersionUID = 1L;
	public static class BackupDBActionListener implements ActionListener {

		private JFrame jf = new JFrame("Backing up database");
		private progressThread pt = new progressThread(jf);       
		public void actionPerformed(ActionEvent e) {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Choose Directory For Database Backup Files ");
			fileChooser.setCurrentDirectory(new java.io.File("."));
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			fileChooser.setAcceptAllFileFilterUsed(false);
            int approve = fileChooser.showSaveDialog(null);
            if (approve != JFileChooser.APPROVE_OPTION) {
                return;
            }
    	    pt.start();
            String AGBmainPath = fileChooser.getSelectedFile().getAbsolutePath() + "/"+ LoginScreen.MAIN_DATABASE_NAME + ".sql";
            String AGBpmPath = fileChooser.getSelectedFile().getAbsolutePath() + "/" + LoginScreen.PROJECT_MANAGER_DB_NAME + ".sql";
            
            String server = Utils.getAuthorizationStrs().get("server");
            String port = Utils.getAuthorizationStrs().get("port");
            String username = Utils.getAuthorizationStrs().get("username");
            String password = Utils.getAuthorizationStrs().get("password");          
            String AGBcmd = " --host=" + server + " --port=" + port 
            		+" --user=\"" + username + "\" --password=\"" + password 
            		+"\"  " + LoginScreen.MAIN_DATABASE_NAME + " > " + AGBmainPath;
            
            String PMcmd = " --host=" + server + " --port=" + port 
            		+" --user=\"" + username + "\" --password=\"" + password 
            		+"\"  " + LoginScreen.PROJECT_MANAGER_DB_NAME + " --ignore-table=" + LoginScreen.PROJECT_MANAGER_DB_NAME + ".user  > " + AGBpmPath;
            
            
            //String mysqldump = this.getClass().getClassLoader().getResource("mysqlcmds/mysqldump").getPath();
            String dir = System.getProperty("user.dir");
            String mysqldump = dir + "/mysqldump";
        	try {
				mysqldump = URLDecoder.decode(mysqldump, "UTF-8").replace(" ", "\\ ").replace("!", "");
			} catch (UnsupportedEncodingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
    		AGBcmd = mysqldump + AGBcmd;
    	    PMcmd = mysqldump + PMcmd;
    	    final String[] cmds = {AGBcmd,PMcmd};        
            Runnable backupThread = new Runnable(){
				public void run() {
					int successCount = 0;
					String errormsg = "";
					String sdoutput = "";
					try {
		        	    for( String cmd : cmds ){
		        	    	//System.out.println(cmd);
		                    ProcessBuilder pb = null;
		                    if (SystemUtils.IS_OS_WINDOWS) {
		                    	  // TODO test windows
		                    	  pb = new ProcessBuilder("cmd", "/c", cmd);
		                    }else{
		                    	  pb = new ProcessBuilder("bash", "-c", cmd);
		                    }
		                    pb.redirectErrorStream(true);                     
		                    int exitvalue = -2;;
		    				Process pr = pb.start(); 					
		                    BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		                    String line;
		                    while ((line = err.readLine()) != null) {
		                        System.out.println(line);
		                        errormsg = errormsg + line;
		                    }
		                    BufferedReader output = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		                    while ((line = output.readLine()) != null) {
		                        System.out.println(line);
		                        sdoutput = sdoutput + line;		                   
		                    }
		                    exitvalue = pr.waitFor();  
		                    if(exitvalue == 0){
		                    	successCount++;
		                    }else{
		                    	System.out.println(exitvalue);
		                    }	
		                    errormsg += "<br>";
		                    sdoutput += "<br>";
		                    
		        	    } 
		        	    jf.dispose();
		                pt.interrupt();
		                if(successCount == 2){
		                   JOptionPane.showMessageDialog(null, "Databases backup files are saved to" + fileChooser.getSelectedFile());
		                }else{
		                   JOptionPane.showMessageDialog(null, "<html><br>Something went wrong:<br>" + errormsg + sdoutput+"</html>");
		                    
		                }     		
		            	               
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			};
			ThreadPool.getAGBThreadPool().executeTask(backupThread);
                
    	}
	}
	
	
}
