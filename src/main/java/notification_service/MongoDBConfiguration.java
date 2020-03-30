package notification_service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
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

    @Value("${spring.mongodb.uri}")
    public String mongoURI;
    @Value("${spring.mongodb.database}")
    public String mongoDatabase;
    @Value("${spring.mongodb.collection}")
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
        MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        return database;
    }


    @Bean
    public MongoCollection<Document> collection(MongoDatabase database) {


        return database.getCollection("users");

    }


    @Bean
    public MongoCollection<Document> messagesCollection(MongoDatabase database) {

        return database.getCollection("messages");

    }

    @Bean
    void importMessages(MongoCollection<Document> messagesCollection){


        List<Document> documents = new ArrayList<Document>();
        try{

            JSONParser jsonParser = new JSONParser();
            FileReader reader = new FileReader(messagesPath);
            documents= (List<Document>) jsonParser.parse(reader);

            messagesCollection.deleteMany(new Document());
            messagesCollection.insertMany(documents);

        } catch (IOException | ParseException e) {
            LOGGER.log(Level.WARNING, "Could get Messages file");
        }
        

    }

    @Bean
    public MongoCollection<Document> pendingNotifications(MongoDatabase database) {


        return database.getCollection("pendingNotifications");

    }
}
