package hasoftware.messaging;

import hasoftware.api.DeviceType;
import hasoftware.api.FunctionCode;
import hasoftware.api.InputMessageType;
import hasoftware.api.Message;
import hasoftware.api.classes.InputMessage;
import hasoftware.api.classes.OutputDevice;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.classes.Point;
import hasoftware.api.messages.ErrorResponse;
import hasoftware.api.messages.InputMessageRequest;
import hasoftware.api.messages.InputMessageResponse;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.api.messages.NotifyResponse;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.api.messages.PointRequest;
import hasoftware.api.messages.PointResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.OutstandingRequest;
import hasoftware.util.StringUtil;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingController extends AbstractController {

    private final static Logger logger = LoggerFactory.getLogger(MessagingController.class);

    private final String _username;
    private final String _password;
    private LinkedBlockingQueue<Event> _eventQueue;
    private final LinkedList<OutstandingRequest<InputMessage>> _requests;

    public MessagingController(Configuration configuration) {
        _username = configuration.getString("Username");
        _password = configuration.getString("Password");
        _requests = new LinkedList<>();
    }

    @Override
    public boolean startUp() {
        logger.debug("startUp");
        if (StringUtil.isNullOrEmpty(_username)) {
            logger.error("Username not set");
            return false;
        }
        if (StringUtil.isNullOrEmpty(_password)) {
            logger.error("Password not set");
            return false;
        }
        if (_eventQueue == null) {
            logger.error("Error EventQueue not set");
            return false;
        }
        return true;
    }

    @Override
    public boolean readyToShutDown() {
        logger.debug("readyToShutDown");
        return true;
    }

    @Override
    public boolean shutDown() {
        logger.debug("shutDown");
        return true;
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        logger.debug("setEventQueue");
        _eventQueue = eventQueue;
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        switch (event.getType()) {
            case TimeCheck:
                break;

            case Connect: {
                // Send LoginRequest
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(_username);
                loginRequest.setPassword(_password);
                Event e = new Event(EventType.SendMessage);
                e.setMessage(loginRequest);
                _eventQueue.add(e);
            }
            break;

            case ReceiveMessage: {
                Message message = event.getMessage();
                if (message.isResponse()) {
                    if (message.isError()) {
                        ErrorResponse errorResponse = (ErrorResponse) message;
                        logger.error("Messaging error: {}", errorResponse.getErrors().get(0).getMessage());
                    } else {
                        if (message.getFunctionCode() == FunctionCode.Login) {
                            // Send NotifyRequest for InputMessage
                            NotifyRequest notifyRequest = new NotifyRequest();
                            notifyRequest.getFunctionCodes().add(FunctionCode.InputMessage);
                            Event e = new Event(EventType.SendMessage);
                            e.setMessage(notifyRequest);
                            _eventQueue.add(e);
                            // Send InputMessage(list)
                            InputMessageRequest inputMessageRequest = new InputMessageRequest();
                            inputMessageRequest.setAction(CDEFAction.List);
                            e = new Event(EventType.SendMessage);
                            e.setMessage(inputMessageRequest);
                            _eventQueue.add(e);
                        } else if (message.getFunctionCode() == FunctionCode.InputMessage) {
                            handleInputMessageResponse((InputMessageResponse) message);
                        } else if (message.getFunctionCode() == FunctionCode.Notify) {
                            handleNotifyResponse((NotifyResponse) message);
                        } else if (message.getFunctionCode() == FunctionCode.Point) {
                            handlePointResponse((PointResponse) message);
                        }
                    }
                }
            }
            break;
        }
        return true;
    }

    private void handlePointResponse(PointResponse pointResponse) {
        logger.debug("handlePointResponse");
        int transactionNumber = pointResponse.getTransactionNumber();
        for (OutstandingRequest<InputMessage> request : _requests) {
            InputMessage inputMessage = request.data;
            if (request.transactionNumber == transactionNumber) {
                if (!pointResponse.isError() && !pointResponse.getPoints().isEmpty()) {
                    // At this point we can assume the point exists in the database
                    Point point = pointResponse.getPoints().get(0);
                    boolean isAlarm = inputMessage.getData().endsWith("A");
                    String messagePrefix = isAlarm ? "ALARM: " : "CANCEL: ";

                    // FIRSTLY Handle sending messages to any of the allocated output devices
                    OutputMessageRequest outputMessageRequest = new OutputMessageRequest(CDEFAction.Create);
                    for (OutputDevice outputDevice : point.getOutputDevices()) {
                        if (outputDevice.getDeviceTypeCode().equals(DeviceType.KIRKDECT.getCode())) {
                            outputMessageRequest.getOutputMessages().add(
                                    new OutputMessage(0, outputDevice.getDeviceTypeCode(),
                                            String.format("%1$s|%2$s|%3$s%4$s", "0", outputDevice.getAddress(), messagePrefix, point.getMessage2()),
                                            null)
                            );
                        } else if (outputDevice.getDeviceTypeCode().equals(DeviceType.SMS.getCode())) {
                            outputMessageRequest.getOutputMessages().add(
                                    new OutputMessage(0, outputDevice.getDeviceTypeCode(),
                                            String.format("%1$s|%2$s|%3$s%4$s", "0", outputDevice.getAddress(), messagePrefix, point.getMessage1()),
                                            null)
                            );
                        } else if (outputDevice.getDeviceTypeCode().equals(DeviceType.ANDROID.getCode())) {
                            outputMessageRequest.getOutputMessages().add(
                                    new OutputMessage(0, outputDevice.getDeviceTypeCode(),
                                            String.format("%1$s|%2$s|%3$s%4$s", "0", outputDevice.getAddress(), messagePrefix, point.getMessage1()),
                                            null)
                            );
                        }
                    }
                    if (!outputMessageRequest.getOutputMessages().isEmpty()) {
                        _eventQueue.add(new Event(EventType.SendMessage, outputMessageRequest));
                    }

                    // SECONDLY Handle adding or removing this event from current events
                    if (inputMessage.getData().endsWith(InputMessageType.Alarm)) {
                        // We should make sure that an current event for this devices does not exist
                    } else if (inputMessage.getData().endsWith(InputMessageType.Cancel)) {
                        // We should make sure that an current event for this devices exists
                    }
                } else {
                    if (!pointResponse.isError()) {
                        // The point did not exist in the database
                        logger.warn("Point for input event [{}] doesn't exist in the database", request.data.getData());
                        // TODO add this point automatically?
                    }
                }
                // In all cases we can now delete the input message
                InputMessageRequest inputMessageRequest = new InputMessageRequest(CDEFAction.Delete);
                inputMessageRequest.getIds().add(inputMessage.getId());
                _eventQueue.add(new Event(EventType.SendMessage, inputMessageRequest));
            }
        }
    }

    private void handleInputMessageResponse(InputMessageResponse inputMessageResponse) {
        logger.debug("handleInputMessageResponse");
        if (inputMessageResponse.getAction() == CDEFAction.List) {
            if (!inputMessageResponse.getInputMessages().isEmpty()) {
                for (InputMessage inputMessage : inputMessageResponse.getInputMessages()) {
                    if (inputMessage.getDeviceTypeCode().equals(DeviceType.POINT.getCode())) {
                        // Ask the server for information about this device & output devices (async)
                        // The address is the data without the last character
                        String data = inputMessage.getData();
                        String address = data.substring(0, data.length() - 1);
                        PointRequest message = new PointRequest(CDEFAction.List);
                        message.setAddress(address);
                        Event event = new Event(EventType.SendMessage, message);
                        _requests.add(new OutstandingRequest<>(message.getTransactionNumber(), inputMessage));
                        _eventQueue.add(event);
                    }
                }
            }
        }
    }

    private void handleNotifyResponse(NotifyResponse notifyResponse) {
        if (notifyResponse.getAction() == CDEFAction.Create) {
            logger.debug("handleNotifyResponse[Create] - {}", notifyResponse.getIds());
            // When we get a notify for created input messages go get them
            InputMessageRequest inputMessageRequest = new InputMessageRequest();
            inputMessageRequest.setAction(CDEFAction.List);
            inputMessageRequest.getIds().addAll(notifyResponse.getIds());
            Event event = new Event(EventType.SendMessage);
            event.setMessage(inputMessageRequest);
            _eventQueue.add(event);
        }
    }
}
