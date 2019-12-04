package OCARIoT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.mongodb.client.model.Filters.eq;



@Component
public class RabbitMQ {

    private static ResourceBundle rb = ResourceBundle.getBundle("application");

    String mongoHost = rb.getString("spring.data.mongodb");
    String mongoDatabase = rb.getString("spring.data.mongodb.database");
    String mongoCollection = rb.getString("spring.data.mongodb.collection");


    MongoClient mongoClient = MongoClients.create(mongoHost);
    MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
    MongoCollection<Document> collection = database.getCollection(mongoCollection);


    @RabbitListener(queues = "${rabbitmq.queue.send.notification}")
    public String notificationService(Message message) throws JsonProcessingException {

        System.out.println("Recieved Message From RabbitMQ: " + message);


        byte[] body = message.getBody();
        String jsonBody = new String(body);
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

    @RabbitListener(queues = "${rabbitmq.queue.delete.users}")
    public void deleteUsers(Message message) throws JsonProcessingException {

        byte[] body = message.getBody();
        JSONObject jsonmsg = new JSONObject(new String(body));
        String id = String.valueOf(jsonmsg.getJSONObject("user").get("id"));


        Document doc = collection.find(eq("id",id)).first();
        if (doc!=null){

            collection.deleteMany(doc);

        }

    }
}
