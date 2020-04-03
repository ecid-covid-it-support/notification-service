package notification_service;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@Configuration
public class MongoDBConfiguration{

    private static final Logger LOGGER = Logger.getLogger( MongoDBConfiguration.class.getName());

    @Value("${mongodb.uri}")
    public String mongoURI;
    @Value("${mongodb.database}")
    public String mongoDatabase;
    @Value("${mongodb.collection}")
    public String mongoCollection;
    @Value("${server.ssl.key-store}")
    public String keystorePath;
    @Value("${server.ssl.key-store-password}")
    public String keystorePass;
    @Value("${server.ssl.key-truststore}")
    public String truststorePath;
    @Value("${preset.messages}")
    public String messagesPath;


    @Bean
    public MongoDatabase database() {

        System.setProperty ("javax.net.ssl.keyStore",keystorePath);
        System.setProperty ("javax.net.ssl.keyStorePassword",keystorePass);
        System.setProperty ("javax.net.ssl.trustStore",truststorePath);
        System.setProperty ("javax.net.ssl.trustStorePassword","changeit");
        MongoClient mongoClient = MongoClients.create(mongoURI+"&sslInvalidHostNameAllowed=true");
        return mongoClient.getDatabase(mongoDatabase);
    }


    @Bean
    public MongoCollection<Document> collection(MongoDatabase database) {


        return database.getCollection("users");

    }


    @Bean
    public MongoCollection<Document> messagesCollection(MongoDatabase database) {

        MongoCollection<Document> messages = database.getCollection("messages");

        try{
            List<JSONObject> documents = new ArrayList<>();
            int i;
            messages.deleteMany(new Document());
            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader(messagesPath);
            Object obj = jsonParser.parse(reader);
            String stringJson = obj.toString();
            JSONArray jsonArray = new JSONArray(stringJson);
            for(i=0;i<jsonArray.length();i++){

                Document doc = Document.parse(jsonArray.get(i).toString());
                messages.insertOne(doc);
            }

        } catch (IOException | ParseException e) {
            LOGGER.log(Level.WARNING, "Could get Messages file");
        }

        return messages;

    }

    @Bean
    public MongoCollection<Document> pendingNotifications(MongoDatabase database) {


        return database.getCollection("pendingNotifications");

    }
}
