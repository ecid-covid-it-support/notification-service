package notification_service;


import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;



@RestController
public class APIController {

    @Autowired
    private  MongoCollection<Document> collection;

    @RequestMapping("/")
    public String index() {
        return "OCARIoT Notification Microservice";
    }

    @PostMapping("/v1/notifications/user/{id}")
    public String create(@PathVariable String id, @RequestBody Map <String,String> body) {

        String token = body.get("token");
        collection.updateOne(eq("id", id), new Document("$addToSet", new Document("Tokens",token)),new UpdateOptions().upsert(true));
        return "User saved";
    }

}
