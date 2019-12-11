package OCARIoT;

import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import static com.mongodb.client.model.Filters.*;
import org.springframework.web.bind.annotation.*;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;


@RestController
public class APIController {

    private static final Logger LOGGER = Logger.getLogger( RabbitMQ.class.getName() );

    private static ResourceBundle rb = ResourceBundle.getBundle("application");
    String mongoHost = rb.getString("spring.data.mongodb");
    String mongoDatabase = rb.getString("spring.data.mongodb.database");
    String mongoCollection = rb.getString("spring.data.mongodb.collection");


    MongoClient mongoClient = MongoClients.create(mongoHost);
    MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
    MongoCollection<Document> collection = database.getCollection(mongoCollection);


    @RequestMapping("/")
    public String index() {
        return "OCARIoT Notification Microservice";
    }

    @PostMapping("user/{id}")
    public String create(@PathVariable String id, @RequestBody Map <String,String> body) throws UnknownHostException {

        String token = body.get("token");
        collection.updateOne(eq("id", id), new Document("$addToSet", new Document("Tokens",token)),new UpdateOptions().upsert(true).bypassDocumentValidation(true));
        Document myDoc = collection.find(eq("id",id)).first();
        return "User saved";
    }

    @GetMapping("/user/{id}")
    public String show(@PathVariable String id){

        Document myDoc = collection.find(eq("id",id)).first();
        return myDoc.toJson();
    }

}
