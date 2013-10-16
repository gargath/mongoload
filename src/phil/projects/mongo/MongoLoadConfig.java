package phil.projects.mongo;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Config object for loaders. It stores the connection details for the Mongo instance and the number of documents to generate.
 * 
 * @author ptaprogg
 *
 */
public class MongoLoadConfig {

	private static Logger logger = Logger.getLogger(MongoLoadConfig.class);
	
	/**
	 * Builder class for the config object.
	 * 
	 * @author ptaprogg
	 *
	 */
	public static final class MongoLoadConfigBuilder {
		private String hostname;
		private int port;
		private String userDB;
		private String authDB;
		private String username;
		private String password;
		private int numdocs;
		private String samplePath;
		private String sampleEncoding;
		
		public MongoLoadConfigBuilder(String userDB, int numdocs) {
			this.userDB = userDB;
			this.numdocs = numdocs;
		}
		
		public MongoLoadConfigBuilder withHostname(String hostname) {
			this.hostname = hostname;
			return this;
		}
		
		public MongoLoadConfigBuilder withPort(int port) {
			this.port = port;
			return this;
		}
		
		public MongoLoadConfigBuilder withUsername(String username) {
			this.username = username;
			return this;
		}
		
		public MongoLoadConfigBuilder withPassword(String password) {
			this.password = password;
			return this;
		}
		
		public MongoLoadConfigBuilder withAuthDB(String authDB) {
			this.authDB = authDB;
			return this;
		}
		
		public MongoLoadConfigBuilder withSamplePath(String path) {
			this.samplePath = path;
			return this;
		}
		
		public MongoLoadConfigBuilder withEncoding(String encoding) {
			this.sampleEncoding = encoding;
			return this;
		}
		
		public MongoLoadConfig build() {
			return new MongoLoadConfig(this);
		}
		
	}
	
	public static final class MongoLoadConfigValidator {
		
		private static Logger logger = Logger.getLogger(MongoLoadConfigValidator.class);
		
		public static void validate(MongoLoadConfig config) {
			if ((config.getPort() == 0) || (config.getPort() > 65535)) {
				logger.error("Port invalid " + config.getPort());
				throw new IllegalArgumentException("Configured port number is invalid");
			}
			if (!"".equals(config.getUsername()) && ("".equals(config.getAuthDB()))) {
				throw new IllegalArgumentException("Username given but no authDB provided");
			}
			if ("".equals(config.getUserDB())) {
				throw new IllegalArgumentException("User DB name missing");
			}
			//TODO: Add validation for sample path
		}
	}
	
	//Config items
	private String hostname = "localhost";
	private int port;
	private String userDB;
	private String authDB;
	private String username;
	private String password;
	private int numdocs;
	private String samplePath;
	private String sampleEncoding;
	
	/**
	 * Constructor for the config object using Properties. Use either this or MongoLoadConfigBuilder to obtain config object
	 * 
	 * @param props The Properties to use
	 */
	public MongoLoadConfig(Properties props) {
		if (props.containsKey("hostname")) {
			this.hostname = (String)props.getProperty("hostname");
		}
		else {
			logger.info("No hostname supplied, assuming localhost");
		}
		if (props.containsKey("port")) {
			try {
				this.port = Integer.parseInt(props.getProperty("port"));
			}
			catch (NumberFormatException nfe) {
				logger.error("Error reading port number from properties file: " + nfe.getMessage());
				throw new IllegalArgumentException("Error reading port number from properties file", nfe);
			}
		}
		this.userDB = props.getProperty("userDB");
		this.authDB = props.getProperty("authDB");
		this.username = props.getProperty("username");
		this.password = props.getProperty("password");
		try {
			this.numdocs = Integer.parseInt((String)props.get("numdocs"));
		}
		catch (NumberFormatException nfe) {
			logger.error("Error reading number of documents from properties file: " + nfe.getMessage());
			throw new IllegalArgumentException("Error reading port number from properties file", nfe);
		}
		this.samplePath = props.getProperty("samplepath");
		this.sampleEncoding = props.getProperty("sampleencoding");
	}
	
	private MongoLoadConfig(MongoLoadConfigBuilder builder) {
		this.hostname = builder.hostname;
		this.port = builder.port;
		this.userDB = builder.userDB;
		this.authDB = builder.authDB;
		this.username = builder.username;
		this.password = builder.password;
		this.numdocs = builder.numdocs;
		this.samplePath = builder.samplePath;
		this.sampleEncoding = builder.sampleEncoding;
	}
	
	private String scrubbedString(String s) {
		return (s == null ? "" : s);
	}

	public String getHostname() {
		return scrubbedString(hostname);
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserDB() {
		return scrubbedString(userDB);
	}

	public void setUserDB(String userDB) {
		this.userDB = userDB;
	}

	public String getAuthDB() {
		return scrubbedString(authDB);
	}

	public void setAuthDB(String authDB) {
		this.authDB = authDB;
	}

	public String getUsername() {
		return scrubbedString(username);
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return scrubbedString(password);
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getNumdocs() {
		return numdocs;
	}

	public void setNumdocs(int numdocs) {
		this.numdocs = numdocs;
	};
	
	public String getSamplePath() {
		return samplePath;
	}
	
	public void setSamplePath(String path) {
		this.samplePath = path;
	}
	
	public String getSampleEncoding() {
		return this.sampleEncoding;
	}
	
	public void setSampleEncoding(String encoding) {
		this.sampleEncoding = encoding;
	}
}