package notification_service;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;



@RestController
public class APIController {

    @Autowired
    private  MongoCollection<Document> collection;

    @Autowired
    private  MongoCollection<Document> pendingNotifications;

    @RequestMapping("/")
    public String index() {
        return "OCARIoT Notification Microservice";
    }

    @PostMapping("/v1/notifications/user/{id}")
    public ResponseEntity<String> create(@PathVariable String id, @RequestBody Map <String,String> body) {

        String token=null;
        String lang = null;


        token = body.get("token");
        lang = body.get("lang");

        if (lang==null || lang.isEmpty()){

            return ResponseEntity.status(400).body("No lang found in the message body");
        }
        if (!lang.equals("en")&&!lang.equals("es")&&!lang.equals("pt")&&!lang.equals("el")){

            return ResponseEntity.status(400).body("Only English(en), Spanish(es), Portuguese(pt), Greek(el) languages supported");
        }
        if (token!=null && !token.isEmpty()){

            collection.updateOne(eq("id", id), new Document("$addToSet", new Document("tokens", token)), new UpdateOptions().upsert(true));

        }

        collection.updateOne(eq("id", id), new Document("$set", new Document("lang", lang)),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("lastLogin", new Date())),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("lastNotification", new Date())),new UpdateOptions().upsert(true));
        return ResponseEntity.status(200).body("User saved or updated");

    }

    @DeleteMapping("/v1/notifications/deletetoken/{id}")
    public ResponseEntity<String> deleteToken(@PathVariable String id, @RequestBody Map <String,String> body){

        String token = body.get("token");
        if (token==null || token.isEmpty()){

            return ResponseEntity.status(400).body("No token found in the message body");
        }

        long found = collection.countDocuments(new BsonDocument("id", new BsonString(id)));
        if (found == 0) {

            return ResponseEntity.status(200).body("User not found");

        } else {

                Document filter = new Document("id",id);
                Document update = new Document("$pull", new Document("Tokens", token));
                collection.updateOne(filter, update);
                return ResponseEntity.status(200).body("Token deleted");
        }

    }

    @GetMapping("/v1/notifications/pendingnotification/{id}")
    public ResponseEntity<JSONObject> pendingNotification(@PathVariable String id) {

        FindIterable<Document> iterable = pendingNotifications.find();
        JSONObject jo = new JSONObject();
        Collection<JSONObject> items = new ArrayList<JSONObject>();
        JSONObject response = new JSONObject();
        JSONObject item = new JSONObject();

        long found = pendingNotifications.countDocuments(new BsonDocument("id", new BsonString(id)));
        if (found == 0) {

            return ResponseEntity.status(200).body(response);

        } else{

            FindIterable<Document> docs = pendingNotifications.find(eq("id", id));

            for (Document doc : docs){

                item=null;
                item.put("title", doc.get("title"));
                item.put("body", doc.get("body"));
                item.put("timestamp", doc.get("timestamp"));
                items.add(item);
                pendingNotifications.findOneAndDelete(doc);

            }
            jo.put("id", id);
            jo.put("notifications", items);
            System.out.println(jo);

            return ResponseEntity.status(200).body(jo);
        }
    }

}
