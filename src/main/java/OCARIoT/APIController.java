package OCARIoT;

import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import static com.mongodb.client.model.Filters.*;
import org.springframework.web.bind.annotation.*;
import java.net.UnknownHostException;
import java.util.Map;

@RestController
public class APIController {

    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("NotificationMicroservice");
    MongoCollection<Document> collection = database.getCollection("Users");


    @RequestMapping("/")
    public String index() {
        return "OCARIoT Notification Microservice";
    }

    @PostMapping("user/{id}")
    public String create(@PathVariable String id, @RequestBody Map<String, String> body) throws UnknownHostException {


        String token = body.get("token");

        collection.updateOne(eq("id", id), new Document("$addToSet", new Document("Tokens",token)),new UpdateOptions().upsert(true).bypassDocumentValidation(true));
        Document myDoc = collection.find(eq("id",id)).first();
        return myDoc.toJson();


    }

    @GetMapping("/user/{id}")
    public String show(@PathVariable String id){

        Document myDoc = collection.find(eq("id",id)).first();
        return myDoc.toJson();
    }

}
