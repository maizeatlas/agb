package org.accretegb.modules.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.menu.BackupDBMenuItem.progressThread;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.util.ThreadPool;
import org.apache.commons.lang.SystemUtils;

public class RestoreDBMenuItem extends MenuItem{

	public RestoreDBMenuItem(String label) {
		super(label);
		this.addActionListener(new RestoreDBActionListener());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static class RestoreDBActionListener implements ActionListener {
		private JFrame jf = new JFrame("Restoring database");
		private progressThread pt = new progressThread(jf); 
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Choose Directory of Database Backup Files ");
			fileChooser.setCurrentDirectory(new java.io.File("."));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setAcceptAllFileFilterUsed(false);
            int approve = fileChooser.showSaveDialog(null);
            if (approve != JFileChooser.APPROVE_OPTION) {
                return;
            }   
            File dir = fileChooser.getSelectedFile();
            if (!dir.isDirectory()) {
            	dir = dir.getParentFile();
            }
            String AGBmainPath = dir + "/"+ LoginScreen.MAIN_DATABASE_NAME + ".sql";
            String AGBpmPath = dir + "/" + LoginScreen.PROJECT_MANAGER_DB_NAME + ".sql";
            File mainfile = new File(AGBmainPath);
            File pmfile = new File(AGBpmPath);
            if(!mainfile.exists() && !pmfile.exists()) { 
                JOptionPane.showMessageDialog(null, "Please ensure " + LoginScreen.MAIN_DATABASE_NAME + ".sql and " + LoginScreen.PROJECT_MANAGER_DB_NAME + ".sql exist");
                return;
            }  
            pt.start();
            String server = Utils.getAuthorizationStrs().get("server");
            String port = Utils.getAuthorizationStrs().get("port");
            String username = Utils.getAuthorizationStrs().get("username");
            String password = Utils.getAuthorizationStrs().get("password");          
            String AGBcmd = " --host=" + server + " --port=" + port 
            		+" --user=\"" + username + "\" --password='" + password 
            		+"'  " + LoginScreen.MAIN_DATABASE_NAME + " < " + AGBmainPath;
            
            String PMcmd = " --host=" + server + " --port=" + port 
            		+" --user=\"" + username + "\" --password='" + password 
            		+"'  " + LoginScreen.PROJECT_MANAGER_DB_NAME + " < " + AGBpmPath;
            
           
           //String mysql = this.getClass().getClassLoader().getResource("mysqlcmds/mysql").getPath();
            String mysqldir = System.getProperty("user.dir");
            String mysql = mysqldir + "/mysql";
        	try {
        		mysql = URLDecoder.decode(mysql, "UTF-8").replace(" ", "\\ ").replace("!", "");
			} catch (UnsupportedEncodingException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
        	AGBcmd = mysql + AGBcmd;
    	    PMcmd = mysql + PMcmd;
    	    final String[] cmds = {AGBcmd,PMcmd};  	   
            Runnable restoreThread = new Runnable(){
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
		                    int exitvalue = -2;;
		    				Process pr = pb.start(); 
		                    BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
		                    String line;
		                    while ((line = err.readLine()) != null) {
		                        System.out.println(line);
		                        errormsg = errormsg + line;
		                        if(LoggerUtils.isLogEnabled())
		            				LoggerUtils.log(Level.INFO,line);
		                    }
		                    BufferedReader output = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		                    while ((line = output.readLine()) != null) {
		                        System.out.println(line);
		                        sdoutput = sdoutput + line;
		                        if(LoggerUtils.isLogEnabled())
		            				LoggerUtils.log(Level.INFO,line);
		                    }
		                    exitvalue = pr.waitFor();  
		                    if(exitvalue == 0){
		                    	successCount++;
		                    } 
		                    
		                    errormsg += "<br>";
		                    sdoutput += "<br>";
		                    
		        	    } 
		        	    jf.dispose();
		                pt.interrupt();
		                if(successCount == 2){
		                   JOptionPane.showMessageDialog(null, "<html><br>Databases backup are restored to database server.<br>"
		                   		+ "Please restart AGB for the applied changes to take effect. </html>");
		                   System.exit(0);
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
			ThreadPool.getAGBThreadPool().executeTask(restoreThread);
            
		}
	}
	
}
