package notification_service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.mongodb.client.model.Filters.eq;


@Service
public class RabbitMQ{

    private static final Logger LOGGER = Logger.getLogger( RabbitMQ.class.getName() );

    @Autowired
    private MongoCollection<Document> collection;

    @Autowired
    private MongoCollection<Document> messagesCollection;

    @Autowired
    private MongoCollection<Document> pendingNotifications;

    final ApplicationContext appCtx = ApplicationContextUtils.getApplicationContext();
    final FirebaseMessage firebaseMessage =  appCtx.getBean(FirebaseMessage.class);
    RabbitMQRequester rabbitMQRequester = appCtx.getBean(RabbitMQRequester.class);





    @RabbitListener(queues = "${rabbitmq.queue.send.notification}")
    public void notificationService(Message message) throws IOException {


        try{
            byte[] body = message.getBody();
            JSONObject jsonmsg = new JSONObject(new String(body));

            if (jsonmsg.has("event_name")) {

                String eventName = (String) jsonmsg.get("event_name");



                switch (eventName){

                    case "SendNotificationEvent":

                        if (jsonmsg.has("notification_type")) {

                            String messageType = jsonmsg.getString("notification_type");
                            System.out.println(jsonmsg);

                            switch (messageType){

                                case "topic":

                                    try {
                                        String topic = jsonmsg.getString("topic");
                                        String title = jsonmsg.getString("title");
                                        String bod = jsonmsg.getString("body");
                                        firebaseMessage.sendToTopic(topic, title, bod);
                                    } catch (JSONException e) {
                                        LOGGER.log(Level.WARNING, "Could not read topic,title or body from json message");
                                    } catch (FirebaseMessagingException e) {
                                        LOGGER.log(Level.WARNING, "Could not send topic notification.");
                                    }

                                    break;

                                case "mission:new":

                                case "mission:done":


                                    try{
                                        String uID = jsonmsg.getString("id");
                                        firebaseMessage.sendToToken(uID,messageType,null, 0);


                                    } catch (JSONException e) {
                                        LOGGER.log(Level.WARNING, "Could not read id from json message");
                                    } catch (FirebaseMessagingException e) {
                                        LOGGER.log(Level.WARNING, "Could not send notification "+messageType);
                                    }
                                    break;

                                case "monitoring:miss_child_data":

                                    try {

                                        String username = null;
                                        String familyID = null;

                                        String uID = jsonmsg.getString("id");
                                        int days_since = jsonmsg.getInt("days_since");

                                        String info = rabbitMQRequester.send("_id="+uID,"children.find");
                                        JSONArray jsonarray = new JSONArray(info);
                                        try {
                                            username = (String) jsonarray.getJSONObject(0).get("username");

                                        } catch (JSONException e) {

                                        }

                                        String family = rabbitMQRequester.send("?children="+uID,"families.find");
                                        jsonarray = new JSONArray(family);
                                        try {
                                            familyID = (String) jsonarray.getJSONObject(0).get("id");
                                            if (familyID!=null && !familyID.isEmpty()) {
                                                firebaseMessage.sendToToken(familyID, messageType, username, days_since);
                                            }

                                        } catch (JSONException e) {

                                        }



                                    } catch (JSONException e) {
                                        LOGGER.log(Level.WARNING, "Could not get id or days_since from json message.");
                                    } catch (FirebaseMessagingException e) {
                                        LOGGER.log(Level.WARNING, "Error sending notification.");
                                    }


                                    break;




                            }


                        }
                        break;

                    case "UserDeleteEvent":

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
                        break;
                }

            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "An error occurred while attempting to read message. Possible problem with JSON format");
        }

    }
}
