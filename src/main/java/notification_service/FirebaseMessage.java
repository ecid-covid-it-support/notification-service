package notification_service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.List;

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


    private List<String> getTokens(String userID){


        return (List<String>) collection.find(eq("id", userID)).first().get("tokens");

    }

    private Document getMessage(String messageType, String lang){

        messageDoc = messagesCollection.find(eq("message_type", messageType)).first();

        return (Document) messageDoc.get(lang);

    }

    public void sendToToken(List<String> tokens, String title, String body) throws FirebaseMessagingException {

        for (String token : tokens) {

                try {

                    Message message = Message.builder()
                            .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                            .setToken(token)
                            .build();

                    FirebaseMessaging.getInstance().send(message);
                    // Send a message to the device corresponding to the provided registration token.

                } catch (FirebaseMessagingException e) {

                }
            }
    }


    public void sendToToken(String userID, String messageType, String username, int days_since) throws FirebaseMessagingException {

        switch (messageType){

            case "notification:child":

            case "notification:family":

            case "notification:teacher":

            case "mission:new":

            case "mission:done":

                userDoc = collection.find(eq("id", userID)).first();
                lang = userDoc.getString("lang");

                try {
                    messageDoc = getMessage(messageType, lang);
                    title = messageDoc.getString("title");
                    body = messageDoc.getString("body");
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Could not retrieve message format from database");
                }
                List<String> tokens = getTokens(userID);

                if (tokens !=null && !tokens.isEmpty()) {

                    sendToToken(tokens, title, body);
                    collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification",new Date())));

                }
                else{
                    Document doc= new Document();
                    doc.put("id",userDoc.get("id"));
                    doc.put("title",title);
                    doc.put("body",body);
                    doc.put("timestamp",new Date());
                    doc.put("message_type",messageType);
                    pendingNotifications.insertOne(doc);
                }

                break;

            case "notification:child_family":

                userDoc = collection.find(eq("id", userID)).first();
                lang = userDoc.getString("lang");

                messageDoc = getMessage(messageType,lang);
                title=messageDoc.getString("title");
                body=messageDoc.getString("body");
                tokens = getTokens(userID);
                if (tokens !=null && !tokens.isEmpty()) {
                    sendToToken(tokens, title, String.format(body,username));
                    collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification",new Date())));
                }
                else{
                    Document doc= new Document();
                    doc.put("id",userDoc.get("id"));
                    doc.put("title",title);
                    doc.put("body",String.format(body,username));
                    doc.put("timestamp",new Date());
                    doc.put("message_type",messageType);
                    pendingNotifications.insertOne(doc);
                }
                break;

            case "monitoring:miss_child_data":

                userDoc = collection.find(eq("id", userID)).first();
                lang = userDoc.getString("lang");

                messageDoc = getMessage(messageType,lang);
                title=messageDoc.getString("title");
                body=messageDoc.getString("body");
                tokens = getTokens(userID);
                if (tokens !=null && !tokens.isEmpty()) {
                    sendToToken(tokens, title, String.format(body,username,days_since));
                    collection.updateOne(eq("id", userID), new Document("$set", new Document("lastNotification",new Date())));
                }
                else{
                    Document doc= new Document();
                    doc.put("id",userDoc.get("id"));
                    doc.put("title",title);
                    doc.put("body",String.format(body,username,days_since));
                    doc.put("timestamp",new Date());
                    doc.put("message_type",messageType);
                    pendingNotifications.insertOne(doc);
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
