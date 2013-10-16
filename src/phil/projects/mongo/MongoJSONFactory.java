package phil.projects.mongo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

/**
 * DBObject factory creating DBObjects with random data based on a sample provided as a JSON file
 * 
 * It will read in the file and try to convert it into its BSON representation. It will then create a document with the same structure and all random values.
 * Note: Sample parsing will only be done the first time generateDocument() is called.
 * 
 * @author ptaprogg
 *
 */
public class MongoJSONFactory implements MongoDBObjectFactory {

	private static final Logger logger = Logger.getLogger(MongoJSONFactory.class);
	
	//The RandomGenerator this factory will use
	private final RandomGenerator rand = RandomGenerator.getInstance();
	
	//The sample used during object generation
	private DBObject sample = null;
	
	/**
	 * Worker method that assembles a BasicDBObject based on the sample provided.
	 * It will iterate over the keys in the sample and generate random values for each.
	 * If a key holds a subdocument, it will recursively generate a random DBObject for this.
	 * 
	 * @param sample The sample DBObject to follow
	 * @return A DBObject with random data based on the sample's structure
	 */
	private final DBObject assembleDBObject(DBObject sample) {

		logger.debug("Starting to assemble object");
		
		//Create a fresh DBObject to load
		BasicDBObject generatedObject = new BasicDBObject(); 
		
		//Get the key set from the sample and iterate over that
		for (String key : sample.keySet() ) {
			Object sampleValue = sample.get(key);
			logger.trace("Value read from sample for key " + key + ": " + sampleValue.toString());
			
			if (sampleValue instanceof String) {
				logger.debug("Generating String value for key " + key);
				generatedObject.put(key, rand.getRandomString(((String)sampleValue).length()));
			}
			else if ((sampleValue instanceof Long) || (sampleValue instanceof Integer)) {
				logger.debug("Generating Integer value for key " + key);
				generatedObject.put(key, rand.getRandomInt());
			}
			else if (sampleValue instanceof Double) {
				logger.debug("Generating Double value for key " + key);
				generatedObject.put(key, rand.getRandomDouble());
			}
			else if (sampleValue instanceof Boolean) {
				logger.debug("Generating Boolean value for key " + key);
				if (rand.getRandomIntInRange(2) == 0) {
					generatedObject.put(key,true);
				}
				else {
					generatedObject.put(key, false);
				}
			}
			else if (sampleValue instanceof BasicDBObject) {
				logger.debug("Generating subdocument for key " + key);
				generatedObject.put(key, assembleDBObject((BasicDBObject)sample.get(key)));
			}
			else if (sampleValue instanceof BasicDBList) {
				BasicDBList list = (BasicDBList)sampleValue;
				BasicDBList out = new BasicDBList();
				for (Object item : list) {
					try {
						out.add(rand.getRandomString(((String)item).length()));						
					}
					catch (ClassCastException cce) {
						logger.error("Error processing token '" + sampleValue.toString() + "'. Contains non-string class " + item.getClass().getCanonicalName());
						throw cce;
					}
				}
				generatedObject.put(key, out);
			}
			else {
				logger.error("Unsupported data type in sample. Object at \"" + key + "\": " + sample.get(key).getClass().getCanonicalName());
				throw new IllegalArgumentException("Unsupported data type in sample document. Object at \"" + key + "\": " + sample.get(key).getClass().getCanonicalName());
			}
		}
		logger.debug("Document generation complete.");
		logger.trace("Generated object: " + generatedObject.toString());
		return generatedObject;
	}
	
	private String readSample(String path, MongoLoadConfig config) throws IOException {
		File f = new File(path);
		if (!f.canRead()) {
			logger.info("Sample file not found at absolute location. Will try relative to userdir");
			f = new File(System.getProperty("user.dir")+path);
			if (!f.canRead()) {
				throw new IllegalArgumentException("Sample file not found");
			}
		}
		
		if (f.length() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Sample file too big.");
		}
		
		FileInputStream fis = new FileInputStream(f);
		byte[] fileContents = new byte[(int)f.length()];
		int bytesRead = fis.read(fileContents);
		while (bytesRead < f.length()) {
			logger.debug("Having to read in multiple steps...");
			bytesRead = bytesRead + fis.read(fileContents, bytesRead-1, fis.available());
		}
		fis.close();
		try {
			String sampleString = new String(fileContents, (("".equals(config.getSampleEncoding()) ? "UTF-8" : config.getSampleEncoding())));
			return sampleString;
		}
		catch (UnsupportedEncodingException uce) {
			logger.error("Specified character encoding " + config.getSampleEncoding() + " is not supported.");
			throw uce;
		}
		
//		return new String(fileContents, (config.getSampleEncoding() == null ? "UTF-8" : config.getSampleEncoding()));
	}
	
	@Override
	public DBObject generateDocument(MongoLoadConfig config) {
		if (sample == null) {
			logger.info("Initializing model");
			try {
				String JSONSample = readSample(config.getSamplePath(), config);
				sample = (DBObject)JSON.parse(JSONSample);
			}
			catch(IOException ioe) {
				logger.error("IO Exception while parsing JSON: " + ioe.getMessage());
				ioe.printStackTrace();
			}
			catch(JSONParseException jpe) {
				logger.error("JSON parse exception while trying to parse sample: " + jpe.getMessage());
				jpe.printStackTrace();
			}
		}
		logger.info("Model init complete");
		return assembleDBObject(sample);
	}

}
