package hasoftware.manager.view;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.messages.ErrorResponse;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.manager.Manager;
import hasoftware.manager.util.AbstractSceneController;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController extends AbstractSceneController {

    private final String ConnectingToServer = "Connecting to server";
    private final String ConnectedToServer = "Connected to server";

    @FXML
    private Button login;

    @FXML
    private PasswordField password;

    @FXML
    private Label status;

    @FXML
    private Label error;

    @FXML
    private TextField username;

    private LinkedBlockingQueue<Event> eventQueue;
    private int loginTransactionNumber;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        status.setText(ConnectingToServer);
        username.setText("manager");
        password.setText("manager");
        username.textProperty().addListener((ObservableValue<? extends String> a, String b, String c) -> {
            if (error.getText().length() != 0) {
                error.setText("");
            }
        });
        password.textProperty().addListener((ObservableValue<? extends String> a, String b, String c) -> {
            if (error.getText().length() != 0) {
                error.setText("");
            }
        });
    }

    @Override
    public void onShown() {
        error.setText("");
        status.setText(ConnectingToServer);
        login.setVisible(false);
    }

    @Override
    public boolean handleEvent(Event event) {
        switch (event.getType()) {
            case Connect:
                Platform.runLater(() -> {
                    status.setText(ConnectedToServer);
                    login.setVisible(true);
                });
                break;

            case Disconnect:
                Platform.runLater(() -> {
                    status.setText(ConnectingToServer);
                    login.setVisible(true);
                });
                break;

            case ReceiveMessage:
                Message message = event.getMessage();
                if (message.getFunctionCode() == FunctionCode.Login
                        && message.getTransactionNumber() == loginTransactionNumber) {
                    if (message.isError()) {
                        final String errorMessage = ((ErrorResponse) message).getErrorMessages().get(0).getMessage();
                        Platform.runLater(() -> {
                            error.setText(errorMessage);
                            login.setDisable(false);
                        });
                    } else {
                        // Successfull login, register for any notifications
                        NotifyRequest notifyRequest = new NotifyRequest();
                        Manager.getInstance().addFunctionCodes(notifyRequest.getFunctionCodes());
                        eventQueue.add(new Event(EventType.SendMessage, notifyRequest));
                        Platform.runLater(() -> {
                            username.setText("");
                            password.setText("");
                            error.setText("");
                            login.setDisable(false);
                            Manager.getInstance().setScene("Main");
                        });
                    }
                }
                break;
        }
        return true;
    }

    @FXML
    public void onLogin(ActionEvent actionEvent) {
        LoginRequest message = new LoginRequest();
        loginTransactionNumber = message.getTransactionNumber();
        message.setUsername(username.getText());
        message.setPassword(password.getText());
        eventQueue.add(new Event(EventType.SendMessage, message));
        login.setDisable(true);
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        this.eventQueue = eventQueue;
        return true;
    }

    @Override
    public void addFunctionCodes(List<Integer> functionCodeList) {
    }
}
