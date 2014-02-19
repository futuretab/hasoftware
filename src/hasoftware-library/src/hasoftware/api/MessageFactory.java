package hasoftware.api;

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

public class MessageFactory {

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
