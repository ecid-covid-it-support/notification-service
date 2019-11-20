package OCARIoT;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;


import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

    @RabbitListener(queues = "${sample.rabbitmq.queue}")
    public void recievedMessage(Message message) throws JsonProcessingException {
        //System.out.println("Recieved Message From RabbitMQ: " + message);
        byte[] body = message.getBody();
        String jsonBody = new String(body);
        System.out.println(jsonBody);
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
            }
         }
        else{
            if (map.containsKey("id")){
                String title = map.get("title");
                String content = map.get("body");
                String userID = map.get("id");

                Document myDoc = collection.find(eq("id",userID)).first();
                //System.out.println(myDoc);
                List<String> tokens = (List<String>) myDoc.get("Tokens");
                System.out.println(tokens);

                for (String token:tokens){

                    try {
                        System.out.println(token);
                        FirebaseMessage.sendToToken(token, title, content);
                    } catch (FirebaseMessagingException e) {
                        e.printStackTrace();
                    }


                }

            }
        }
    }
}
