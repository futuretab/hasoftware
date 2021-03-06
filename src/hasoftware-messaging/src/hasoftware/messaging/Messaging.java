package hasoftware.messaging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import hasoftware.cdef.CDEFClient;
import hasoftware.configuration.Configuration;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Messaging {

    private final static String ApplicationName = "messaging";
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
        Messaging.logger = LoggerFactory.getLogger(Messaging.class);
        new Messaging().run();
    }

    private final static int TimeCheckPeriod = 1000;
    private final static String ConfigurationFilename = "hasoftware.ini";
    private final static String ConfigurationSection = "Messaging";

    private CDEFClient _cdefClient;
    private MessagingController _messagingController;
    private final LinkedBlockingQueue<Event> _eventQueue;
    private Event _timeCheckEvent;

    public Messaging() {
        _eventQueue = new LinkedBlockingQueue<>();
    }

    public void run() {
        if (startUp()) {
            passEvents();
            shutDown();
        }
    }

    public boolean startUp() {
        logger.debug("startUp");
        Configuration config = new Configuration();
        if (!config.open(ConfigurationFilename)) {
            logger.error("Can't load configuration file - " + ConfigurationFilename);
            return false;
        }
        config.setSection(ConfigurationSection);

        // Configure the CDEF Client
        {
            _cdefClient = new CDEFClient(config);
            _cdefClient.setEventQueue(_eventQueue);
            if (!_cdefClient.startUp()) {
                logger.error("Failed to start CDEFClient");
                return false;
            }
        }

        // Configure the Messaging Controller
        {
            _messagingController = new MessagingController(config);
            _messagingController.setEventQueue(_eventQueue);
            if (!_messagingController.startUp()) {
                logger.error("Failed to start MessagingController");
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
        _cdefClient.handleEvent(event);
        _messagingController.handleEvent(event);
    }

    private boolean shutDown() {
        logger.debug("shutDown");
        _messagingController.shutDown();
        _cdefClient.shutDown();
        return true;
    }
}
