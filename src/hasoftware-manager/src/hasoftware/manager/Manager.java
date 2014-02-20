package hasoftware.manager;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import de.jensd.fx.fontawesome.AwesomeDude;
import hasoftware.api.LocalModel;
import hasoftware.cdef.CDEFClient;
import hasoftware.configuration.Configuration;
import hasoftware.manager.util.AbstractSceneController;
import hasoftware.manager.util.SceneController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Manager extends Application {

    private final static int TimeCheckPeriod = 100;
    private final static String ConfigurationFilename = "hasoftware.ini";
    private final static String ConfigurationSection = "Manager";
    private final static String ConfigurationSectionServer = "Server";
    private final static String ApplicationName = "manager";

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
        logger = LoggerFactory.getLogger(Manager.class);

        logger.debug("Current Time: " + System.currentTimeMillis());
        logger.debug("Date        : " + (new Date()).getTime());

        Font.loadFont(AwesomeDude.class.getResource(AwesomeDude.FONT_AWESOME_TTF_PATH).toExternalForm(), 10.0);

        launch(args);
    }

    private static Manager _instance;

    public static Manager getInstance() {
        return _instance;
    }

    private Stage _primaryStage;
    private HashMap<String, SceneController> _scenes;
    private CDEFClient _cdefClient;
    private LinkedBlockingQueue<Event> _eventQueue;
    private Event _timeCheckEvent;
    private boolean _running;

    @Override
    public void start(Stage primaryStage) {
        _primaryStage = primaryStage;
        _instance = this;
        _scenes = new HashMap<>();
        _eventQueue = new LinkedBlockingQueue<>();
        _running = true;

        Configuration config = new Configuration();
        if (!config.open(ConfigurationFilename)) {
            logger.error("Can't load configuration file - " + ConfigurationFilename);
            return;
        }
        config.setSection(ConfigurationSection);

        // Configure the CDEF Client
        {
            String serverHost = config.getSectionString(ConfigurationSectionServer, "Host", null);
            int serverPort = config.getSectionInt(ConfigurationSectionServer, "Port", -1);
            if (serverPort == -1 || serverHost == null) {
                logger.error("[Server] section details not set Host:{} Port:{}", serverHost, serverPort);
                return;
            }
            _cdefClient = new CDEFClient(serverHost, serverPort);
            _cdefClient.setEventQueue(_eventQueue);
            if (!_cdefClient.startUp()) {
                logger.error("Failed to start CDEFClient");
                return;
            }
        }

        LocalModel.JavaFxApplication = true;
        LocalModel.getInstance().setEventQueue(_eventQueue);

        _timeCheckEvent = new Event(EventType.TimeCheck);

        _primaryStage.setOnCloseRequest((WindowEvent t) -> {
            _running = false;
            Platform.exit();
        });

        createScenes();
        setScene("Login");
        primaryStage.show();

        new Thread(() -> {
            run();
        }).start();
    }

    private void run() {
        long lastTimeCheck = 0;

        while (_running) {
            try {
                Event event = _eventQueue.poll(200, TimeUnit.MILLISECONDS);
                if (event != null) {
                    sendEvent(event);
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
        _cdefClient.shutDown();
    }

    private void sendEvent(Event event) {
        _cdefClient.handleEvent(event);
        for (SceneController controller : _scenes.values()) {
            controller.getController().handleEvent(event);
        }
    }

    private void createScenes() {
        createScene("Main");
        createScene("Login");
    }

    private void createScene(String name) {
        logger.debug("Loading scene {}", name);
        try {
            String fullFxml = "/hasoftware/manager/view/" + name + ".fxml";
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fullFxml));
            Parent root = fxmlLoader.load();
            AbstractSceneController controller = (AbstractSceneController) fxmlLoader.getController();
            controller.setEventQueue(_eventQueue);
            Scene scene = new Scene(root);
            _scenes.put(name, new SceneController(scene, controller));
        } catch (IOException ex) {
            logger.error("Scene {} not found - {}", name, ex.getMessage());
        }
    }

    public void addFunctionCodes(List<Integer> functionCodeList) {
        for (SceneController sceneController : _scenes.values()) {
            sceneController.getController().addFunctionCodes(functionCodeList);
        }
    }

    public void setScene(final String name) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> {
                setScene(name);
            });
        } else {
            if (_scenes.containsKey(name)) {
                SceneController sceneController = _scenes.get(name);
                _primaryStage.setTitle("NASCO Manager - " + name);
                _primaryStage.setScene(sceneController.getScene());
                _primaryStage.sizeToScene();
                _primaryStage.centerOnScreen();
                sceneController.getController().onShown();
            }
        }
    }
}
