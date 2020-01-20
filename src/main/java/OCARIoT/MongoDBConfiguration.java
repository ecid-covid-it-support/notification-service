package OCARIoT;


import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MongoDBConfiguration{

    @Value("${spring.data.mongodb.uri}")
    public String mongoURI;
    @Value("${spring.data.mongodb.database}")
    public String mongoDatabase;
    @Value("${spring.data.mongodb.collection}")
    public String mongoCollection;
    @Value("${server.ssl.key-store}")
    public String keystorePath;
    @Value("${server.ssl.key-store-password}")
    public String keystorePass;
    @Value("${server.ssl.trust-store-password}")
    public String truststorePath;


    @Bean
    public  MongoClientOptions mongoClientOptions(){
        System.setProperty ("javax.net.ssl.keyStore",keystorePath);
        System.setProperty ("javax.net.ssl.keyStorePassword",keystorePass);
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        MongoClientOptions options=builder.sslEnabled(true).build();
        return options;
    }

    public @Bean MongoClient mongoClient() throws Exception {
        MongoClient mongoClient = MongoClients.create(mongoURI);
        return mongoClient;
    }

    @Bean
    public MongoCollection<Document> collection(MongoClient mongoClient) {

        //final MongoClient mongoClient = MongoClients.create(mongoURI);
        final MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        return database.getCollection(mongoCollection);
    }
}
