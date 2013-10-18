package phil.projects.mongo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * A singleton random generator providing numbers and strings.
 * 
 * @author ptaprogg
 *
 */
public class RandomGenerator {
	
	/**
	 * Exception thrown when the Generator runs out of unique strings of a given length. 
	 * This should rarely happen, usually for short string lengths or extremely long runs. 
	 *
	 */
	class UniqueStringSaturationException extends RuntimeException {
		
		static final long serialVersionUID = 2013100801;
		
		UniqueStringSaturationException(String s) {
			super(s);
		}
	}
	
	private static Logger logger = Logger.getLogger(RandomGenerator.class);
	
	//The java.util.Random generator used by this instance
	private Random rand = new Random(System.currentTimeMillis());
	
	//The singleton instance
	private static RandomGenerator instance = null;
	
	private RandomGenerator() {};

	//Getter method for Singleton instance
	public static RandomGenerator getInstance() {
		if (instance == null) {
			instance = new RandomGenerator();
		}
		return instance;
	}
	
	//Used to gather statistics on unique string retries
	private static long retryCount = 0;
	
	//Used to keep track of generated unique strings
	private HashSet<String> knownStrings = new HashSet<String>();
	
	//Used to keep track of the number of generated unique strings for calculating saturation
	private HashMap<Integer,Long> stringCount = new HashMap<Integer,Long>();
	
	//Characters to be used to form random strings
	static final String allowedCharacters = "abcdefghijklmnopqrstuvwxyz";
	
	/**
	 * Calculates the maximum possible number of random strings for a given length based
	 * on the number of allowed characters.  
	 * 
	 * @param n The length of string for which to calculate the number of combinations
	 * 
	 * @return The number of possible combinations as a long
	 */
	private long uniqueCapacity(int n) {
		int length = allowedCharacters.length();
		int capacity = 1;
		for (int i = 1; i <= n; i++) {
			capacity *= length; 
		}
		return capacity;
	}
	
	/**
	 * Generates a random string of a given length. Does not guarantee uniqueness.
	 * 
	 * @param length The length of the returned string
	 * @return A random string of the supplied length
	 */
	public String getRandomString(int length) {
		
		if (length > 1024) {
			throw new IllegalArgumentException("Requested length " + length + " is larger than 1024 characters");
		}
		//char array to hold the generated string
		char[] string = new char[length];

		//iterate over array length and pick a random character from allowedCharacters for each index
		for (int i = 0; i < length; i++) {
			string[i] = allowedCharacters.charAt(rand.nextInt(allowedCharacters.length()));
		}
		String ret = new String(string);
		logger.trace("Generated random string: " + ret);
		return ret;
	}

	/**
	 * Generates a random string of given length that is guaranteed to be unique for this instance of the
	 * RandomGenerator.
	 * 
	 * @param length The length of the returned string
	 * @return A unique random string of the supplied length
	 * 
	 * @throws UniqueStringSaturationException If the number of previously generated unique strings of this length is at 80% of the maximum number of possible combinations.
	 */
	public String getRandomUniqueString(int length) {
		
		if (length > 1024) {
			throw new IllegalArgumentException("Requested length " + length + " is larger than 1024 characters");
		}
		
		//Check whether the stringCount map already contains an entry for this length
		if (stringCount.get(length) == null) {
			//If not, create it and set the number of generated strings to 0
			stringCount.put(Integer.valueOf(length), Long.valueOf(0));
		}
		
		//Try to generate a unique string until one is found. Abort if this instance is nearing saturation for the given length of string
		do {
			String ret = getRandomString(length);
			if (knownStrings.add(ret)) {
				//A unique string was found. Add it to the list of strings and increment counter
				stringCount.put(Integer.valueOf(length), Long.valueOf((stringCount.get(Integer.valueOf(length)).intValue()+1)));
				return ret;
			}
			else {
				//The generated string has already been returned previously. Retry...
				retryCount++;
				logger.warn("Discarding non-unique String " + ret);
				logger.debug("Count for strings of length " + length + " is " + stringCount.get(Integer.valueOf(length)));
			}
		}
		//while will only be reached if a collision is detected. This allows for strings to be generated even past the cutoff point as long as no collisions occur.
		while (stringCount.get(Integer.valueOf(length)) < (uniqueCapacity(length)*.8));
		logger.error("Count for Strings of length " + length + " is at 80%. Aborting...");
		throw new UniqueStringSaturationException("Unique Strings of length " + length + " saturated.");
	}

	/**
	 * Simple pass-through method to retrieve an int within given range from the internal Random generator.
	 * 
	 * @param range The range to pass to the Random generator.
	 * @return An int between 0 and range-1
	 */
	public int getRandomIntInRange(int range) {
		return rand.nextInt(range);
	}
	
	/**
	 * Simple pass-through method to retrieve an int from the internal Random generator.
	 * 
	 * @return An int between 0 and INTEGER_MAXVALUE
	 */	
	public int getRandomInt() {
		return rand.nextInt();
	}
	
	/**
	 * Simple pass-through method to retrieve a double from the internal Random generator.
	 * 
	 * @return An random double
	 */	
	public double getRandomDouble() {
		return rand.nextDouble();
	}
	
	public String printStatistics() {
		float totalStrings = (float)knownStrings.size() + (float)retryCount;
		float wastage = ((float)Math.round(((float)retryCount / totalStrings)*10000))/100;
		
		return knownStrings.size() + " strings produces; " + retryCount + " retries; wastage: " + wastage + "%";
	}
	
}
