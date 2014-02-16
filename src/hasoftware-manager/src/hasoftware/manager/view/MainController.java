package hasoftware.manager.view;

import hasoftware.manager.util.AbstractSceneController;
import hasoftware.manager.util.LocalModel;
import hasoftware.util.Event;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController extends AbstractSceneController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private TabPane tabPane;

    private LinkedBlockingQueue<Event> eventQueue;

    private final String[] tabs = {"FastPage", "Site", "OutputDevice"};
    private final Map<String, AbstractSceneController> tabControllerMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        for (String tab : tabs) {
            tabPane.getTabs().add(new Tab(tab));
        }
        tabPane.getSelectionModel().clearSelection();
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Tab> obj, Tab o, Tab n) -> {
                    onTabSelectionChange(obj, o, n);
                }
        );
    }

    @Override
    public void onShown() {
        LocalModel.getInstance().onShown();
    }

    @Override
    public boolean handleEvent(Event event) {
        // Pass the event to all the sub controllers
        LocalModel.getInstance().handleEvent(event);
        for (AbstractSceneController controller : tabControllerMap.values()) {
            controller.handleEvent(event);
        }
        return true;
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        this.eventQueue = eventQueue;
        return true;
    }

    @Override
    public void addFunctionCodes(List<Integer> functionCodeList) {
        LocalModel.getInstance().addFunctionCodes(functionCodeList);
        for (AbstractSceneController controller : tabControllerMap.values()) {
            controller.addFunctionCodes(functionCodeList);
        }
    }

    private void onTabSelectionChange(ObservableValue<? extends Tab> obj, Tab o, Tab n) {
        Parent root;
        AbstractSceneController controller = null;
        if (n.getContent() == null) {
            String fullFxml = "/hasoftware/manager/view/" + n.getText() + ".fxml";
            try {
                FXMLLoader loader = new FXMLLoader();
                root = (Parent) loader.load(MainController.class.getResource(fullFxml).openStream());
                n.setContent(root);
                controller = (AbstractSceneController) loader.getController();
                controller.setEventQueue(eventQueue);
                tabControllerMap.put(n.getText(), controller);
            } catch (IOException ex) {
                logger.error("Unable to load tab context {} - {}", fullFxml, ex.getMessage());
            }
        } else {
            controller = tabControllerMap.get(n.getText());
        }
        if (controller != null) {
            controller.onShown();
        }
    }
}
