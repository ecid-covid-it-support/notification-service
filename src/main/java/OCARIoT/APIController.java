package OCARIoT;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.gson.JsonObject;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
public class APIController {

    UserMockedData userMockedData = UserMockedData.getInstance();

    @RequestMapping("/")
    public String index() {
        return "OCARIoT";
    }

    @PostMapping("user/{id}")
    public User create(@PathVariable String id, @RequestBody Map<String, String> body){
        int userId = Integer.parseInt(id);
        String token = body.get("token");
        return userMockedData.createUser(userId, token);
    }

    @PutMapping("user/{id}")
    public User update(@PathVariable String id, Map<String, String> body){

        int userId = Integer.parseInt(id);
        String token = body.get("token");
        return userMockedData.updateUser(userId,token);
    }

    @GetMapping("/user/{id}")
    public User show(@PathVariable String id){
        int userId = Integer.parseInt(id);
        return userMockedData.getUserById(userId);
    }

    @PostMapping("notification/topic")
    public JsonObject topic(@RequestBody Map<String, String> body) throws IOException, FirebaseMessagingException {

        String topic = body.get("topic");
        String title = body.get("title");
        String content = body.get("body");
        FirebaseMessage.sendToTopic(topic, title, content);
        return null;

    }

    @PostMapping("notification/{id}")
    public JsonObject token(@PathVariable String id, @RequestBody Map<String, String> body) throws IOException, FirebaseMessagingException {

        int userID = Integer.parseInt(id);

        String title = body.get("title");
        String content = body.get("body");
        String token = userMockedData.getTokenById(userID);

        FirebaseMessage.sendToToken(token, title, content);
        return null;
    }




}
