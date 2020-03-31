package notification_service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public MongoCollection<Document> collection() {

        MongoClient mongoClient = MongoClients.create(mongoURI+"&sslInvalidHostNameAllowed=true");
        MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        return database.getCollection("users");

    }


    @Bean
    public MongoCollection<Document> messagesCollection() {

        MongoClient mongoClient = MongoClients.create(mongoURI+"&sslInvalidHostNameAllowed=true");
        MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        return database.getCollection("messages");

    }

    @Bean
    public MongoCollection<Document> pendingNotifications() {

        MongoClient mongoClient = MongoClients.create(mongoURI+"&sslInvalidHostNameAllowed=true");
        MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        return database.getCollection("pendingNotifications");

    }
}
