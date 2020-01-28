package notification_service;


import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;


public class FirebaseMessage {

    public static void sendToToken(String token, String title, String body) throws FirebaseMessagingException {

        Message message = Message.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setToken(token)
                .build();

        // Send a message to the device corresponding to the provided registration token.
        FirebaseMessaging.getInstance().send(message);
    }

    public static void sendToTopic(String topic, String title, String body) throws FirebaseMessagingException {


        Message message = Message.builder()
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .setTopic(topic)
                .build();

        // Send a message to the devices subscribed to the provided topic.
        FirebaseMessaging.getInstance().send(message);

    }
}
