package org.accretegb.modules.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import org.accretegb.modules.hibernate.dao.ClassificationDAO;

public class ThreadPool {
	
	private static ThreadPool instance = null;
	private static ExecutorService executor = Executors.newFixedThreadPool(15);		
	private ThreadPool(){}
	
	public static ThreadPool getAGBThreadPool() {
	      if(instance == null) {
	         instance = new ThreadPool();
	      }
	      return instance;
	}
	
	public void scheduleTask(SwingWorker swingWorker){
		executor.submit(swingWorker);
		
	}

	public void scheduleTask(Thread thread){
		executor.submit(thread);
		
	}

	public Future submitTask(Runnable runnable){
		return executor.submit(runnable);
		
	}
	
	public void executeTask(Runnable runnable){
		 executor.execute(runnable);
		
	}
	
	/*public void waitAndInvoke(Runnable runnable){
		 executor.
		
	}*/
		
	
}
