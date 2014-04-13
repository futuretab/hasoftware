package hasoftware.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hasoftware.api.messages.CurrentEventRequest;
import hasoftware.api.messages.CurrentEventResponse;
import hasoftware.api.messages.ErrorResponse;
import hasoftware.api.messages.InputMessageRequest;
import hasoftware.api.messages.InputMessageResponse;
import hasoftware.api.messages.LocationRequest;
import hasoftware.api.messages.LocationResponse;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.LoginResponse;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.api.messages.NotifyResponse;
import hasoftware.api.messages.OutputDeviceRequest;
import hasoftware.api.messages.OutputDeviceResponse;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.api.messages.OutputMessageResponse;
import hasoftware.api.messages.PointRequest;
import hasoftware.api.messages.PointResponse;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.io.IOException;

public class MessageFactory {

    private static ObjectMapper _mapper = new ObjectMapper();

    public static Message decodeJson(String data) {
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

    public static String encodeJson(Message message) {
        String data = message.getClass().getSimpleName();
        try {
            data = data + " " + _mapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
        }
        return data;
    }

    public static Message decode(CDEFMessage cdefMessage) {
        Message message = null;
        int functionCode = cdefMessage.getU16(0);
        int systemFlags = cdefMessage.getU8(6);
        if (systemFlags == (CDEFSystemFlags.Error | CDEFSystemFlags.Response)) {
            message = new ErrorResponse(cdefMessage);
        } else {
            boolean isRequest = ((systemFlags & CDEFSystemFlags.Response) == 0);
            switch (functionCode) {
                case FunctionCode.Notify:
                    return (isRequest) ? new NotifyRequest(cdefMessage) : new NotifyResponse(cdefMessage);
                case FunctionCode.InputMessage:
                    return (isRequest) ? new InputMessageRequest(cdefMessage) : new InputMessageResponse(cdefMessage);
                case FunctionCode.OutputMessage:
                    return (isRequest) ? new OutputMessageRequest(cdefMessage) : new OutputMessageResponse(cdefMessage);
                case FunctionCode.OutputDevice:
                    return (isRequest) ? new OutputDeviceRequest(cdefMessage) : new OutputDeviceResponse(cdefMessage);
                case FunctionCode.Login:
                    return (isRequest) ? new LoginRequest(cdefMessage) : new LoginResponse(cdefMessage);
                case FunctionCode.Location:
                    return (isRequest) ? new LocationRequest(cdefMessage) : new LocationResponse(cdefMessage);
                case FunctionCode.Point:
                    return (isRequest) ? new PointRequest(cdefMessage) : new PointResponse(cdefMessage);
                case FunctionCode.CurrentEvent:
                    return (isRequest) ? new CurrentEventRequest(cdefMessage) : new CurrentEventResponse(cdefMessage);
            }
        }
        return message;
    }
}
