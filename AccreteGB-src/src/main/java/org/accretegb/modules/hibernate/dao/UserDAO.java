package org.accretegb.modules.hibernate.dao;

// default package
// Generated May 31, 2015 9:04:43 PM by Hibernate Tools 3.4.0.CR1

import java.util.ArrayList;
import java.util.List;

import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.accretegb.modules.hibernate.User;
import org.apache.commons.codec.binary.Base64;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;

public class UserDAO{
	
private  final String USERNAMEPASSWORD = "select * from user u where u.user_name = :username and u.password = :password";
	
	@Autowired
	private  HibernateSessionFactory hibernateSessionFactory;
	
	private static UserDAO instance = null;	
	public static UserDAO getInstance() {
	      if(instance == null) {
	         instance = new UserDAO();
	      }
	      return instance;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  int findByUserNamePwd(final String username, String password) {
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();
		password = encodePass(password);
        Query query = session.createSQLQuery(USERNAMEPASSWORD).addEntity(User.class)
                .setParameter("username", username)
                .setParameter("password", password);
       
        int userId = 0;
        if(((List<User>) query.list()).size()>0)
        {
        	userId =  ((List<User>) query.list()).get(0).getUserId();
        }	
        return userId;
	}
	
	public  String findUserName (int userId){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from user where user_id = "+ userId).addEntity(User.class);
		List<User> UserList = (List<User>)query.list();
		String userName = UserList.get(0).getUserName();
		session.close();
		return userName;
	}
	
	public  int findUserId (String user_name){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from user where user_name = '"+user_name+"'")
				.addEntity(User.class);

		List<User> UserList = (List<User>)query.list();
		int userId = UserList.get(0).getUserId();
		session.close();
		
		return userId;
			
	}
	
	public String findEmail (String user_name){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from user where user_name = '"+user_name+"'")
				.addEntity(User.class);

		List<User> UserList = (List<User>)query.list();
		String email = UserList.get(0).getEmail();
		session.close();
		
		return email;
			
	}
	
	public User findUser (String user_name){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();		
		Query query = session.createSQLQuery("select * from user where user_name = '"+user_name+"'")
				.addEntity(User.class);

		List<User> UserList = (List<User>)query.list();
		session.close();
		
		return UserList.get(0);
			
	}
	
	public  ArrayList<String> findUserNames (List<Integer> userIds){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		ArrayList<String> userNames = new ArrayList<String>();
		for(Integer userId : userIds){
			Query query = session.createSQLQuery("select * from user where user_id = "+ userId).addEntity(User.class);
			List<User> userList = (List<User>)query.list();
			userNames.add(userList.get(0).getUserName());
			
		}
		session.close();
		return userNames;
	}
	
	public  ArrayList<Integer> findAllUserIds (){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();	
		ArrayList<Integer> userIds = new ArrayList<Integer>();	
		Query query = session.createSQLQuery("select * from user").addEntity(User.class);
		List<User> userList = (List<User>)query.list();
		for(User user : userList){
			userIds.add(user.getUserId());
		}				
		session.close();
		return userIds;
	}
	
	
	@SuppressWarnings({ "-access", "unchecked" })
	public  int insert(String userName, String password, String firstName, String lastName, String email){
		Session session = hibernateSessionFactory.getPmSessionFactory().openSession();
		Query query = session.createSQLQuery(
				"select * from user where user_name = '"+userName+"'");
		if(query.list().size()>0){
			session.close();
			return 0;
		}else{
			Session sessionNew = hibernateSessionFactory.getPmSessionFactory().openSession();	
			Transaction transaction = session.beginTransaction();
			password = encodePass(password);
			User newUser = new User( userName,  password,  firstName, lastName, email);
			session.save(newUser);
			transaction.commit();
			session.close();
			sessionNew.close();
			return 1;
		}
		
	}
	
	
	private String encodePass(String pass){
		// encode data on your side using BASE64
        byte[] bytesEncoded = Base64.encodeBase64(pass.getBytes());
        String encodedPass = new String(bytesEncoded);
        return encodedPass;
	}
	
	private String decodePass(String pass){
		 byte[] bytesEncoded = pass.getBytes();
         // Decode data on other side, by processing encoded data
         byte[] valueDecoded= Base64.decodeBase64(bytesEncoded );
         String decodedPass = new String(valueDecoded);
         return decodedPass;
	}
	
}
