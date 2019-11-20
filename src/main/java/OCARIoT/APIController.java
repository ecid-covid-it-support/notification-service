package OCARIoT;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.gson.JsonObject;
import com.mongodb.client.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import static com.mongodb.client.model.Filters.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import static java.lang.System.out;


@RestController
public class APIController {




    /*UserMockedData userMockedData = UserMockedData.getInstance();

    @Value("${spring.data.mongodb.uri}")
            private String host;*/




    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("NotificationMicroservice");
    MongoCollection<Document> collection = database.getCollection("Users");

   /* public APIController() throws UnknownHostException {
    }*/


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

    /*@PostMapping("notification/topic")
    public JsonObject topic(@RequestBody Map<String, String> body) throws IOException, FirebaseMessagingException {

        String topic = body.get("topic");
        String title = body.get("title");
        String content = body.get("body");
        FirebaseMessage.sendToTopic(topic, title, content);
        return null;

    }*/

    /*@PostMapping("notification/{id}")
    public JsonObject token(@PathVariable String id, @RequestBody Map<String, String> body) throws IOException, FirebaseMessagingException {

        int userID = Integer.parseInt(id);

        String title = body.get("title");
        String content = body.get("body");
        String token = userMockedData.getTokenById(userID);

        FirebaseMessage.sendToToken(token, title, content);
        return null;
    }*/
}
