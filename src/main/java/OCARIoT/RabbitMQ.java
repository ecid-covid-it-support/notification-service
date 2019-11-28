package OCARIoT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import static com.mongodb.client.model.Filters.eq;



@Component
public class RabbitMQ {

    MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    MongoDatabase database = mongoClient.getDatabase("NotificationMicroservice");
    MongoCollection<Document> collection = database.getCollection("Users");


    @RabbitListener(queues = "${rabbitmq.queue.Notification}")
    public String notificationService(Message message) throws JsonProcessingException {

        System.out.println("Recieved Message From RabbitMQ: " + message);


        byte[] body = message.getBody();
        String jsonBody = new String(body);
        //System.out.println(jsonBody);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(jsonBody, Map.class);

        if (map.containsKey("topic")){
            String topic = map.get("topic");
            String title = map.get("title");
            String content = map.get("body");
            try {
                FirebaseMessage.sendToTopic(topic, title, content);

            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
                return "Not possible to send message to topic " + topic +".";
            }
            return "Notification sent to topic " + topic+".";
         }
        else{
            if (map.containsKey("id")){

                String title = map.get("title");
                String content = map.get("body");
                String userID = map.get("id");

                long found = collection.countDocuments(new BsonDocument("code", new BsonString(userID)));
                if (found==0){

                    return "User does not exist in database.";

                }

                List<String> tokens = (List) collection.find(eq("id",userID)).first().get("Tokens");
                //System.out.println(tokens);
                //System.out.println(tokens.isEmpty());


                for (String token:tokens){

                    try {

                        FirebaseMessage.sendToToken(token, title, content);


                    } catch (FirebaseMessagingException e) {
                        e.printStackTrace();
                    }


                }

               return "Notification message sent to user " + userID;

            }
        }
        return  "Message must contain 'id' or 'topic' keys and values";
    }

    @RabbitListener(queues = "${rabbitmq.queue.Delete}")
    public String deleteUsers(Message message) throws JsonProcessingException {

        byte[] body = message.getBody();
        String jsonBody = new String(body);
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(jsonBody, Map.class);


        if (map.containsKey("id")) {
            String id = map.get("id");

            Document doc = collection.find(eq("id",id)).first(); //get first document
            if (doc==null){
                return "User " + id + " does not exist in the database.";

            }
            collection.deleteMany(doc);

            return "User " + id + " deleted";

        }
        return "Message does not contain key 'id'.";
    }
}
