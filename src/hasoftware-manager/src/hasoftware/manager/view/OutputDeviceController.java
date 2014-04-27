package hasoftware.manager.view;

import hasoftware.api.LocalModel;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputDevice;
import hasoftware.api.messages.OutputDeviceRequest;
import hasoftware.cdef.CDEFAction;
import hasoftware.manager.util.AbstractSceneController;
import hasoftware.manager.util.TimeUTCFormatCell;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.TimeUTC;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputDeviceController extends AbstractSceneController {

    private static final Logger logger = LoggerFactory.getLogger(OutputDeviceController.class);

    @FXML
    private Button buttonDelete;

    @FXML
    private Button buttonNew;

    @FXML
    private Button buttonSave;

    @FXML
    private ComboBox<String> comboDeviceType;

    @FXML
    private TableView<OutputDevice> tableOutputDevices;

    @FXML
    private TextField textAddress;

    @FXML
    private TextField textDescription;

    @FXML
    private TextField textName;

    @FXML
    private TextField textSerialNumber;

    private LinkedBlockingQueue<Event> eventQueue;
    private OutputDevice selectedOutputDevice;
    private boolean isOutputDeviceNew;
    private LocalModel localModel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        localModel = LocalModel.getInstance();

        selectedOutputDevice = null;
        isOutputDeviceNew = false;

        buttonDelete.setVisible(false);

        buttonSave.setVisible(false);

        comboDeviceType.setItems(localModel.getOutputDeviceTypes());

        Callback<TableColumn, TableCell> _timeUTCCellFactory = (TableColumn p) -> new TimeUTCFormatCell();

        TableColumn c;

        tableOutputDevices.setItems(localModel.getOutputDevices());
        tableOutputDevices.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends OutputDevice> observable, OutputDevice oldValue, OutputDevice newValue) -> {
                    onOutputDeviceSelectionChange(observable, oldValue, newValue);
                });
        tableOutputDevices.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("name"));
        tableOutputDevices.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("description"));
        tableOutputDevices.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("address"));
        tableOutputDevices.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("deviceTypeCode"));
        tableOutputDevices.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("serialNumber"));
        c = tableOutputDevices.getColumns().get(5);
        c.setCellValueFactory(new PropertyValueFactory<>("createdOn"));
        c.setCellFactory(_timeUTCCellFactory);
        c = tableOutputDevices.getColumns().get(6);
        c.setCellValueFactory(new PropertyValueFactory<>("updatedOn"));
        c.setCellFactory(_timeUTCCellFactory);
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
        switch (event.getType()) {
            case ReceiveMessage:
                Message message = event.getMessage();
                //if (message.getFunctionCode() == FunctionCode.Notify) {
                //    handleNotifyResponse((NotifyResponse) message);
                //} else if (message.getFunctionCode() == FunctionCode.OutputDevice) {
                //    handleOutputDeviceResponse((OutputDeviceResponse) message);
                //}
                break;
        }
        return true;
    }

    @Override
    public void onShown() {
        //OutputDeviceRequest message = new OutputDeviceRequest(CDEFAction.List);
        //Event event = new Event(EventType.SendMessage, message);
        //eventQueue.add(event);
    }

    @FXML
    void onDelete(ActionEvent event) {
        // NOTE: Here we just send the request, we update the table as part of the notification handler
        OutputDeviceRequest message = new OutputDeviceRequest();
        message.setAction(CDEFAction.Delete);
        message.getIds().add(selectedOutputDevice.getId());
        eventQueue.add(new Event(EventType.SendMessage, message));
    }

    @FXML
    void onNew(ActionEvent event) {
        isOutputDeviceNew = true;
        tableOutputDevices.getSelectionModel().select(null);
    }

    @FXML
    void onSave(ActionEvent e) {
        // NOTE: Here we just send the request, we update the table as part of the notification handler
        // NOTE: We cant just copy the text fields into selectedOutputDevice as that would update the table
        //       So we have to create a temporary OutputDevice and copy all fields over and send that as the update
        OutputDeviceRequest message = new OutputDeviceRequest();
        message.setAction(selectedOutputDevice == null ? CDEFAction.Create : CDEFAction.Update);
        message.getOutputDevices().add(
                createOutputDevice(
                        (selectedOutputDevice == null) ? 0 : selectedOutputDevice.getId(),
                        textName.getText(),
                        textDescription.getText(),
                        textAddress.getText(),
                        comboDeviceType.getValue(),
                        textSerialNumber.getText())
        );
        eventQueue.add(new Event(EventType.SendMessage, message));
        tableOutputDevices.getSelectionModel().select(null);
    }

    private OutputDevice createOutputDevice(int id, String name, String description, String address, String deviceTypeCode, String serialNumber) {
        OutputDevice outputDevice = new OutputDevice();
        outputDevice.setId(id);
        outputDevice.setName(name);
        outputDevice.setDescription(description);
        outputDevice.setAddress(address);
        outputDevice.setDeviceTypeCode(deviceTypeCode);
        outputDevice.setSerialNumber(serialNumber);
        outputDevice.setCreatedOn(TimeUTC.Null);
        outputDevice.setUpdatedOn(TimeUTC.Null);
        return outputDevice;
    }

    void onOutputDeviceSelectionChange(ObservableValue<? extends OutputDevice> obj, OutputDevice o, OutputDevice n) {
        selectedOutputDevice = n;
        if (selectedOutputDevice != null) {
            textName.setText(selectedOutputDevice.getName());
            textDescription.setText(selectedOutputDevice.getDescription());
            textAddress.setText(selectedOutputDevice.getAddress());
            comboDeviceType.setValue(selectedOutputDevice.getDeviceTypeCode());
            textSerialNumber.setText(selectedOutputDevice.getSerialNumber());
            buttonDelete.setVisible(true);
            buttonSave.setVisible(true);
        } else {
            textName.setText("");
            textDescription.setText("");
            textAddress.setText("");
            comboDeviceType.setValue(null);
            textSerialNumber.setText("");
            buttonDelete.setVisible(false);
            buttonSave.setVisible(isOutputDeviceNew);
        }
        isOutputDeviceNew = false;
    }

}
