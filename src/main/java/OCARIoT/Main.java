package OCARIoT;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.FileInputStream;
import java.io.IOException;


@SpringBootApplication
public class Main {

    public static void main(String[] args) throws IOException {


        SpringApplication.run(Main.class,args);

        FileInputStream serviceAccount = new FileInputStream("/Users/jpdoliveira/IdeaProjects/OCARIoT/src/main/resources/ocariot-3ecd2-firebase-adminsdk-nq31m-d53e9217cd.json");
        FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
        FirebaseApp.initializeApp(options);

    }
}
