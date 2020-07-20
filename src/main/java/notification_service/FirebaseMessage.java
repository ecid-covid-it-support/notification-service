package notification_service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.mongodb.client.MongoCollection;
import com.vdurmont.emoji.EmojiParser;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.client.model.Filters.eq;

@Component
public class FirebaseMessage {

    private static final Logger LOGGER = Logger.getLogger( RabbitMQ.class.getName());

    @Autowired
    private MongoCollection<Document> collection;

    @Autowired
    private MongoCollection<Document> messagesCollection;

    @Autowired
    private MongoCollection<Document> pendingNotifications;


    private String title;
    private String body;
    Document userDoc;
    Document messageDoc;
    String lang;

    @Autowired
    RabbitMQRequester rabbitMQRequester;

    private List<String> getTokens(String userID){


        return (List<String>) Objects.requireNonNull(collection.find(eq("id", userID)).first()).get("tokens");

    }

    private Document getMessage(String messageType, String lang){

        messageDoc = messagesCollection.find(eq("message_type", messageType)).first();
        Document message = null;

        try {
            assert messageDoc != null;
            message = (Document) messageDoc.get(lang);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not get retrieve message format from database");
        }

        return message;

    }

    public Boolean sendToToken(String userID, String title, String body){

        title = EmojiParser.parseToUnicode(title);
        body = EmojiParser.parseToUnicode(body);
        boolean createPendingNotification = true;


        List<String> tokens = getTokens(userID);

        for (String token : tokens) {

            try {

                Message message = Message.builder()
                        .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                        .setToken(token)
                        .build();

                FirebaseMessaging.getInstance().send(message);
                // Send a message to the device corresponding to the provided registration token.

                createPendingNotification = false;

            } catch (FirebaseMessagingException e) {

                if (e.getErrorCode().equals("invalid-argument") || e.getErrorCode().equals("invalid-registration-token")|| e.getErrorCode().equals("registration-token-not-registered")){
                    Document filter = new Document("id",userID);
                    Document update = new Document("$pull", new Document("tokens", token));
                    collection.updateOne(filter, update);
                }


                LOGGER.log(Level.WARNING, "Error sending notification to token");
            }
        }

        return createPendingNotification;
    }

    public void sendToToken(String institutionID,String messageType, String sensorType, JSONObject location, int days_since) {

        if ("iot:miss_data".equals(messageType)) {

            int i;
            String local = null;
            String room = null;
            ArrayList<String> arrayEducators = new ArrayList<>();

            try {
                local = location.getString("local");
                room = location.getString("room");
            } catch (JSONException e) {
                LOGGER.log(Level.WARNING, "Could not get local or room.");
            }

            String allteachers = rabbitMQRequester.send("?institution=" + institutionID, "educators.find");
            

            JSONArray jsonarray = new JSONArray(allteachers);


            for (i = 0; i < jsonarray.length(); i++) {


                String educatorID = (String) jsonarray.getJSONObject(i).get("id");
                arrayEducators.add(educatorID);
                

            }

            for (i = 0; i < arrayEducators.size(); i++) {

                String educatorID = arrayEducators.get(i);
                userDoc = collection.find(eq("id", educatorID)).first();


                if (userDoc != null) {

                    lang = userDoc.getString("lang");

                    messageDoc = getMessage(messageType, lang);
                    title = messageDoc.getString("title");
                    body = messageDoc.getString("body");

                    Boolean createPendingNotification = sendToToken(educatorID, title, String.format(body, sensorType, local, room, days_since));


                    if (createPendingNotification){
                        Document doc = new Document();
                        doc.put("id", educatorID);
                        doc.put("title", title);
                        doc.put("body", String.format(body, sensorType, local, room, days_since));
                        doc.put("timestamp", new Date());
                        doc.put("message_type", messageType);
                        pendingNotifications.insertOne(doc);
                    }


                }
            }
        }
    }


    public void sendToToken(String userID, String messageType, String username, int days_since) {

        switch (messageType){

            case "notification:child":

            case "notification:family":

            case "notification:educator":

                userDoc = collection.find(eq("id", userID)).first();

                if (userDoc != null) {
                    lang = userDoc.getString("lang");

                    try {
                        messageDoc = getMessage(messageType, lang);
                        title = messageDoc.getString("title");
                        body = messageDoc.getString("body");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Could not retrieve message format from database");
                    }


                    Boolean createPendingNotification=sendToToken(userID, title, String.format(body, days_since));
                    collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification", new Date())));

                    if (createPendingNotification){
                        if (title != null && body != null) {
                            Document doc = new Document();
                            doc.put("id", userDoc.get("id"));
                            doc.put("title", title);
                            doc.put("body", String.format(body, days_since));
                            doc.put("timestamp", new Date());
                            doc.put("message_type", messageType);
                            pendingNotifications.insertOne(doc);
                            collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification", new Date())));
                        }
                    }
                }
                break;



            case "mission:new":

            case "mission:done":

                userDoc = collection.find(eq("id", userID)).first();

                if (userDoc != null) {

                    lang = userDoc.getString("lang");

                    try {
                        messageDoc = getMessage(messageType, lang);
                        title = messageDoc.getString("title");
                        body = messageDoc.getString("body");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Could not retrieve message format from database");
                    }

                    Boolean createPendingNotification=sendToToken(userID, title, body);
                    //collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification",new Date())));

                    if(createPendingNotification){
                        if(title!=null && body!=null) {
                            Document doc = new Document();
                            doc.put("id", userDoc.get("id"));
                            doc.put("title", title);
                            doc.put("body", body);
                            doc.put("timestamp", new Date());
                            doc.put("message_type", messageType);
                            pendingNotifications.insertOne(doc);
                        }
                    }
                }

                break;

            case "notification:child_family":

                userDoc = collection.find(eq("id", userID)).first();

                if (userDoc != null) {
                    lang = userDoc.getString("lang");

                    try {
                        messageDoc = getMessage(messageType, lang);
                        title = messageDoc.getString("title");
                        body = messageDoc.getString("body");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Could not retrieve message format from database");
                    }

                    Boolean createPendingNotification=sendToToken(userID, title, String.format(body, username, days_since));
                    //collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification",new Date())));

                    if(createPendingNotification){
                        if(title!=null && body!=null) {

                            Document doc = new Document();
                            doc.put("id", userDoc.get("id"));
                            doc.put("title", title);
                            doc.put("body", String.format(body, username));
                            doc.put("timestamp", new Date());
                            doc.put("message_type", messageType);
                            pendingNotifications.insertOne(doc);
                        }
                    }
                }
                break;

            case "monitoring:miss_child_data":

                userDoc = collection.find(eq("id", userID)).first();
                if(userDoc!=null) {

                    lang = userDoc.getString("lang");

                    try {
                        messageDoc = getMessage(messageType, lang);
                        title = messageDoc.getString("title");
                        body = messageDoc.getString("body");
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Could not retrieve message format from database");
                    }


                    Boolean createPendingNotification = sendToToken(userID, title, String.format(body, username, days_since));
                        //collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification",new Date())));

                    if(createPendingNotification){
                        if(title!=null && body!=null) {
                            Document doc = new Document();
                            doc.put("id", userDoc.get("id"));
                            doc.put("title", title);
                            doc.put("body", String.format(body, username, days_since));
                            doc.put("timestamp", new Date());
                            doc.put("message_type", messageType);
                            pendingNotifications.insertOne(doc);
                        }
                    }

                }


                break;


        }

    }

    public void sendToTopic(String topic, String title, String body) throws FirebaseMessagingException {


        Message message = Message.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setTopic(topic)
                .build();

        // Send a message to the devices subscribed to the provided topic.
        FirebaseMessaging.getInstance().send(message);

    }
}
