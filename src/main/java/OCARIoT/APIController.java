package OCARIoT;


import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Objects;



@RestController
public class APIController {

    @Autowired
    private  MongoCollection<Document> collection;

    @RequestMapping("/")
    public String index() {
        return "OCARIoT Notification Microservice";
    }

    @PostMapping("user/{id}")
    public String create(@PathVariable String id, @RequestBody Map <String,String> body) {

        String token = body.get("token");
        collection.updateOne(eq("id", id), new Document("$addToSet", new Document("Tokens",token)),new UpdateOptions().upsert(true).bypassDocumentValidation(true));
        return "User saved";
    }

    @GetMapping("/user/{id}")
    public String show(@PathVariable String id){

        Document myDoc = collection.find(eq("id",id)).first();
        return Objects.requireNonNull(myDoc).toJson();
    }

}
