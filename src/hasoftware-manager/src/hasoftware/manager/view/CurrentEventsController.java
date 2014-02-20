package hasoftware.manager.view;

import hasoftware.manager.util.AbstractSceneController;
import hasoftware.api.LocalModel;
import hasoftware.util.Event;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentEventsController extends AbstractSceneController {

    private static final Logger logger = LoggerFactory.getLogger(CurrentEventsController.class);

    @FXML
    private ListView listCurrentEvents;

    @FXML
    private Button buttonCancel;

    private LinkedBlockingQueue<Event> eventQueue;
    private LocalModel localModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        localModel = LocalModel.getInstance();

        listCurrentEvents.setItems(localModel.getCurrentEvents());
    }

    @FXML
    void onCancel(ActionEvent actionEvent) {
    }

    @Override
    public void addFunctionCodes(List<Integer> functionCodeList) {
        // TODO
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        this.eventQueue = eventQueue;
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        return true;
    }

    @Override
    public void onShown() {
    }
}
