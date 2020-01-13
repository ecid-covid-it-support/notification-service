package OCARIoT;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

import java.io.*;
import java.security.KeyStoreException;



@SpringBootApplication
@EnableAutoConfiguration(exclude={MongoAutoConfiguration.class})
public class Main {



    public static void main(String[] args) throws IOException, KeyStoreException {


        SpringApplication.run(Main.class,args);

        FileInputStream serviceAccount = new FileInputStream("/Users/jpdoliveira/IdeaProjects/OCARIoT/src/main/resources/ocariot-3ecd2-firebase-adminsdk-nq31m-d53e9217cd.json");
        FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).build();
        FirebaseApp.initializeApp(options);

    }
}
