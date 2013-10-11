package phil.projects.mongo;

import com.mongodb.DBObject;

public interface MongoDBObjectFactory {
	
	public DBObject generateDocument();
	
}
