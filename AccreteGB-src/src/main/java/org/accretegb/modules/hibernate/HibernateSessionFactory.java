package org.accretegb.modules.hibernate;

/*
* Licensed to Openaccretegb-common under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. Openaccretegb-common licenses this
* file to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

@Component("hibernateSessionFactory")
public class HibernateSessionFactory {

	private static SessionFactory sessionFactory ;
	
	private static SessionFactory pmSessionFactory ;

	@SuppressWarnings("deprecation")
	/*private static SessionFactory buildSessionFactory() {
		try {
			Configuration conf = new Configuration();
			
			return conf.configure("hibernate.cfg.xml").buildSessionFactory();
		
		} catch (Throwable ex) {
			
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static SessionFactory buildPmSessionFactory() {
		try {
			Configuration conf = new Configuration();
			
			return conf.configure("pmhibernate.cfg.xml").buildSessionFactory();
		
		} catch (Throwable ex) {
			
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}*/

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public static SessionFactory getPmSessionFactory() {
		return pmSessionFactory;
	}
	
	public static void setSessionFactory(SessionFactory sessionFactory ) {
		HibernateSessionFactory.sessionFactory = sessionFactory;
	}
	
	public static void setPmSessionFactory(SessionFactory sessionFactory) {
		HibernateSessionFactory.pmSessionFactory = sessionFactory;
	}

	public static void shutdown() {
		getSessionFactory().close();
		getPmSessionFactory().close();
	}
	
	

}
