package OCARIoT;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;


@RestController
public class APIController {


    private static final ResourceBundle rb = ResourceBundle.getBundle("application");
    final String mongoDatabase = rb.getString("data.mongodb.database");
    final String mongoCollection = rb.getString("data.mongodb.collection");
    final String mongoURI = rb.getString("data.mongodb.uri");

    final MongoClient mongoClient = MongoClients.create(mongoURI);
    final MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
    final MongoCollection<Document> collection = database.getCollection(mongoCollection);




    @RequestMapping("/")
    public String index() {
        return "OCARIoT Notification Microservice";
    }

    @PostMapping("user/{id}")
    public String create(@PathVariable String id, @RequestBody Map <String,String> body) {

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
