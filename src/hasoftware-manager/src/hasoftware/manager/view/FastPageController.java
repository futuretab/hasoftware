package hasoftware.manager.view;

import de.jensd.fx.fontawesome.AwesomeIcon;
import hasoftware.api.DeviceType;
import hasoftware.api.LocalModel;
import hasoftware.api.classes.OutputDevice;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.cdef.CDEFAction;
import hasoftware.manager.util.AbstractSceneController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.StringUtil;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FastPageController extends AbstractSceneController {

    private static final Logger logger = LoggerFactory.getLogger(FastPageController.class);

    @FXML
    private Label labelCount;

    @FXML
    private Label labelStatus;

    @FXML
    private ListView listAvailableDevices;

    @FXML
    private ListView listSelectedDevices;

    @FXML
    private ListView listPredefinedMessages;

    @FXML
    private Button buttonSend;

    @FXML
    private TextField textMessage;

    @FXML
    private TextField textSearch;

    private LinkedBlockingQueue<Event> eventQueue;
    private ObservableList<OutputDevice> filteredOutputDevices;
    private ObservableList<OutputDevice> selectedOutputDevices;
    private LocalModel localModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        localModel = LocalModel.getInstance();

        textSearch.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue obj, String o, String n) {
                search(o, n);
            }
        });

        textMessage.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue obj, String o, String n) {
                countMessageCharacters(o, n);
            }
        });

        labelCount.setText("");

        filteredOutputDevices = FXCollections.observableArrayList();

        listAvailableDevices.setItems(localModel.getOutputDevices());
        listAvailableDevices.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView p) {
                ListCell cell = new OutputDeviceListCell();
                cell.setOnMouseClicked((MouseEvent mouseEvent) -> {
                    onAvialableDoubleClick(mouseEvent);
                });
                return cell;
            }
        });

        selectedOutputDevices = FXCollections.observableArrayList();
        listSelectedDevices.setItems(selectedOutputDevices);
        listSelectedDevices.setCellFactory(new Callback<ListView, ListCell>() {
            @Override
            public ListCell call(ListView p) {
                ListCell cell = new OutputDeviceListCell();
                cell.setOnMouseClicked((MouseEvent mouseEvent) -> {
                    onSelectedDoubleClick(mouseEvent);
                });
                return cell;
            }
        });
    }

    @FXML
    void onClear(ActionEvent actionEvent) {
        selectedOutputDevices.clear();
    }

    @FXML
    void onSend(ActionEvent actionEvent) {
        OutputMessageRequest message = new OutputMessageRequest(CDEFAction.Create);
        for (OutputDevice outputDevice : selectedOutputDevices) {
            OutputMessage outputMessage = new OutputMessage();
            outputMessage.setId(0);
            outputMessage.setDeviceTypeCode(outputDevice.getDeviceTypeCode());
            outputMessage.setData(String.format("%1$s|%2$s|%3$s", "0", outputDevice.getAddress(), textMessage.getText()));
            message.getOutputMessages().add(outputMessage);
        }
        Event event = new Event(EventType.SendMessage, message);
        eventQueue.add(event);
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

    private void onAvialableDoubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            OutputDeviceListCell odlc = (OutputDeviceListCell) mouseEvent.getSource();
            OutputDevice outputDevice = odlc.getItem();
            if (!selectedOutputDevices.contains(outputDevice)) {
                selectedOutputDevices.add(outputDevice);
            }
        }
    }

    private void onSelectedDoubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2) {
            OutputDeviceListCell odlc = (OutputDeviceListCell) mouseEvent.getSource();
            OutputDevice outputDevice = odlc.getItem();
            selectedOutputDevices.remove(outputDevice);
        }
    }

    private void countMessageCharacters(String o, String n) {
        if (StringUtil.isNullOrEmpty(n)) {
            labelCount.setText("");
        } else {
            labelCount.setText("" + n.length());
        }
    }

    private void search(String o, String n) {
        if (StringUtil.isNullOrEmpty(n)) {
            listAvailableDevices.setItems(localModel.getOutputDevices());
        } else {
            filteredOutputDevices.clear();
            for (OutputDevice outputDevice : localModel.getOutputDevices()) {
                if (outputDevice.getName().toUpperCase().contains(n.toUpperCase())) {
                    filteredOutputDevices.add(outputDevice);
                }
            }
            listAvailableDevices.setItems(filteredOutputDevices);
        }
    }

    class OutputDeviceListCell extends ListCell<OutputDevice> {

        private static final String FONT_AWESOME = "FontAwesome";

        private final GridPane grid = new GridPane();
        private final Label icon = new Label();
        private final Label name = new Label();

        public OutputDeviceListCell() {
            configureGrid();
            configureIcon();
            addControlsToGrid();
        }

        private void configureGrid() {
            grid.setHgap(2);
            grid.setVgap(2);
            grid.setPadding(new Insets(0, 2, 0, 2));
        }

        private void configureIcon() {
            icon.setPrefWidth(16);
            icon.setFont(Font.font(FONT_AWESOME, FontWeight.BOLD, 14));
        }

        private void addControlsToGrid() {
            grid.add(icon, 0, 0);
            grid.add(name, 1, 0);
        }

        @Override
        public void updateItem(OutputDevice outputDevice, boolean empty) {
            super.updateItem(outputDevice, empty);
            if (empty) {
                clearContent();
            } else {
                addContent(outputDevice);
            }
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void addContent(OutputDevice outputDevice) {
            setText(null);
            if (outputDevice.getDeviceTypeCode().equals(DeviceType.SMS.getCode())) {
                icon.setText(AwesomeIcon.MOBILE_PHONE.toString());
                icon.setTextFill(Color.RED);
            } else if (outputDevice.getDeviceTypeCode().equals(DeviceType.KIRKDECT.getCode())) {
                icon.setText(AwesomeIcon.PHONE.toString());
                icon.setTextFill(Color.GREEN);
            } else if (outputDevice.getDeviceTypeCode().equals(DeviceType.EMAIL.getCode())) {
                icon.setText(AwesomeIcon.ENVELOPE_ALT.toString());
                icon.setTextFill(Color.BLUE);
            } else if (outputDevice.getDeviceTypeCode().equals(DeviceType.ANDROID.getCode())) {
                icon.setText(AwesomeIcon.ANDROID.toString());
                icon.setTextFill(Color.BLACK);
            } else {
                icon.setText(AwesomeIcon.QUESTION.toString());
                icon.setTextFill(Color.GRAY);
            }
            name.setText(outputDevice.getName() + " | " + outputDevice.getAddress());
            setGraphic(grid);
        }
    }
}
