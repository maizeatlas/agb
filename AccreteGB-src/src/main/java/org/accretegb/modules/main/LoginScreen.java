package org.accretegb.modules.main;

import static org.accretegb.modules.config.AccreteGBBeanFactory.getContext;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.accretegb.modules.MainLayout;
import org.accretegb.modules.config.AccreteGBLogger;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.dao.UserDAO;
import org.accretegb.modules.projectmanager.PopulateProjectTree;
import org.accretegb.modules.util.LoggerUtils;
import org.hibernate.cfg.Configuration;

import com.jtattoo.plaf.smart.SmartLookAndFeel;
import com.mysql.jdbc.Connection;

import net.miginfocom.swing.MigLayout;

public class LoginScreen extends JFrame {
		
	private static final String DATABASE_CONNECTION_FAILED = "Database connection failed!";
	private static final String LOGIN_TITLE = "AccreteGB - The Breeder's ToolBox Login";
	public static final String MAIN_DATABASE_NAME = "agbv2";
	public static final String PROJECT_MANAGER_DB_NAME = "projectmanager";
	private static final int CONNECTION_FLAG = 1;
	private static final int USER_VALIDATION_FLAG = 2;
	private static final int USER_SIGNUP_FLAG = 3;
	private static final int AGB_INIT_FLAG = 4;
	
	final static String USER_DB_FILE = "credentials.txt";
	private static String KEY_FILE = "key.txt";
    private static String CREDENTIALS_REGEX = "UDEL19716ACCRETEGB";
    private boolean hibernateReconfigured = false;
    public static int loginUserId = -1;
    private int validConnection = -99;
    private int newUserId = -1;
    private boolean doneInit = false;
    private boolean doneSignup = false;
	ArrayList<String> readloginInfo= new ArrayList<String>();
	StringEncrypter stringEncrypter;
	JTextField serverName;
	JTextField port;
	JTextField dbUserName;
	JTextField dbPwd ;
	JLabel dbMsg = new JLabel();
	JLabel loginMsg = new JLabel();
	JLabel signupMsg= new JLabel();
	
	public void initialize() {
		setSize(new Dimension(500, 365));	
		setTitle(LOGIN_TITLE);
		Dimension screenDims = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenDims.width / 2 - getSize().width / 2, screenDims.height / 2- getSize().height / 2);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
		JPanel main = new JPanel(new MigLayout("insets 10, gap 5"));
		getContentPane().add(main);
		addDBsettingPanel(main);
		addSignPanel(main);
		addLoginPanel(main);
		
	}
	
	public void addDBsettingPanel(JPanel main){
		JPanel DBsettingPanel = new JPanel(new MigLayout("insets 5, gap 5"));
		DBsettingPanel.setBorder(BorderFactory.createTitledBorder("Database Setting"));
		DBsettingPanel.add(new JLabel("Server Name: "));
		serverName = new JTextField(12);
		DBsettingPanel.add(serverName);
		
		DBsettingPanel.add(new JLabel("Port Number: "));
		port = new JTextField(12);
		DBsettingPanel.add(port,"wrap");
		
		DBsettingPanel.add(new JLabel("User Name: "));
		dbUserName = new JTextField(12);
		DBsettingPanel.add(dbUserName);
		
		DBsettingPanel.add(new JLabel("Password: "));
		dbPwd = new JPasswordField(12);
		DBsettingPanel.add(dbPwd,"wrap");
		
		dbMsg.setForeground(Color.red);
		dbMsg.setFont(new Font("Verdana", Font.PLAIN, 10));
		DBsettingPanel.add(dbMsg,"span, split 2, hidemode 1, al right");
		
		JButton connect = new JButton("Validate");
		DBsettingPanel.add(connect);
		
		readLoginInfoFromFile();
		if(!readloginInfo.isEmpty()){
			serverName.setText(readloginInfo.get(0));
			port.setText(readloginInfo.get(1));
			dbUserName.setText(readloginInfo.get(2));
			dbPwd.setText(readloginInfo.get(3));
			//Do not try to connect when login window opens
			//validateDBconnection();
		}else{
			dbMsg.setText("Please configure database connection before logging in!");
			dbMsg.setVisible(true);
		}	
		connect.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				validateDBconnection();
			}
			
		});
		
		main.add(DBsettingPanel,"w 100%,wrap");
		
	}
	
	public void validateDBconnection(){
		
		if(validConnection == 0) {
			// connection was validated.
			return;
		}else {
			validConnection = -99;
		}
		
		getProgressBarWorker("Validating the database connection", CONNECTION_FLAG);
		SwingWorker connectionWorker = new SwingWorker() {
	        @Override
	        protected Object doInBackground() throws Exception {
	        	validConnection = validateDBsetting(serverName.getText().trim(),port.getText().trim(),dbUserName.getText().trim(),dbPwd.getText().trim());									
	            return null;
	        }

	        @Override
	        public void done(){
	        	switch(validConnection){
	        	case 0: 
	        		dbMsg.setText("Valid connection!");
	        		dbMsg.setVisible(true);	
	        		loginMsg.setText("");
	        		signupMsg.setText("");
	        		break;
	        	case -1:
	        		dbMsg.setText("Connection settings can not be empty");
	        		break;
	        	case -2:
	        		dbMsg.setText("<HTML>Connection failed!<br>Ensure the server host both databases \""+ MAIN_DATABASE_NAME + "\" and \""+ PROJECT_MANAGER_DB_NAME +"\"</HTML>");
	        		break;
	        	case -3:
	        		dbMsg.setText("<HTML>Connection can not be established!<br>Please check database settings, internet settings and database server");
	        		break;
	        	}
	        	if(validConnection < 0){	
	        		dbMsg.setVisible(true);
	        		hibernateReconfigured=false;
	        	}
	        }
	    };
	    connectionWorker.execute(); 
	}
	
	private void getProgressBarWorker(String label, final int flag) {
		final JDialog barDialog = new JDialog();
		barDialog.setLayout(new FlowLayout());
		barDialog.setAlwaysOnTop(true);
		barDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

	    final JProgressBar jProgressBar = new JProgressBar(0, 100);
	    final JLabel status = new JLabel(label +": ");
	    barDialog.add(status);
	    barDialog.add("jProgressBar", jProgressBar);

	    barDialog.pack();
	    barDialog.setLocationRelativeTo(this);
	    barDialog.setVisible(true);
	    SwingWorker barWorker = new SwingWorker() {
	        @Override
	        protected Object doInBackground() throws Exception {
	        	int i = 0;
	        	while (i < 100) {
	        		i++;
	        		jProgressBar.setValue(i);
	        		Thread.sleep(1000);
	        		if (flag == CONNECTION_FLAG && validConnection != -99) {
	        			break;
	        		}else if (flag == USER_VALIDATION_FLAG && loginUserId  != -1) {
	        			break;
	        		}else if (flag == AGB_INIT_FLAG && doneInit) {
	        			break;
	        		}else if (flag == USER_SIGNUP_FLAG && doneSignup) {
	        			break;
	        		}
	        		if (i == 99 ) {
	        			i = 1;
	        		}
	        	}
	            return null;
	        }

	        @Override
	        public void done(){
	        	barDialog.setVisible(false);
	        	barDialog.dispose();
	        }
	    };
	    barWorker.execute(); 
		
	}
	
	public void reConfigHibernate(){
		Configuration config = new Configuration();
		config.configure("hibernate.cfg.xml");
		String sql_mode = "?sessionVariables=sql_mode=''";//";sessionVariables=sql_mode='STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'";
		config.setProperty("hibernate.connection.url", "jdbc:mysql://"+serverName.getText()+":"+port.getText()+"/"+MAIN_DATABASE_NAME+sql_mode);
		config.setProperty("hibernate.connection.username", dbUserName.getText().trim());	
		config.setProperty("hibernate.connection.password", String.valueOf(dbPwd.getText().trim()).equals("")? "" :dbPwd.getText());
		HibernateSessionFactory.setSessionFactory(config.buildSessionFactory());
	    
		
		config.configure("pmhibernate.cfg.xml");		
		config.setProperty("hibernate.connection.url", "jdbc:mysql://"+serverName.getText()+":"+port.getText()+"/"+PROJECT_MANAGER_DB_NAME+sql_mode);
		config.setProperty("hibernate.connection.password", String.valueOf(dbPwd.getText().trim()).equals("")? "" :dbPwd.getText());
		config.setProperty("hibernate.connection.username", dbUserName.getText());		
		HibernateSessionFactory.setPmSessionFactory(config.buildSessionFactory());
		hibernateReconfigured = true;
	}
	public void addLoginPanel(JPanel main){
		JPanel loginPanel = new JPanel(new MigLayout("insets 5, gap 5"));
		loginPanel.setBorder(BorderFactory.createTitledBorder("Login"));
		loginPanel.add(new JLabel("User Name:   "));
		final JTextField userName = new JTextField(12);
		loginPanel.add(userName);
		
		loginPanel.add(new JLabel("Password:      "));
		final JTextField pwd = new JPasswordField(12);
		loginPanel.add(pwd,"wrap");
		
		loginMsg.setForeground(Color.red);
		loginMsg.setFont(new Font("Verdana", Font.PLAIN, 10));
		loginPanel.add(loginMsg,"span, split 2, hidemode 1, al right");
		
		final JButton login = new JButton("Login");
		loginPanel.add(login);
		
		if(!readloginInfo.isEmpty()){
			userName.setText(readloginInfo.get(4));
			pwd.setText(readloginInfo.get(5));
		}
		
		login.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				login.setEnabled(false);
				loginUserId = -1;
				SwingWorker loginWorkder = new SwingWorker() {
			        @Override
			        protected Object doInBackground() throws Exception {
			        	validateDBconnection();
			        	while(validConnection == -99) {
			        		Thread.sleep(500);
			        	}
			        	if(validConnection == 0){
							getProgressBarWorker("Validating the user", USER_VALIDATION_FLAG);
							if( !hibernateReconfigured){
								reConfigHibernate();
							}
							loginUserId = UserDAO.getInstance().findByUserNamePwd(userName.getText().trim(), pwd.getText().trim());
						}else {
			        		loginMsg.setText(DATABASE_CONNECTION_FAILED);
							login.setEnabled(true);		
						}		        	
			        	return null;
			        }

			        @Override
			        public void done(){
			        	if(loginUserId > 0){
							loginMsg.setText("Valid user!");
							signupMsg.setText("");
							writeCredentialsFile(serverName.getText().trim(), port.getText().trim(), dbUserName.getText().trim(),dbPwd.getText().trim(),userName.getText().trim(), pwd.getText().trim());
			        	}else{
			        		loginUserId= -2;
							loginMsg.setText("Invalid user name or password! ");
							login.setEnabled(true);
						}
			        }
			    };
			    loginWorkder.execute(); 
			}			
		});
		
		
		SwingWorker mainFrameWorker = new SwingWorker() {
	        @Override
	        protected Object doInBackground() throws Exception {
	        	while(loginUserId < 0) {
	        		Thread.sleep(1000);
	        	}
	        	getProgressBarWorker("Initializing AccreteGB", AGB_INIT_FLAG);
	        	MainLayout mainLayout = (MainLayout) getContext().getBean("mainLayoutBean");
	        	mainLayout.getFrame().setVisible(true);
	        	
	        	return null;
	        }

	        @Override
	        public void done(){
	        	doneInit = true;
	        	setVisible(false);
	        	dispose();
	        	PopulateProjectTree populateProjectTree = new PopulateProjectTree(loginUserId);
	        }
	    };
	    mainFrameWorker.execute(); 
		
		
		main.add(loginPanel,"w 100%, wrap");
		
	}
	
	public void addSignPanel(JPanel main){
		JPanel signupPanel = new JPanel(new MigLayout("insets 5, gap 5"));
		signupPanel.setBorder(BorderFactory.createTitledBorder("Signup"));
		signupPanel.add(new JLabel("User Name:    "));
		final JTextField userName = new JTextField(12);
		signupPanel.add(userName);
		
		signupPanel.add(new JLabel("Email:   "));
		final JTextField email = new JTextField(12);
		signupPanel.add(email, "wrap");
		
		signupPanel.add(new JLabel("First Name:   "));
		final JTextField firstName = new JTextField(12);
		signupPanel.add(firstName);
		
		signupPanel.add(new JLabel("Password:   "));
		final JPasswordField pwd = new JPasswordField(12);
		signupPanel.add(pwd,"wrap");
		
		signupPanel.add(new JLabel("Last Name:   "));
		final JTextField lastName = new JTextField(12);
		signupPanel.add(lastName);
		
		signupPanel.add(new JLabel("Confirm Pwd:"));
		final JPasswordField confirmPwd = new JPasswordField(12);
		signupPanel.add(confirmPwd,"wrap");
		
		signupMsg.setForeground(Color.red);
		signupMsg.setFont(new Font("Verdana", Font.PLAIN, 10));
		signupPanel.add(signupMsg,"span,split 2,  al right, hidemode 1");
		Vector<Component> order = new Vector<Component>(5);
	    order.add(userName);
	    order.add(firstName);
	    order.add(lastName);
	    order.add(email);
	    order.add(pwd);
	    order.add(confirmPwd);
	    MyOwnFocusTraversalPolicy newPolicy = new MyOwnFocusTraversalPolicy(order);
	    signupPanel.setFocusCycleRoot(true);
	    signupPanel.setFocusTraversalPolicy(newPolicy);
	    
	    final JButton signup = new JButton("Sign up");
		signupPanel.add(signup);
		
		signup.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				if(userName.getText().trim().equals("") || firstName.getText().trim().equals("")
						|| lastName.getText().trim().equals("") || pwd.getText().trim().equals(""))
				{
					signupMsg.setText("All fields are required! ");
					return;
				}
				if(!pwd.getText().trim().equals(confirmPwd.getText().trim())){
					signupMsg.setText("Password mismatch !");
					return;
				}
				doneSignup = false;
				signup.setEnabled(false);
				SwingWorker signupWorkder = new SwingWorker() {
			        @Override
			        protected Object doInBackground() throws Exception {
			        	validateDBconnection();
			        	while(validConnection == -99) {
			        		Thread.sleep(500);
			        	}
			        	if(validConnection == 0){
							getProgressBarWorker("Signing up the user", USER_SIGNUP_FLAG);
							if( !hibernateReconfigured){
								reConfigHibernate();
							}
							newUserId = UserDAO.getInstance().insert(userName.getText().trim(), pwd.getText().trim(), 
									firstName.getText().trim(),lastName.getText().trim(), email.getText().trim());
						}
			        	return null;
			        }

			        @Override
			        public void done(){
			        	doneSignup = true;
			        	if(newUserId == 0){
							signupMsg.setText("Username already exsits!");
						}else{
							signupMsg.setText("User profile was saved!");
						}
			        	signup.setEnabled(true);
			        }
			    };
			    signupWorkder.execute(); 						
			}
			
		});
		
		main.add(signupPanel,"w 100%, wrap");
		
	}
	
	public static class MyOwnFocusTraversalPolicy extends FocusTraversalPolicy
	{
		Vector<Component> order;

		public MyOwnFocusTraversalPolicy(Vector<Component> order) {
			this.order = new Vector<Component>(order.size());
			this.order.addAll(order);
		}
		public Component getComponentAfter(Container focusCycleRoot,
				Component aComponent)
		{
			int idx = (order.indexOf(aComponent) + 1) % order.size();
			return order.get(idx);
		}

		public Component getComponentBefore(Container focusCycleRoot,
				Component aComponent)
		{
			int idx = order.indexOf(aComponent) - 1;
			if (idx < 0) {
				idx = order.size() - 1;
			}
			return order.get(idx);
		}

		public Component getDefaultComponent(Container focusCycleRoot) {
			return order.get(0);
		}

		public Component getLastComponent(Container focusCycleRoot) {
			return order.lastElement();
		}

		public Component getFirstComponent(Container focusCycleRoot) {
			return order.get(0);
		}
	}
	
	public int validateDBsetting(final String server, final String port, final String username, final String pwd){
	        int valid = 0;
	        if(server.trim().equals("") || port.trim().equals("") || username.equals("") )
			{
	        	valid = -1;
			}
	        String url = "jdbc:mysql://" + server+ ":"+port+"?connectTimeout=5000";
	        Connection con = null;
	        ResultSet rs = null;
		    try {
		    	Class.forName("com.mysql.jdbc.Driver");
		    	con = (Connection) DriverManager
						.getConnection(url, username,pwd);	
		    	if(con != null){
		    		rs = con.getMetaData().getCatalogs();
		    		int exist = 0;
		    		while(rs.next()){
		    			String db = rs.getString(1);		    			
		    			if(db.equalsIgnoreCase(PROJECT_MANAGER_DB_NAME) || db.equalsIgnoreCase(MAIN_DATABASE_NAME)){
		    				exist++;
		    			}
		    		}
		    		if(exist == 2){
		    			valid = 0;
		    		}else{
		    			valid = -2;
		    		}
		    	}else{
		    		return -3;
		    	}
		    	
		    } catch (java.lang.ClassNotFoundException e1) {
		    } catch (SQLException e2) {
		    	valid = -3;
		    }finally{
		    	if(rs != null){
		    		try {
						rs.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    	if(con != null){
		    		try {
						con.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    	}
		    }
		    
		   return valid;
	}
	
	 /**
     * writes username and password to local file
     * @param username - username to be written
     * @param password - password to be checked
     * 
     */
    private void writeCredentialsFile(String server, String port, String dbuser, String dbPwd, String user, String userPwd){
    	 
		try {
			StringEncrypter encrypter = new StringEncrypter();
			File keyFile = new File(KEY_FILE);
			if(!keyFile.exists()){
				encrypter.writeKeyToFile(keyFile);
			}
	    	encrypter.readAndSetKeyFromFile(keyFile);
	    	
	    	String[] credentials = { server, port, dbuser, dbPwd, user, userPwd};
	    	encrypter.encryptToFile(USER_DB_FILE, credentials, CREDENTIALS_REGEX);
	    	
			
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    }
    
	
	/**
	 * read database settings and user info from local file
	 * @return
	 */
	public void readLoginInfoFromFile(){
		
		File userFile = new File(USER_DB_FILE);
    	File keyFile = new File(KEY_FILE);
		if(userFile.exists() && userFile.length() != 0 && keyFile.exists() && keyFile.length() != 0){	
			try {
				StringEncrypter encrypter = new StringEncrypter();
				encrypter.readAndSetKeyFromFile(keyFile);
				
				String[] credentials = encrypter.decryptFromFile(USER_DB_FILE, CREDENTIALS_REGEX);
				if(credentials.length == 6){
					for(String c : credentials)
						readloginInfo.add(c);
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
	
	 /**
     * The Main Method of the application
     * Application starts here.
     * @param args
     */
    public static void main(String[] args) {
        try {
           Properties props = new Properties();
           props.put("logoString", "");
           SmartLookAndFeel.setCurrentTheme(props);
           UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
           LoginScreen login = new LoginScreen();
           login.initialize();
           login.setVisible(true);
           AccreteGBLogger.setLogEnabled(true);
        } catch (Exception ex) {
            if (LoggerUtils.isLogEnabled()) {
                LoggerUtils.log(Level.INFO, "Not able to verify user : " + ex);
            }
        }
    }

}
