package OCARIoT;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;


public class FirebaseMessage {


    public static void sendToToken(String token, String title, String body) throws FirebaseMessagingException {


        Message message = Message.builder()
                .setNotification(new Notification(title,body))
                .setToken(token)
                .build();

        // Send a message to the device corresponding to the provided
        // registration token.
        String response = FirebaseMessaging.getInstance().send(message);
        // Response is a message ID string.
        //System.out.println("Successfully sent message: " + response);
        // [END send_to_token]
    }

    public static void sendToTopic(String topic, String title, String body) throws FirebaseMessagingException {



        Message message = Message.builder()
                .setNotification(new Notification(title,body))
                .setTopic(topic)
                .build();

        // Send a message to the devices subscribed to the provided topic.
        String response = FirebaseMessaging.getInstance().send(message);
        // Response is a message ID string.
        //System.out.println("Successfully sent message: " + response);
        // [END send_to_topic]
    }



}
