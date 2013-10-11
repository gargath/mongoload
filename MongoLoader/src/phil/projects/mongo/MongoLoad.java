package phil.projects.mongo;

import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class MongoLoad {
	
	private static Logger logger = Logger.getLogger(MongoLoad.class);
	
	//The config object holding connection details
	private MongoLoadConfig config;
	
	//The document factory used to generate the documents being inserted
	private MongoDBObjectFactory documentFactory;
	
	
	//Used to keep track of the generation progress
	protected int progress = 0;
	
	public MongoLoad(MongoLoadConfig config, MongoDBObjectFactory factory) {
		this.config = config;
		this.documentFactory = factory;
	}
	
	/**
	 * Helper method to connect to Mongo instance
	 * 
	 * @return The requested Mongo database once connection succeeds
	 * @throws MongoException If a problem during communication with MongoDB occurs
	 * @throws IllegalArgumentException If the port number or the provided DBs are invalid, null or empty
	 * @throws UnknownHostException If the hostname specified for the Mongo instance cannot be resolved
	 */
	DB connect() throws IllegalArgumentException, UnknownHostException {
		//Assume localhost for empty hostname
		if ((config.getHostname() == null) || (config.getHostname().equals(""))) {
			logger.info("No hostname supplied, assuming localhost");
			config.setHostname("localhost");
		}
		//Check for valid port number
		if ((config.getPort() == 0) || (config.getPort() > 65535)) {
			throw new IllegalArgumentException("Invalid port " + String.valueOf(config.getPort()) + ". Unable to connect.");
		}
		//Ensure that database names were provided
		if ((config.getUserDB() == null) || (config.getUserDB().equals(""))) {
			logger.error("User DB name missing. Unable to connect.");
			throw new IllegalArgumentException("User DB name missing");
		}
		
		//Now connect the client
		MongoClient client = new MongoClient(config.getHostname(), config.getPort());

		DB db = null;
		
		//If no username was given, simply return the requested DB without authentication
		if ((config.getUsername() == null) || (config.getUsername().equals(""))) {
			db =  client.getDB(config.getUserDB());
		}
		else {
			//else get authDB...
			if ((config.getAuthDB() == null) || (config.getAuthDB().equals(""))) {
				throw new IllegalArgumentException("Username provided but authentication DB missing");
			}
			DB aDB = client.getDB(config.getAuthDB());
			if (aDB == null) {
				throw new IllegalArgumentException ("Requested authentication DB does not exist");
			}
			//...and try to authenticate
			boolean auth = false;
			try {
				auth = aDB.authenticate(config.getUsername(), config.getPassword().toCharArray());
			}
			catch (Exception e) {
				//If an exception occurs during connect, throw it up as a RuntimeException
				throw new RuntimeException(e);
			}

			if (!auth) {
				throw new IllegalArgumentException("Authentication against DB " + config.getAuthDB() + " for user " + config.getUsername() + " failed.");
			}
			else {
				//If authentication succeeds, return requested DB 
				db = aDB.getSisterDB(config.getUserDB());
			}
		}
		if (db == null) {
			//The requested DB does not exist
			throw new IllegalArgumentException("Requested DB does not exist");
		}
		return db;
	}

	/**
	 * Generates a number of invoices and stores them in Mongo DB provided.
	 * The invoices collection used to store the invoices is dropped and re-created each time. 
	 * 
	 */
	public void createAndPopulate() throws UnknownHostException {
		//Mark start time
		long startTime = System.currentTimeMillis();

		DB db = null;
		//Connect and retrieve the DB
		db = connect();

		if (db == null) {
			logger.fatal("Connect did not return a DB. This should never happen... Aborting...");
			System.exit(2);
		}
		logger.debug("Successfully connected to DB " + db.getName());
		
		//Get the invoices collection...
		DBCollection invoices = db.getCollection("invoices");
		logger.info("Retrieved invoices collection");
		//...and drop it. This will always succeed, no need to guard against null
		invoices.drop();
		logger.debug("Collection emptied");
		logger.info("Starting to generate " + config.getNumdocs() + " invoices.");
		
		//Generate invoices and save each to the collection
		for (progress = 0; progress < config.getNumdocs(); progress++) {
			DBObject invoice = documentFactory.generateDocument();
			//using WriteConcern.ACKNOWLEDGED to ensure each document is at least accepted by the master
			invoices.save(invoice, WriteConcern.ACKNOWLEDGED);
			logger.debug("Saved an invoice to Mongo. Invoices remaining: " + (config.getNumdocs() - (progress+1)));
		}

		//Check how many invoices actually made it into the collection
		long storedInvoices = invoices.count();
		//Mark completion time
		long endTime = System.currentTimeMillis();
		logger.info("Generate run completed in " +(endTime-startTime)+ "ms. Invoice collection now holds " + storedInvoices + " invoice documents.");
		if (storedInvoices != config.getNumdocs()) {
			//This can happen if other concurrent connections modified the collection while we were running.
			logger.warn("Number of invoices in collection does not match number of invoices generated!");
		}
		return;
	}
	
	/**
	 * Allows to test the connection parameters by creating an authenticated connection and retrieving a president.
	 * 
	 * @return True if connection succeeds and a president is found, false otherwise
	 */
	public boolean testConnection() {
		
		DB db = null;
		//Try to use connect helper to access the requested DB
		try {
			db = connect();
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		logger.info("Successfully connected to DB " + db.getName());
		//Get the presidents collection and retrieve a president to demonstrate working connection and auth success
		DBCollection collection = db.getCollection("presidents");
		DBObject president = collection.findOne();
		//findOne() will return null if authentication failed or presidents are empty
		if (president == null) {
			System.out.println("No president!");
			return false;
		}
		else {
			System.out.println(president.toString());
		}
		return true;
	}
	
	/**
	 * Get the progress made into generation so far.
	 * 
	 * @return The completed percentage
	 */
	public int getProgressPercent() {
		return Math.round((((float)this.progress/(float)this.config.getNumdocs())*100));
	}
	

}
