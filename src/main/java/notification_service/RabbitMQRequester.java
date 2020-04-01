package notification_service;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RabbitMQRequester {

    private static final Logger LOGGER = Logger.getLogger( RabbitMQRequester.class.getName() );

    @Autowired
    private RabbitTemplate rabbitTemplate;


    public String send(String handle, String method) {

        Message response = null;

       try {


           JSONObject myObj = new JSONObject();
           myObj.put("resource_name", method);
           myObj.append("handle", handle);

           Message message = MessageBuilder.withBody(myObj.toString().getBytes())
                   .setContentType("application/json")
                   .build();

           response = rabbitTemplate.sendAndReceive("account.rpc", method, message);
       } catch (JSONException e) {
           LOGGER.log(Level.WARNING, "JSON error on response from account ");
       } catch (AmqpException e) {
           LOGGER.log(Level.WARNING, "Error on response from account RPC");
       }


        return new String(response.getBody());


    }
}