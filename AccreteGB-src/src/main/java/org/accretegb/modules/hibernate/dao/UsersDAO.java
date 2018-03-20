package org.accretegb.modules.hibernate.dao;

import java.util.List;

import org.accretegb.modules.hibernate.Users;
import org.accretegb.modules.hibernate.HibernateSessionFactory;
import org.hibernate.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("usersDAO")
public class UsersDAO {
	
	private  final String USERNAMEPASSWORD = "select * from users u where u.username = :username and u.password = :password";
	
	@Autowired
	private HibernateSessionFactory hibernateSessionFactory;
	
	private static UsersDAO instance = null;	
	public static UsersDAO getInstance() {
	      if(instance == null) {
	         instance = new UsersDAO();
	      }
	      return instance;
	}
	
	@SuppressWarnings({ "-access", "unchecked" })
	public List<Users> findByUserNamePassport(final String username, String password) {
        Query query = hibernateSessionFactory.getSessionFactory().openSession().createSQLQuery(USERNAMEPASSWORD).addEntity(Users.class)
                .setParameter("username", username).setParameter("password", password);
        return (List<Users>) query.list();
	}

}