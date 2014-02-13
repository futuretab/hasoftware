package hasoftware.can;

import hasoftware.api.DeviceType;
import hasoftware.api.classes.InputMessage;
import hasoftware.api.messages.InputMessageRequest;
import hasoftware.api.messages.LoginRequest;
import hasoftware.cdef.CDEFAction;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.StringUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CANController extends AbstractController implements ICANMessageReceiver {

    private final static Logger logger = LoggerFactory.getLogger(CANController.class);

    private final static long TimestampPeriod = 60 * 1000; // Timestamp every minute

    private final String _canPort;
    private final String _username;
    private final String _password;
    private LinkedBlockingQueue<Event> _eventQueue;
    private ExecutorService _executorService;
    private SEProtocol _protocol;
    private boolean _onBus;
    private long _nextTimestamp;

    public CANController(Configuration configuration) {
        _canPort = configuration.getString("Port");
        _username = configuration.getString("Username");
        _password = configuration.getString("Password");
        _onBus = false;
        _nextTimestamp = 0;
    }

    @Override
    public boolean startUp() {
        logger.debug("startUp");
        if (StringUtil.isNullOrEmpty(_canPort)) {
            logger.error("Port not set");
            return false;
        }
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
        _executorService = Executors.newSingleThreadExecutor();
        _protocol = new SEProtocol(_executorService, _canPort);
        if (!_protocol.open()) {
            logger.error("Error opening CAN Port {}", _canPort);
            return false;
        }
        _protocol.addMessageReceiver(this);
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
        if (_onBus) {
            if (_protocol.goOffBus()) {
                _onBus = false;
                logger.debug("CAN bus OFF");
            }
        }
        _protocol.close();
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
            case Connect:
                // Send LoginRequest
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(_username);
                loginRequest.setPassword(_password);
                Event e = new Event(EventType.SendMessage);
                e.setMessage(loginRequest);
                _eventQueue.add(e);
                break;

            case TimeCheck:
                if (!_onBus) {
                    if (_protocol.goOnBus()) {
                        _onBus = true;
                        logger.debug("CAN bus ON");
                    }
                } else {
                    if (event.getTime() > _nextTimestamp) {
                        _nextTimestamp = event.getTime() + TimestampPeriod;
                        _protocol.sendTimestamp(0);
                        logger.debug("CAN timestamp");
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void onReceive(CANMessage message) {
        int gatewayId = message.getGatewayId();
        StringBuilder dataSb = new StringBuilder("");
        for (int i = 0; i < 8; i++) {
            if (i < message.getLength()) {
                dataSb.append(String.format("%02X ", message.getData()[i]));
            } else {
                dataSb.append(".. ");
            }
        }
        logger.debug(String.format("RECV %04X %08X %d [%s]", gatewayId, message.getId(), message.getLength(), dataSb.toString()));

        InputMessage inputMessage = null;
        if (message.getId() == CANMessageId.CallNotification) {
            int callId = ReadU16(message.getData(), 0);
            inputMessage = new InputMessage();
            inputMessage.setId(0);
            inputMessage.setDeviceTypeCode(DeviceType.POINT.getCode());
            inputMessage.setData(String.format("%d.%dA", gatewayId, callId));
        } else if (message.getId() == CANMessageId.CancelNotification) {
            int callId = ReadU16(message.getData(), 0);
            inputMessage = new InputMessage();
            inputMessage.setId(0);
            inputMessage.setDeviceTypeCode(DeviceType.POINT.getCode());
            inputMessage.setData(String.format("%d.%dC", gatewayId, callId));
        }
        if (inputMessage != null) {
            InputMessageRequest request = new InputMessageRequest();
            request.setAction(CDEFAction.Create);
            request.getInputMessages().add(inputMessage);
            Event event = new Event(EventType.SendMessage);
            event.setMessage(request);
            _eventQueue.add(event);
        }
    }

    private static int ReadU16(byte[] data, int offset) {
        return (data[offset] << 8) | data[offset + 1];
    }
}
