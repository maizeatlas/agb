package org.accretegb.modules.main;

import java.io.File;
import java.io.IOException;
import java.util.*;  
import javax.mail.*;  
import javax.mail.internet.*;
import javax.swing.JOptionPane;

import org.accretegb.modules.util.ThreadPool;

import javax.activation.*;  
  
public class SendEmail  
{  
	private String reply_to;
	private String send_to;
	private String token_holder;
	private String collaborator;
	private ArrayList<String> projects;
	private String username = "accretegb@gmail.com";
	private String password = "agb17@udel";
	private static String EMAIL_FILE = "email.txt";
	private static String KEY_FILE = "key.txt";
	private static String CREDENTIALS_REGEX = "UDEL19716ACCRETEGB";
	public SendEmail(String reply_to, String send_to, String token_holder,
			String collaborator, ArrayList<String> projects){
		this.reply_to = reply_to;
		this.send_to = send_to;
		this.token_holder = token_holder;
		this.collaborator = collaborator;
		this.projects = projects;
	}
	
	public SendEmail() {
		// TODO Auto-generated constructor stub
	}

    public boolean connect(String user, String pwd){
    	try {
    	    Properties props = new Properties();
    	    // required for gmail 
    	    props.put("mail.smtp.starttls.enable","true");
    	    props.put("mail.smtp.auth", "true");
    	    // or use getDefaultInstance instance if desired...
    	    Session session = Session.getInstance(props, null);
    	    Transport transport = session.getTransport("smtp");
    	    transport.connect("smtp.gmail.com", 587, user, pwd);
    	    transport.close();
    	    JOptionPane.showMessageDialog(null, "Email was set up successfuly.");
    	    return true;
    	 } 
    	 catch(AuthenticationFailedException e) {
    	       System.out.println("AuthenticationFailedException - for authentication failures");
    	       return false;
    	 }
    	 catch(MessagingException e) {
    	       System.out.println("for other failures");
    	       return false;
    	 }
    }
    
    public void readEmailInfoFromFile(){
		
		File userFile = new File(EMAIL_FILE);
    	File keyFile = new File(KEY_FILE);
		if(userFile.exists() && userFile.length() != 0 && keyFile.exists() && keyFile.length() != 0){	
			try {
				StringEncrypter encrypter = new StringEncrypter();
				encrypter.readAndSetKeyFromFile(keyFile);
				
				String[] credentials = encrypter.decryptFromFile(EMAIL_FILE, CREDENTIALS_REGEX);
				if(credentials.length == 2){
					this.username = credentials[0];
					this.password = credentials[1];
				}
				else{
					System.out.println("credential file name isn't correct");
				}
				
			} catch (IOException e) {
				e.printStackTrace();	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
    
    public void writeCredentialsFile(String username, String password){
   	 
		try {
			StringEncrypter encrypter = new StringEncrypter();
			File keyFile = new File(KEY_FILE);
			if(!keyFile.exists()){
				encrypter.writeKeyToFile(keyFile);
			}
	    	encrypter.readAndSetKeyFromFile(keyFile);
	    	
	    	String[] credentials = { username,password};
	    	encrypter.encryptToFile(EMAIL_FILE, credentials, CREDENTIALS_REGEX);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    }
    
  	
	public void send(){
		Runnable sendThread = new Runnable(){
			public void run() {
				if(username==null && password==null){
					readEmailInfoFromFile();
					System.out.println(username + ", " + password);
				}
				if(username==null && password==null){
					JOptionPane.showMessageDialog(null, "Please set up a Gmail account using Tools.");
					return;
				}
				Properties props = new Properties();
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.starttls.enable", "true");
				props.put("mail.smtp.host", "smtp.gmail.com");
				props.put("mail.smtp.port", "587");

				Session session = Session.getInstance(props,
				  new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				  });

				try {

					Message message = new MimeMessage(session);
					Address[] replyTo ={new InternetAddress(reply_to)}; 
					message.setReplyTo(replyTo);
					message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(send_to));
					message.setSubject("AGB: Token Release Request");
					String projectString = "";
					for(String project: projects){
						projectString = projectString+project+"\n";
					}
					message.setText(token_holder + ",\n\nYour collaborator, "+collaborator + " is requesting access to the projects"
						+ " listed below for which you currently hold the token(s):\n\n"+projectString
						+ "\n\nNote: this is an automated message from the AGB Project Manager. "
						+ "Reponses to this message will be re-directed to the collaborator, "+collaborator+".");

					Transport.send(message);
					JOptionPane.showMessageDialog(null, "Email was sent to " + send_to);

				} catch (MessagingException e) {
					JOptionPane.showMessageDialog(null, "Email was not sent successfully.\nPlease verify the Email account was setup correctly.");
				}
			}
		};
		ThreadPool.getAGBThreadPool().executeTask(sendThread);	   
	 }

}  