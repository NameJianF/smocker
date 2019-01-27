package com.jenetics.smocker.dao;

import javax.inject.Inject;

import org.jboss.logging.Logger;

import com.jenetics.smocker.model.config.SmockerConf;

public class DaoConfigUpdaterThread {
	
	private static DaoConfigUpdaterThread instance = null;
	private static final int SLEEP_TIME = 2000;
	private static SmockerConf singleConf = null;
	@Inject
	private Logger logger;
	
	private DaoConfigUpdaterThread() {
		super();
	}
	
	public static synchronized DaoConfigUpdaterThread getInstance() {
		if (instance == null) {
			instance = new DaoConfigUpdaterThread();
			instance.start();
		} 
		return instance;
	}
	
	public static SmockerConf getSingleConf() {
		return singleConf;
	}

	private void start() {
		Thread thread = new Thread(this::run);
		thread.start();
	}
	
	private void run () {
		while (true) {
			try {
				singleConf = DaoConfig.getSingleConfig();
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				logger.error("Unable to update config", e);
			}
			
		}
	}
	
	
}