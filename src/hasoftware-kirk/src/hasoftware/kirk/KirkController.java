package hasoftware.kirk;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.cdef.CDEFAction;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.StringUtil;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KirkController extends AbstractController {

    private final static Logger logger = LoggerFactory.getLogger(KirkController.class);

    private final String _username;
    private final String _password;
    private LinkedBlockingQueue<Event> _eventQueue;
    private final KirkProtocol _protocol;

    public KirkController(Configuration configuration) {
        _username = configuration.getString("Username");
        _password = configuration.getString("Password");
        _protocol = new KirkProtocol(configuration);
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
        return _protocol.startUp();
    }

    @Override
    public boolean readyToShutDown() {
        logger.debug("readyToShutDown");
        _protocol.readyToShutDown();
        return true;
    }

    @Override
    public boolean shutDown() {
        logger.debug("shutDown");
        _protocol.shutDown();
        return true;
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        logger.debug("setEventQueue");
        _eventQueue = eventQueue;
        _protocol.setEventQueue(eventQueue);
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        switch (event.getType()) {
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
                if (!message.isError() && message.isResponse()) {
                    if (message.getFunctionCode() == FunctionCode.Login) {
                        // Send NotifyRequest for OutputMessage
                        NotifyRequest notifyRequest = new NotifyRequest();
                        notifyRequest.getFunctionCodes().add(FunctionCode.OutputMessage);
                        Event e = new Event(EventType.SendMessage);
                        e.setMessage(notifyRequest);
                        _eventQueue.add(e);
                        // Send OutputMessage(list)
                        OutputMessageRequest outputMessageRequest = new OutputMessageRequest();
                        outputMessageRequest.setAction(CDEFAction.List);
                        e = new Event(EventType.SendMessage);
                        e.setMessage(outputMessageRequest);
                        _eventQueue.add(e);
                    }
                }
                break;
            }
        }
        _protocol.handleEvent(event);
        return true;
    }
}
