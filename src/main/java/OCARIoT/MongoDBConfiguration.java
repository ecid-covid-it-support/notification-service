package OCARIoT;




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



    @Bean
    public MongoCollection<Document> collection() {
        System.setProperty ("javax.net.ssl.keyStore",keystorePath);
        System.setProperty ("javax.net.ssl.keyStorePassword",keystorePass);
        MongoClient mongoClient = MongoClients.create(mongoURI);
        MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        return database.getCollection(mongoCollection);
    }
}
