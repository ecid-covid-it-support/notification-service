package OCARIoT;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;



@Component
public class RabbitMQ {

    private static final Logger LOGGER = Logger.getLogger( RabbitMQ.class.getName() );

    private static ResourceBundle rb = ResourceBundle.getBundle("application");

    String mongoHost = rb.getString("spring.data.mongodb");
    String mongoDatabase = rb.getString("spring.data.mongodb.database");
    String mongoCollection = rb.getString("spring.data.mongodb.collection");


    MongoClient mongoClient = MongoClients.create(mongoHost);
    MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
    MongoCollection<Document> collection = database.getCollection(mongoCollection);


    @RabbitListener(queues = "${rabbitmq.queue.send.notification}")
    public void notificationService(Message message) throws JsonProcessingException {

        //LOGGER.log(Level.INFO,"Received message from RabbitMQ "+ message);

        byte[] body = message.getBody();
        JSONObject jsonmsg = new JSONObject(new String(body));

        if (jsonmsg.has("topic")){
            String topic = (String) jsonmsg.get("topic");
            String title = (String) jsonmsg.get("title");
            String content = (String) jsonmsg.get("body");
            try {
                FirebaseMessage.sendToTopic(topic, title, content);

            } catch (FirebaseMessagingException e) {
                e.printStackTrace();
                //return "Not possible to send message to topic " + topic +".";
            }
            //return "Notification sent to topic " + topic+".";
         }
        else{
            if (jsonmsg.has("id")){

                String title = (String) jsonmsg.get("title");
                String content = (String) jsonmsg.get("body");
                String userID = (String) jsonmsg.get("id");

                long found = collection.countDocuments(new BsonDocument("id", new BsonString(userID)));
                if (found==0){
                    //LOGGER.log(Level.INFO,"User does not exist in database ");
                    //return "User does not exist in database.";
                }
                else {

                    //LOGGER.log(Level.INFO,"Seatching for Tokens");
                    List<String> tokens = (List) collection.find(eq("id", userID)).first().get("Tokens");
                    //LOGGER.log(Level.INFO,"Tokens "+ tokens);

                    for (String token : tokens) {

                        try {

                            FirebaseMessage.sendToToken(token, title, content);


                        } catch (FirebaseMessagingException e) {
                            Bson filter = Filters.eq("id",userID);
                            Bson delete = Updates.pull("Tokens",token);
                            collection.updateOne(filter,delete);
                            //e.printStackTrace();

                        }
                    }
                    //return "Notification message sent to user " + userID;
                }
            }
        }
        //return  "Message must contain 'id' or 'topic' keys and values";
    }

    @RabbitListener(queues = "${rabbitmq.queue.delete.users}")
    public void deleteUsers(Message message) throws JsonProcessingException {

        byte[] body = message.getBody();
        JSONObject jsonmsg = new JSONObject(new String(body));
        //LOGGER.log(Level.INFO,"Received message from RabbitMQ " + message);
        String id = null;

        if (jsonmsg.has("user")) {
            id = String.valueOf(jsonmsg.getJSONObject("user").get("id"));


            Document doc = collection.find(eq("id", id)).first();
            if (doc != null) {

                collection.deleteMany(doc);
                //LOGGER.log(Level.INFO, "User " + id + " deleted from database");


            } else if (doc == null) {

                //LOGGER.log(Level.WARNING, "User " + id + " does not exist on database");

            }
        }
    }
}
