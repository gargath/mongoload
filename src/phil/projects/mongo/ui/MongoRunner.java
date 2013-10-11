package phil.projects.mongo.ui;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import phil.projects.mongo.MongoDBObjectFactory;
import phil.projects.mongo.MongoLoad;
import phil.projects.mongo.MongoLoadConfig;

/**
 * This is the runner class for the load that will be started in a separate thread. 
 * 
 * @author ptaprogg
 *
 */
public class MongoRunner implements Runnable {

	//The config object for the loader
	private MongoLoadConfig config;
	//The loader that will be used
	private MongoLoad loader;
	
	private static Logger logger = Logger.getLogger(MongoRunner.class);
	
	/**
	 * Constructor accepting the config object
	 * @param config
	 */
	public MongoRunner(MongoLoadConfig config) {
		this.config = config;
	}

	@SuppressWarnings("unused")
	private MongoRunner() {};
	
	@Override
	public void run() {
		//Create a new invoice loader. The type is interchangeable with other loaders
//		loader = new MongoLoad(config, new MongoInvoiceFactory());
		
		String factoryClassName = "phil.projects.mongo.MongoInvoiceFactory";
		
		MongoDBObjectFactory factory = null;
		try {
			logger.debug("Trying to get factory class");
			factory = (MongoDBObjectFactory)(Class.forName(factoryClassName)).newInstance();	
		}
		catch (ClassCastException cce) {
			throw new RuntimeException("Specified DBObjectFactory " + factoryClassName + " is of wrong type", cce);
		}
		catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("Specified DBObjectFactory " + factoryClassName + " not found");
		}
		catch (IllegalAccessException iae) {
			throw new RuntimeException("IllegalAccessException encountered when trying to instantiate " + factoryClassName);
		}
		catch (InstantiationException ie) {
			throw new RuntimeException("Failed to instantiate " + factoryClassName);
		}
		if (factory == null) {
			throw new RuntimeException("Failed to instantiate " + factoryClassName);
		}
		logger.debug("Factory class got");
		loader = new MongoLoad(config, factory);
		logger.debug("Loader initialized");
		//Test the connection
		loader.testConnection();
		logger.debug("Connection test complete");
		//Start the run
		try {
			loader.createAndPopulate();
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Unknown hostname " + config.getHostname());
		}
		logger.info("Loader thread finished");
	}
	
	/**
	 * This method is used to monitor the progress
	 * 
	 * @return The progress as reported by the loader or 0 if the loader has not yet been initialized
	 */
	public int getProgress() {
		if (loader == null) {
			return 0;
		}
		else {
			return loader.getProgressPercent();
		}
	}

}