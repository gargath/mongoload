package phil.projects.mongo;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoInvoiceFactory implements MongoDBObjectFactory {

	Logger logger = Logger.getLogger(MongoInvoiceFactory.class);
	
	@Override
	public DBObject generateDocument() {
		//Get the RandomGenerator
		RandomGenerator rand = RandomGenerator.getInstance();
		
		//The invoice number this invoice will use
		String invoiceno = rand.getRandomUniqueString(6);
		logger.debug("Generating invoice with number " + invoiceno);
		//The top-level document
		BasicDBObject invoice = new BasicDBObject("invoice number", invoiceno);
		//The line-item document to be added to this invoice
		BasicDBObject items = new BasicDBObject();
		//all sub-documents should be marked as partial to prevent accidental upserts
		items.markAsPartialObject();
		int numberOfItems = rand.getRandomIntInRange(14)+1;
		logger.debug("Invoice " + invoiceno + " will have " + numberOfItems + " items");

		//Will store total amount
		int invoiceAmount = 0;
		//Generate items loop
		for (int i = 0; i < numberOfItems; i++) {
			//Sub-document to hold amount and price for this item
			BasicDBObject itemDetails = new BasicDBObject();
			itemDetails.markAsPartialObject();

			//Get random number for amount and unit price, then calculate line price
			int amount = rand.getRandomIntInRange(20)+1;
			int unitprice = rand.getRandomIntInRange(100)+1;
			int price = amount*unitprice;
			
			//Append values to details document then append that to parent
			itemDetails.append("amount", amount);
			itemDetails.append("unit price", unitprice);
			itemDetails.append("price", price);
			items.append(rand.getRandomString(5), itemDetails);
			
			//Update total amount
			invoiceAmount += price;
			logger.debug("Generated lineitem: " + itemDetails.toString());
		}
		//Append item, total price and total number of items to invoice document
		invoice.append("items", items);
		invoice.append("total number of items", numberOfItems);
		invoice.append("total price", invoiceAmount);
		
		logger.debug("Invoice generation complete for number " + invoiceno);
		logger.trace("Generated invoice: " + invoice.toString());
		
		return invoice;
	}

}
