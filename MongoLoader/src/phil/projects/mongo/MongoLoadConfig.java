package phil.projects.mongo;

import java.util.Properties;

/**
 * Config object for loaders. It stores the connection details for the Mongo instance and the number of documents to generate.
 * 
 * @author ptaprogg
 *
 */
public class MongoLoadConfig {

	/**
	 * Builder class for the config object.
	 * 
	 * @author ptaprogg
	 *
	 */
	public static class MongoLoadConfigBuilder {
		private String hostname;
		private int port;
		private String userDB;
		private String authDB;
		private String username;
		private String password;
		private int numdocs;
		
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
		
		public MongoLoadConfig build() {
			return new MongoLoadConfig(this);
		}
		
	}
	
	//Config items
	private String hostname;
	private int port;
	private String userDB;
	private String authDB;
	private String username;
	private String password;
	private int numdocs;
	
	/**
	 * Constructor for the config object using Properties. Use either this or MongoLoadConfigBuilder to obtain config object
	 * 
	 * @param props The Properties to use
	 */
	public MongoLoadConfig(Properties props) {
		this.hostname = (String)props.get("hostname");
		try {
			this.port = Integer.parseInt((String)props.get("port"));
		}
		catch (NumberFormatException nfe) {
			System.err.println("Error reading port from properties file");
			System.exit(2);
		}
		this.userDB = (String)props.get("userDB");
		this.authDB = (String)props.get("authDB");
		this.username = (String)props.get("username");
		this.password = (String)props.get("password");
		try {
			this.numdocs = Integer.parseInt((String)props.get("numdocs"));
		}
		catch (NumberFormatException nfe) {
			System.err.println("Error reading number of documents from properties file");
			System.exit(2);
		}
	}
	
	private MongoLoadConfig(MongoLoadConfigBuilder builder) {
		this.hostname = builder.hostname;
		this.port = builder.port;
		this.userDB = builder.userDB;
		this.authDB = builder.authDB;
		this.username = builder.username;
		this.password = builder.password;
		this.numdocs = builder.numdocs;
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
}