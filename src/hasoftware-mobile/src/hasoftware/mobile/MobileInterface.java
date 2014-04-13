package hasoftware.mobile;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.api.messages.NotifyResponse;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFClient;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.StringUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MobileInterface {

    private final static String ApplicationName = "mobile-interface";
    private static Logger logger;

    public static void main(String[] args) {
        // Reconfigure and reload the default logger configuration
        System.setProperty("app_name", ApplicationName);
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextInitializer ci = new ContextInitializer(lc);
        lc.reset();
        try {
            ci.autoConfig();
        } catch (JoranException je) {
            System.err.println(je.getMessage());
        }
        MobileInterface.logger = LoggerFactory.getLogger(MobileInterface.class);
        new MobileInterface().run();
    }

    private final static int TimeCheckPeriod = 1000;
    private final static String ConfigurationFilename = "hasoftware.ini";
    private final static String ConfigurationSection = "Mobile Interface";

    private final LinkedBlockingQueue<Event> _eventQueue;
    private final List<AbstractController> _controllers;
    private Event _timeCheckEvent;
    private String _serverUsername;
    private String _serverPassword;

    private MobileInterface() {
        _eventQueue = new LinkedBlockingQueue<>();
        _controllers = new ArrayList<>();
    }

    public void run() {
        if (startUp()) {
            passEvents();
        }
        shutDown();
    }

    public boolean startUp() {
        logger.debug("startUp");
        Configuration config = new Configuration();
        if (!config.open(ConfigurationFilename)) {
            logger.error("Can't load configuration file - " + ConfigurationFilename);
            return false;
        }
        config.setSection(ConfigurationSection);

        _serverUsername = config.getString("Username");
        if (StringUtil.isNullOrEmpty(_serverUsername)) {
            logger.error("Username not set");
            return false;
        }

        _serverPassword = config.getString("Password");
        if (StringUtil.isNullOrEmpty(_serverPassword)) {
            logger.error("Password not set");
            return false;
        }

        // Configure the CDEF Client
        {
            CDEFClient controller = new CDEFClient(config);
            controller.setEventQueue(_eventQueue);
            if (!controller.startUp()) {
                logger.error("Failed to start CDEFClient");
                return false;
            }
            _controllers.add(controller);
        }

        // Configure the F1103 Controller
        {
            F1103Controller controller = new F1103Controller(config);
            controller.setEventQueue(_eventQueue);
            if (!controller.startUp()) {
                logger.error("Failed to start F1103Controller");
                return false;
            }
        }

        // Configure the SMS Controller
        {
            SMSController controller = new SMSController(config);
            controller.setEventQueue(_eventQueue);
            if (!controller.startUp()) {
                logger.error("Failed to start SMSController");
                return false;
            }
        }

        // Configure the Andoid Controller
        {
            AndroidController controller = new AndroidController(config);
            controller.setEventQueue(_eventQueue);
            if (!controller.startUp()) {
                logger.error("Failed to start AndoidController");
                return false;
            }
        }

        _timeCheckEvent = new Event(EventType.TimeCheck);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                _eventQueue.add(new Event(EventType.Shutdown));
            }
        });
        return true;
    }

    private void passEvents() {
        long lastTimeCheck = 0;

        while (true) {
            try {
                Event event = _eventQueue.poll(200, TimeUnit.MILLISECONDS);
                if (event != null) {
                    sendEvent(event);
                    if (event.getType() == EventType.Shutdown) {
                        return;
                    }
                }
                long now = System.currentTimeMillis();
                if ((now - lastTimeCheck) > TimeCheckPeriod) {
                    lastTimeCheck = now;
                    _timeCheckEvent.setTime(now);
                    sendEvent(_timeCheckEvent);
                }
            } catch (InterruptedException ex) {
                logger.error("Interrupted - {}", ex.getMessage());
                return;
            }
        }
    }

    private void sendEvent(Event event) {
        if (event.getType() != EventType.TimeCheck) {
            logger.debug("handleEvent [{} @{}]", event.getType().getCode(), event.getTime() / 1000);
        }
        handleEvent(event);
        for (AbstractController controller : _controllers) {
            controller.handleEvent(event);
        }
    }

    public boolean handleEvent(Event event) {
        switch (event.getType()) {
            case Connect: {
                // Send LoginRequest
                LoginRequest loginRequest = new LoginRequest();
                loginRequest.setUsername(_serverUsername);
                loginRequest.setPassword(_serverPassword);
                Event e = new Event(EventType.SendMessage);
                e.setMessage(loginRequest);
                _eventQueue.add(e);
            }
            break;

            case ReceiveMessage:
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
                    } else if (message.getFunctionCode() == FunctionCode.Notify) {
                        handleNotifyResponse((NotifyResponse) message);
                    }
                }
                break;
        }
        return true;
    }

    private boolean shutDown() {
        logger.debug("shutDown");
        for (AbstractController controller : _controllers) {
            controller.shutDown();
        }
        return true;
    }

    private void handleNotifyResponse(NotifyResponse notifyResponse) {
        if (notifyResponse.getAction() == CDEFAction.Create) {
            logger.debug("handleNotifyResponse[Create] - {}", notifyResponse.getIds());
            // When we get a notify for created output messages go get them
            OutputMessageRequest outputMessageRequest = new OutputMessageRequest();
            outputMessageRequest.setAction(CDEFAction.List);
            outputMessageRequest.getIds().addAll(notifyResponse.getIds());
            Event event = new Event(hasoftware.util.EventType.SendMessage);
            event.setMessage(outputMessageRequest);
            _eventQueue.add(event);
        }
    }
}
