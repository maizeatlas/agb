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
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import org.accretegb.modules.customswingcomponent.Utils;
import org.accretegb.modules.main.LoginScreen;
import org.accretegb.modules.main.SendEmail;
import org.accretegb.modules.main.StringEncrypter;
import org.accretegb.modules.util.LoggerUtils;
import org.accretegb.modules.util.ThreadPool;
import org.apache.commons.lang.SystemUtils;

import net.miginfocom.swing.MigLayout;

/**
 *@author Ningjing
 */
public class SetupEmailMenuItem extends MenuItem{


	public SetupEmailMenuItem(String label) {
		super(label);
		this.addActionListener(new SetupEmailActionListener());
	}
	private static final long serialVersionUID = 1L;
	public static class SetupEmailActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			
			final JPanel panel = new JPanel(new MigLayout("insets 5 5 5 5, gapx 0"));
			panel.add(new JLabel("Email address: "));
			final JTextField email = new JTextField(20);
			panel.add(email,"wrap");
			panel.add(new JLabel("Password: "));
			final JPasswordField password = new JPasswordField(20);
			panel.add(password,"wrap");
			panel.setToolTipText("Use the credentials for an Gmail account. This account will be used to sent emails to token holder to release token.");
			Runnable connectThread = new Runnable(){
				public void run() {
					int opt = JOptionPane.OK_OPTION;
					while(opt==JOptionPane.OK_OPTION){
						int option = JOptionPane.showConfirmDialog(null, panel, "Setup and email account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
						if(option == JOptionPane.OK_OPTION){
							String user = email.getText().trim();
							String pwd = password.getText().trim();
							
							SendEmail connection = new SendEmail();
							if(!connection.connect(user, pwd)){
								opt = JOptionPane.showOptionDialog(null, 
								        "Please use a valid Gmail account.", 
								        "",
								        JOptionPane.OK_CANCEL_OPTION, 
								        JOptionPane.INFORMATION_MESSAGE, 
								        null, 
								        new String[]{"Reset","Cancel"},
								        "default");
								
							}else{
								opt = JOptionPane.CANCEL_OPTION;
								//SAVE TO FILE
								connection.writeCredentialsFile(user, pwd);
							}
						}else{
							opt = JOptionPane.CANCEL_OPTION;
						}
					}           	
				}
			};
			ThreadPool.getAGBThreadPool().executeTask(connectThread);	   
    	}
	}
	
	
	
	
}
