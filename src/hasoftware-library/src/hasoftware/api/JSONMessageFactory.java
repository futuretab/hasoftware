package hasoftware.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hasoftware.api.messages.CurrentEventRequest;
import hasoftware.api.messages.CurrentEventResponse;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.NotifyRequest;
import java.io.IOException;

public class JSONMessageFactory {

    private static final ObjectMapper _mapper = new ObjectMapper();

    public static Message decode(String data) {
        try {
            int index = data.indexOf(' ');
            String type = data.substring(0, index);
            String json = data.substring(index);
            switch (type) {
                case "LoginRequest":
                    return _mapper.readValue(json, LoginRequest.class);
                case "NotifyRequest":
                    return _mapper.readValue(json, NotifyRequest.class);
                case "CurrentEventRequest":
                    return _mapper.readValue(json, CurrentEventRequest.class);
                case "CurrentEventResponse":
                    return _mapper.readValue(json, CurrentEventResponse.class);
            }
        } catch (IOException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        return null;
    }

    public static String encode(Message message) {
        String data = message.getClass().getSimpleName();
        try {
            data = data + " " + _mapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
        }
        return data;
    }
}
