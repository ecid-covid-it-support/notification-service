package OCARIoT;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessagingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
@EnableAutoConfiguration
public class Main {

    public static void main(String[] args) throws IOException, FirebaseMessagingException {

        SpringApplication.run(Main.class,args);

        FileInputStream serviceAccount = new FileInputStream("//Users/jpdoliveira/IdeaProjects/OCARIoT/src/main/java/OCARIoT/ocariot-3ecd2-firebase-adminsdk-nq31m-c7616a4aad.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://ocariot-3ecd2.firebaseio.com")
                .build();

        FirebaseApp.initializeApp(options);

    }
}
