package notification_service;


import com.google.api.client.json.Json;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
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

    @PostMapping(value = "/v1/notifications/user/{id}",produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<Object> create(@PathVariable String id, @RequestBody Map <String,String> body) {

        String token;
        String lang;
        String type;

        token = body.get("token");
        lang = body.get("lang");
        type = body.get("type");
        ArrayList<String> emptyArrayTokens = new ArrayList<>();

        JSONObject jo = new JSONObject();


        if (lang==null || lang.isEmpty()){

            jo.put("code", 400);
            jo.put("message", "MISSING PARAMETERS");
            jo.put("description", "No lang found in the message body");
            jo.put("redirect_link", "/user/{id}");
            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.BAD_REQUEST);
        }
        if (!lang.equals("en")&&!lang.equals("es")&&!lang.equals("pt")&&!lang.equals("el")){

            jo.put("code", 400);
            jo.put("message", "WRONG PARAMETERS");
            jo.put("description", "Only English(en), Spanish(es), Portuguese(pt), Greek(el) languages supported");
            jo.put("redirect_link", "/user/{id}");
            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.BAD_REQUEST);
        }
        if (type==null||type.isEmpty()){

            jo.put("code", 400);
            jo.put("message", "MISSING PARAMETERS");
            jo.put("description", "Type of user not defined");
            jo.put("redirect_link", "/user/{id}");
            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.BAD_REQUEST);
        }
        if (!type.equals("children")&&!type.equals("family")&&!type.equals("educator")){

            jo.put("code", 400);
            jo.put("message", "WRONG PARAMETERS");
            jo.put("description", "Type of user can only be one of children, family or educator");
            jo.put("redirect_link", "/user/{id}");
            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.BAD_REQUEST);
        }
        if (token!=null && !token.isEmpty()){

            collection.updateOne(eq("id", id), new Document("$addToSet", new Document("tokens", token)), new UpdateOptions().upsert(true));

        }

        collection.updateOne(eq("id", id), new Document("$set", new Document("lang", lang)),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("type", type)),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("lastLogin", new Date())),new UpdateOptions().upsert(true));
        collection.updateOne(eq("id", id), new Document("$set", new Document("lastNotification", new Date())),new UpdateOptions().upsert(true));

        Document myDoc = collection.find(eq("id", id)).first();
        return new ResponseEntity<Object>(myDoc, HttpStatus.OK);

    }

    @PatchMapping(value = "/v1/notifications/deletetoken/{id}",produces = MediaType.APPLICATION_JSON_VALUE )
    public ResponseEntity<Object> deleteToken(@PathVariable String id, @RequestBody Map <String,String> body){

        JSONObject jo = new JSONObject();

        String token = body.get("token");


        if (token==null || token.isEmpty()){


            jo.put("code", 400);
            jo.put("message", "MISSING PARAMETERS");
            jo.put("description", "No token found in the message body");
            jo.put("redirect_link", "/deletetoken/{id}");
            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.BAD_REQUEST);
        }

        long found = collection.countDocuments(new BsonDocument("id", new BsonString(id)));
        if (found == 0) {


            jo.put("code", 200);
            jo.put("message", "OK");
            jo.put("description", "User not found");
            jo.put("redirect_link", "/deletetoken/{id}");
            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.OK);

        } else {

            Document filter = new Document("id",id);
            Document update = new Document("$pull", new Document("tokens", token));
            collection.updateOne(filter, update);
            Document myDoc = collection.find(filter).first();
            jo = new JSONObject(myDoc);

            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.OK);

        }

    }

    @GetMapping(value = "/v1/notifications/pendingnotification/{id}",produces = MediaType.APPLICATION_JSON_VALUE)
    public HttpEntity<Object> pendingNotification(@PathVariable String id) {

        JSONObject jo = new JSONObject();
        Collection<JSONObject> items = new ArrayList<>();
        JSONObject response = new JSONObject();
        JSONObject item = new JSONObject();

        long found = pendingNotifications.countDocuments(new BsonDocument("id", new BsonString(id)));
        if (found == 0) {

            response.put("id", id);
            response.put("notifications", (Collection<?>) null);

            return new ResponseEntity<Object>(response.toMap(), HttpStatus.OK);

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


            return new ResponseEntity<Object>(jo.toMap(), HttpStatus.OK);
        }
    }

}
