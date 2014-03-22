package hasoftware.manager.view;

import de.jensd.fx.fontawesome.AwesomeIcon;
import hasoftware.api.LocalModel;
import hasoftware.api.classes.CurrentEvent;
import hasoftware.manager.util.AbstractSceneController;
import hasoftware.util.Event;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
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

        listCurrentEvents.getStylesheets().add("/hasoftware/manager/view/manager.css");
        listCurrentEvents.setItems(localModel.getCurrentEvents());
        listCurrentEvents.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView p) {
                ListCell cell = new ActiveEventListCell();
                //cell.setOnMouseClicked((MouseEvent mouseEvent) -> { onAvialableDoubleClick(mouseEvent); });
                return cell;
            }
        });
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

    class ActiveEventListCell extends ListCell<CurrentEvent> {

        private static final String FONT_AWESOME = "FontAwesome";

        private final GridPane grid = new GridPane();
        private final Label icon = new Label();
        private final Label name = new Label();
        private final Label time = new Label();

        public ActiveEventListCell() {
            grid.setHgap(2);
            grid.setVgap(2);
            grid.setPadding(new Insets(0, 2, 0, 2));
            icon.setPrefWidth(28);
            icon.setFont(Font.font(FONT_AWESOME, FontWeight.NORMAL, 28));
            name.setMinWidth(490);
            name.setMaxWidth(490);
            time.setAlignment(Pos.CENTER_RIGHT);
            time.setMinWidth(200);
            time.setMaxWidth(200);
            grid.add(icon, 0, 0);
            grid.add(name, 1, 0);
            grid.add(time, 2, 0);
        }

        @Override
        public void updateItem(CurrentEvent currentEvent, boolean empty) {
            super.updateItem(currentEvent, empty);
            if (empty) {
                clearContent();
            } else {
                addContent(currentEvent);
            }
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void addContent(CurrentEvent currentEvent) {
            setText(null);
            icon.setText(AwesomeIcon.MOBILE_PHONE.toString());
            icon.setTextFill(Color.RED);

            name.setText("ID:" + currentEvent.getId() + " PointID:" + currentEvent.getPoint().getId());

            long millis = System.currentTimeMillis() - currentEvent.getCreatedOn().getTimeUTC();
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
            if (hours != 0) {
                time.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
            } else if (minutes != 0) {
                time.setText(String.format("%02d:%02d", minutes, seconds));
            } else {
                time.setText(String.format("%02d", seconds));
            }
            setGraphic(grid);
        }
    }
}
