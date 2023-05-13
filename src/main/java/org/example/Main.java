package org.example;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.InsertOneResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import static com.mongodb.client.model.Filters.eq;


// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {

    public static String base62EncodingTable = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.load();

        String testInputURL = "https://www.google.com/maps";
//        String inputURL = "https://www.youtube.com/";
        String inputURL = "https://facebook.com/";
        String uri = dotenv.get("MONGODB_URI");
        // Try to find long url in the database
        // If match then return the shortURL
        // If no match then generate a new shortURL and push to the database
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("short_urls");
            MongoCollection<Document> collection = database.getCollection("short_urls");
            Document doc = collection.find(eq("long_url", inputURL)).first();
            if (doc != null) {
                JSONObject obj = new JSONObject(doc.toJson());
                // Gets the shortURL from database
                System.out.println(obj.get("short_url"));
            } else {
                System.out.println("Pushing to database (not active yet)");
                long noOfDocuments = collection.countDocuments();
                int id = generateUniqueID((int) noOfDocuments);
                String shortUrl = encodeIDtoBase62(id);
                // IMPLEMENT A FUNCTION HERE TO PUSH SHORTENED URL TO DATABASE
                pushUrlToDatabase(id, shortUrl, inputURL, collection);
            }
        }
    }

    public static void pushUrlToDatabase(int id, String shortUrl, String longUrl, MongoCollection<Document> collection) {
        try {
            InsertOneResult result = collection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("id", id)
                    .append("short_url", shortUrl)
                    .append("long_url", longUrl));
            System.out.println("Success! Inserted document id: " + result.getInsertedId());
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public static String encodeIDtoBase62(int id) {
        // Base 62 encoding works by converting each character in the url to its ASCII number and then mapping it to the corresponding base62 character
        // First must generate unique id, then convert it to base62 7-digit number which will be the shortened url, finally store the id, shortURL and longURL in a database
        String stringID = Integer.toString(id);
        StringBuilder base62URL = new StringBuilder();
        while (id > 0) {
            int remainder = id % 62;
            base62URL.insert(0, base62EncodingTable.charAt(remainder));
            id = id / 62;
        }
        return String.valueOf(base62URL);
    }


    public static int generateUniqueID(int noOfDocuments) {
        //Generate a unique
        // This will be auto-incrementing, so get last id and add 1
        // for example there are 5000 entries in the database already
        return noOfDocuments + 5000 + 1;
    }

}
