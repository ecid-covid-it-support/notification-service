package OCARIoT;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.mongodb.client.model.Filters.eq;


@Service
public class RabbitMQ{

    private static final Logger LOGGER = Logger.getLogger( RabbitMQ.class.getName() );


    @Autowired
    private  MongoCollection<Document> collection;

    @RabbitListener(queues = "${rabbitmq.queue.send.notification}")
    public void notificationService(Message message) throws IOException {


        try {
            byte[] body = message.getBody();
            JSONObject jsonmsg = new JSONObject(new String(body));

            if (jsonmsg.has("event_name")) {

                String eventName = (String) jsonmsg.get("event_name");

                if (jsonmsg.get("event_name").equals("SendNotificationEvent")) {

                    if (jsonmsg.has("notification")) {

                        if (jsonmsg.getJSONObject("notification").has("topic")) {


                            try {
                                String topic = String.valueOf(jsonmsg.getJSONObject("notification").get("topic"));
                                String title = String.valueOf(jsonmsg.getJSONObject("notification").get("title"));
                                String content = String.valueOf(jsonmsg.getJSONObject("notification").get("body"));
                                FirebaseMessage.sendToTopic(topic, title, content);

                            } catch (FirebaseMessagingException e) {
                                LOGGER.log(Level.WARNING, "An error occurred while attempting perform the operation with the SendNotificationEvent name event. " +
                                        "Cannot read property 'topic', 'body' or 'title'");

                            }
                        }
                        if (jsonmsg.getJSONObject("notification").has("id")) {


                            try {
                                String title = String.valueOf(jsonmsg.getJSONObject("notification").get("title"));
                                String content = String.valueOf(jsonmsg.getJSONObject("notification").get("body"));
                                String userID = String.valueOf(jsonmsg.getJSONObject("notification").get("id"));


                                long found = collection.countDocuments(new BsonDocument("id", new BsonString(userID)));
                                if (found == 0) {

                                    LOGGER.log(Level.INFO, "User does not exist in database ");

                                } else {

                                    List<String> tokens = (List<String>) (Objects.requireNonNull(collection.find(eq("id", userID)).first())).get("Tokens");

                                    for (String token : tokens) {

                                        try {

                                            FirebaseMessage.sendToToken(token, title, content);

                                        } catch (FirebaseMessagingException e) {

                                            Bson filter = Filters.eq("id", userID);
                                            Bson delete = Updates.pull("Tokens", token);
                                            collection.updateOne(filter, delete);


                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                LOGGER.log(Level.WARNING, "An error occurred while attempting perform the operation with the SendNotificationEvent name event. " +
                                        "Cannot read property 'id', 'body' or 'title' or user does not exist in database");
                            }
                        }
                    }
                }
                if (jsonmsg.get("event_name").equals("UserDeleteEvent")) {

                    String id;

                    if (jsonmsg.has("user")) {
                        try {

                            id = String.valueOf(jsonmsg.getJSONObject("user").get("id"));


                            Document doc = collection.find(eq("id", id)).first();
                            if (doc != null) {

                                collection.deleteMany(doc);
                                LOGGER.log(Level.INFO, "User " + id + " deleted from database");


                            } else {

                                LOGGER.log(Level.WARNING, "User " + id + " does not exist on database");

                            }
                        } catch (JSONException e) {
                            LOGGER.log(Level.WARNING, "An error occurred while attempting perform the operation with the UserDeleteEvent name event. Cannot read property 'id' or undefined");
                        }
                    }
                }

            }
        } catch (JSONException e) {
            LOGGER.log(Level.WARNING, "An error occurred while attempting to read message. Possible problem with JSON format");
        }
    }
}
