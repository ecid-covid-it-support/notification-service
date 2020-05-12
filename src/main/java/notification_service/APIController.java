package notification_service;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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

import static com.mongodb.client.model.Filters.eq;





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

        String token;
        String lang;
        String type;

        token = body.get("token");
        lang = body.get("lang");
        type = body.get("type");
        ArrayList<String> emptyArrayTokens = new ArrayList<>();

        Document jo = new Document();


        if (lang==null || lang.isEmpty()){

            jo.put("code", 400);
            jo.put("message", "MISSING PARAMETERS");
            jo.put("description", "No lang found in the message body");
            jo.put("redirect_link", "/user/{id}");
            return ResponseEntity.status(400).body(jo.toString());
        }
        if (!lang.equals("en")&&!lang.equals("es")&&!lang.equals("pt")&&!lang.equals("el")){

            jo.put("code", 400);
            jo.put("message", "WRONG PARAMETERS");
            jo.put("description", "Only English(en), Spanish(es), Portuguese(pt), Greek(el) languages supported");
            jo.put("redirect_link", "/user/{id}");
            return ResponseEntity.status(400).body(jo.toString());
        }
        if (type==null||type.isEmpty()){

            jo.put("code", 400);
            jo.put("message", "MISSING PARAMETERS");
            jo.put("description", "Type of user not defined");
            jo.put("redirect_link", "/user/{id}");
            return ResponseEntity.status(400).body(jo.toString());
        }
        if (!type.equals("children")&&!type.equals("family")&&!type.equals("educator")){

            jo.put("code", 400);
            jo.put("message", "WRONG PARAMETERS");
            jo.put("description", "Type of user can only be one of children, family or educator");
            jo.put("redirect_link", "/user/{id}");
            return ResponseEntity.status(400).body(jo.toString());
        }
        if (token!=null && !token.isEmpty()){

            collection.updateOne(eq("id", id), new Document("$addToSet", new Document("tokens", token)), new UpdateOptions().upsert(true));

        }

        collection.updateOne(eq("id", id), new Document("$set", new Document("lang", lang)),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("type", type)),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("lastLogin", new Date())),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("lastNotification", new Date())),new UpdateOptions().upsert(true));

        Document doc = (Document) collection.find(eq("id",id)).first().remove("_id");
        return ResponseEntity.status(200).body(doc.toString());

    }

    @PatchMapping("/v1/notifications/deletetoken/{id}")
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
                Document update = new Document("$pull", new Document("tokens", token));
                collection.updateOne(filter, update);
                return ResponseEntity.status(200).body("Token deleted");
        }

    }

    @GetMapping("/v1/notifications/pendingnotification/{id}")
    public ResponseEntity<String> pendingNotification(@PathVariable String id) {

        JSONObject jo = new JSONObject();
        Collection<JSONObject> items = new ArrayList<>();
        JSONObject response = new JSONObject();
        JSONObject item = new JSONObject();

        long found = pendingNotifications.countDocuments(new BsonDocument("id", new BsonString(id)));
        if (found == 0) {

            response.put("id", id);
            response.put("notifications", (Collection<?>) null);
            System.out.println(response);
            return ResponseEntity.status(200).body(response.toString());

        } else{

            FindIterable<Document> docs = pendingNotifications.find(eq("id", id));

            for (Document doc : docs){

                item.put("title", doc.get("title"));
                item.put("body", doc.get("body"));
                item.put("timestamp", doc.get("timestamp"));
                items.add(item);
                pendingNotifications.findOneAndDelete(doc);

            }
            jo.put("id", id);
            jo.put("notifications", items);


            return ResponseEntity.status(200).body(jo.toString());
        }
    }

}
