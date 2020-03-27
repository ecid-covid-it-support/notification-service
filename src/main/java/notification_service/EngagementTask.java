package notification_service;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class EngagementTask {

    @Autowired
    private MongoCollection<Document> collection;

    @Autowired
    private FirebaseMessage firebaseMessage;

    @Autowired
    private RabbitMQRequester rabbitMQRequester;

    private static final Logger LOGGER = Logger.getLogger( RabbitMQ.class.getName() );

    @Scheduled(fixedRate = 3600000) //1 hour 3600000
    public void sendEngagementNotification() {

        Date timeNow = new Date();

        FindIterable<Document> iterable = collection.find();
        MongoCursor<Document> cursor = iterable.iterator();

        try {
            while (cursor.hasNext()) {

                Document document = cursor.next();

                String userID = document.getString("id");
                Date lastLogin = (Date) document.get("lastLogin");
                Date lastNotification = (Date) document.get("lastNotification");
                String userType = document.getString("type");

                long diffLogin = timeNow.getTime() - lastLogin.getTime();
                long diffNotification = timeNow.getTime() - lastNotification.getTime();

                //long diffLoginMinutes = diffLogin / (60 * 1000) % 60;
                //long diffLoginHours = diffLogin / (60 * 60 * 1000) % 24;
                long diffLoginDays = diffLogin / (24 * 60 * 60 * 1000);

                //long diffNotificationMinutes = diffNotification / (60 * 1000) % 60;
                //long diffNotificationHours = diffNotification / (60 * 60 * 1000) % 24;
                long diffNotificationDays = diffNotification / (24 * 60 * 60 * 1000);

                int daysSinceLastLogin = 7;
                int i;


                if (diffLoginDays >= daysSinceLastLogin && diffNotificationDays >= 2) {


                    switch (userType){

                        case "children":

                            String familyID;
                            String user;


                            firebaseMessage.sendToToken(userID,"notification:child", null, daysSinceLastLogin);


                            //Get Children username
                            String info = rabbitMQRequester.send("_id="+userID,"children.find");

                            JSONArray jsonarray = new JSONArray(info);


                            user = (String) jsonarray.getJSONObject(0).get("username");



                            String family = rabbitMQRequester.send("?children="+userID,"families.find");
                            jsonarray = new JSONArray(family);
                            try {

                                for (i=0;i<jsonarray.length();i++) {

                                    familyID = (String) jsonarray.getJSONObject(i).get("id");

                                    if (familyID != null && !familyID.isEmpty()) {
                                        firebaseMessage.sendToToken(familyID, "notification:child_family", user, daysSinceLastLogin);
                                    }
                                }

                            } catch (JSONException e) {

                                LOGGER.log(Level.WARNING, "Could not retrieve id of family when sending engagement notification");
                            }
                            //get teachers of children

                            /*String teacher  = rabbitMQRequester.send("?children="+userID,"educators.find");
                            jsonarray = new JSONArray(family);
                            try {
                                teacherID = (String) jsonarray.getJSONObject(0).get("id");
                                if (teacherID!=null && !teacherID.isEmpty()) {
                                    firebaseMessage.sendToToken(teacherID, "notification:child_teacher", user);

                                }

                            } catch (JSONException e) {

                            }*/

                            break;

                        case "family":

                            firebaseMessage.sendToToken(userID,"notification:family", null, daysSinceLastLogin);

                            break;

                        case "educator":

                            System.out.println("Estou a enviar para o educator");
                            firebaseMessage.sendToToken(userID,"notification:educator", null,daysSinceLastLogin);

                            break;
                    }
                }


            }
        } catch (Exception e) {
            cursor.close();
        }
    }
}




