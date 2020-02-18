package notification_service;


import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
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
    public ResponseEntity<String> create(@PathVariable String id, @RequestBody Map <String,String> body) {

                String token = body.get("token");
                String lang = body.get("lang");
                if (token==null || token.isEmpty()){

                   return ResponseEntity.status(400).body("No token found in the message body");
                }
                if (lang==null || lang.isEmpty()){

                    return ResponseEntity.status(400).body("No lang found in the message body");
                }
                if (!lang.equals("en")&&!lang.equals("es")&&!lang.equals("pt")&&!lang.equals("el")){

                    return ResponseEntity.status(400).body("Only English(en), Spanish(es), Portuguese(pt), Greek(el) languages supported");
                }
                collection.updateOne(eq("id", id), new Document("$addToSet", new Document("Tokens", token)), new UpdateOptions().upsert(true));
                BasicDBObject updateFields = new BasicDBObject();
                updateFields.append("lang", lang).append("ts", new Date());
                BasicDBObject setQuery = new BasicDBObject();
                setQuery.append("$set", updateFields);
                collection.updateOne(eq("id", id), setQuery);
                return ResponseEntity.status(200).body("User saved or updated");

    }

}
