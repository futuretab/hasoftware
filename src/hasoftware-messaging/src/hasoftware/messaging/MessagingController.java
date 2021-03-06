package hasoftware.messaging;

import hasoftware.api.DeviceType;
import hasoftware.api.FunctionCode;
import hasoftware.api.InputMessageType;
import hasoftware.api.LocalModel;
import hasoftware.api.Message;
import hasoftware.api.classes.CurrentEvent;
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
import hasoftware.util.TimeUTC;
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
    private LocalModel _localModel;

    public MessagingController(Configuration configuration) {
        _username = configuration.getString("Username");
        _password = configuration.getString("Password");
        _requests = new LinkedList<>();
        _localModel = LocalModel.getInstance();
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
        _localModel.setEventQueue(eventQueue);
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        _localModel.handleEvent(event);
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
                        logger.error("Messaging error: {}", errorResponse.getErrorMessages().get(0).getMessage());
                    } else {
                        if (message.getFunctionCode() == FunctionCode.Login) {
                            // Send NotifyRequest for InputMessage
                            NotifyRequest notifyRequest = new NotifyRequest();
                            _localModel.addFunctionCodes(notifyRequest.getFunctionCodes());
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
                    OutputMessageRequest outputMessageRequest = new OutputMessageRequest();
                    outputMessageRequest.setAction(CDEFAction.Create);
                    for (OutputDevice outputDevice : point.getOutputDevices()) {
                        if (outputDevice.getDeviceTypeCode().equals(DeviceType.KIRKDECT.getCode())) {
                            outputMessageRequest.getOutputMessages().add(
                                    createOutputMessage(outputDevice.getDeviceTypeCode(),
                                            String.format("%1$s|%2$s|%3$s%4$s", "0", outputDevice.getAddress(), messagePrefix, point.getMessage2()))
                            );
                        } else if (outputDevice.getDeviceTypeCode().equals(DeviceType.SMS.getCode())) {
                            outputMessageRequest.getOutputMessages().add(
                                    createOutputMessage(outputDevice.getDeviceTypeCode(),
                                            String.format("%1$s|%2$s|%3$s%4$s", "0", outputDevice.getAddress(), messagePrefix, point.getMessage1()))
                            );
                        } else if (outputDevice.getDeviceTypeCode().equals(DeviceType.ANDROID.getCode())) {
                            outputMessageRequest.getOutputMessages().add(
                                    createOutputMessage(outputDevice.getDeviceTypeCode(),
                                            String.format("%1$s|%2$s|%3$s%4$s", "0", outputDevice.getAddress(), messagePrefix, point.getMessage1()))
                            );
                        }
                    }
                    if (!outputMessageRequest.getOutputMessages().isEmpty()) {
                        _eventQueue.add(new Event(EventType.SendMessage, outputMessageRequest));
                    }

                    // SECONDLY Handle adding or removing this event from current events
                    CurrentEvent currentEvent = _localModel.getCurrentEventByPoint(point);
                    if (inputMessage.getData().endsWith(InputMessageType.Alarm)) {
                        // We should make sure that a current event for this devices does not exist
                        if (currentEvent == null) {
                            _localModel.createCurrentEvent(point);
                        }
                    } else if (inputMessage.getData().endsWith(InputMessageType.Cancel)) {
                        // We should make sure that a current event for this devices exists
                        if (currentEvent != null) {
                            _localModel.deleteCurrentEvent(currentEvent);
                        }
                    }
                } else {
                    if (!pointResponse.isError()) {
                        // The point did not exist in the database
                        logger.warn("Point for input event [{}] doesn't exist in the database", request.data.getData());
                        // TODO add this point automatically?
                    }
                }
                // In all cases we can now delete the input message
                InputMessageRequest inputMessageRequest = new InputMessageRequest();
                inputMessageRequest.setAction(CDEFAction.Delete);
                inputMessageRequest.getIds().add(inputMessage.getId());
                _eventQueue.add(new Event(EventType.SendMessage, inputMessageRequest));
            }
        }
    }

    private OutputMessage createOutputMessage(String deviceTypeCode, String data) {
        OutputMessage outputMessage = new OutputMessage();
        outputMessage.setId(0);
        outputMessage.setDeviceTypeCode(deviceTypeCode);
        outputMessage.setData(data);
        outputMessage.setCreatedOn(TimeUTC.Null);
        return outputMessage;
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
                        PointRequest message = new PointRequest();
                        message.setAction(CDEFAction.List);
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
