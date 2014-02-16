package hasoftware.manager.util;

import hasoftware.api.DeviceType;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputDevice;
import hasoftware.api.messages.NotifyResponse;
import hasoftware.api.messages.OutputDeviceRequest;
import hasoftware.api.messages.OutputDeviceResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.IEventCreator;
import hasoftware.util.IEventHandler;
import hasoftware.util.OutstandingRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LocalModel implements IEventCreator, IEventHandler {

    private static final LocalModel _instance = new LocalModel();

    public static LocalModel getInstance() {
        return _instance;
    }

    private LinkedBlockingQueue<Event> _eventQueue;
    private final ObservableList<String> _outputDeviceTypes;
    private final ObservableList<OutputDevice> _outputDevices;
    private LinkedList<OutstandingRequest<Integer>> _requests;

    private LocalModel() {
        _outputDeviceTypes = FXCollections.observableArrayList();
        _outputDeviceTypes.addAll(
                DeviceType.KIRKDECT.getCode(),
                DeviceType.SMS.getCode(),
                DeviceType.EMAIL.getCode(),
                DeviceType.ANDROID.getCode());

        _outputDevices = FXCollections.observableArrayList();

        _requests = new LinkedList<>();
    }

    public ObservableList<String> getOutputDeviceTypes() {
        return _outputDeviceTypes;
    }

    public ObservableList<OutputDevice> getOutputDevices() {
        return _outputDevices;
    }

    public void addFunctionCodes(List<Integer> functionCodeList) {
        functionCodeList.add(FunctionCode.OutputDevice);
    }

    public void onShown() {
        OutputDeviceRequest request = new OutputDeviceRequest(CDEFAction.List);
        _requests.add(new OutstandingRequest<>(request.getTransactionNumber(), 0));
        Event event = new Event(EventType.SendMessage, request);
        _eventQueue.add(event);
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        _eventQueue = eventQueue;
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        switch (event.getType()) {
            case ReceiveMessage:
                Message message = event.getMessage();
                if (message.getFunctionCode() == FunctionCode.Notify) {
                    handleNotifyResponse((NotifyResponse) message);
                } else if (message.getFunctionCode() == FunctionCode.OutputDevice) {
                    handleOutputDeviceResponse((OutputDeviceResponse) message);
                }
                break;
        }
        return true;
    }

    private void handleNotifyResponse(NotifyResponse notifyResponse) {
        if (notifyResponse.getNotifyFunctionCode() == FunctionCode.OutputDevice) {
            handleOutputDeviceNotify(notifyResponse.getAction(), notifyResponse.getIds());
        }
    }

    private void handleOutputDeviceNotify(final int action, final List<Integer> ids) {
        switch (action) {
            case CDEFAction.Create:
            case CDEFAction.Update:
                OutputDeviceRequest request = new OutputDeviceRequest(CDEFAction.List);
                request.getIds().addAll(ids);
                _requests.add(new OutstandingRequest<>(request.getTransactionNumber(), 1));
                _eventQueue.add(new Event(EventType.SendMessage, request));
                break;

            case CDEFAction.Delete:
                Platform.runLater(() -> {
                    for (int id : ids) {
                        for (OutputDevice outputDevice : _outputDevices) {
                            if (outputDevice.getId() == id) {
                                _outputDevices.remove(outputDevice);
                                break;
                            }
                        }
                    }
                });
                break;
        }
    }

    private void handleOutputDeviceResponse(OutputDeviceResponse outputDeviceResponse) {
        for (OutstandingRequest<Integer> request : _requests) {
            if (request.transactionNumber == outputDeviceResponse.getTransactionNumber()) {
                if (!outputDeviceResponse.isError()) {
                    if (outputDeviceResponse.getAction() == CDEFAction.List) {
                        final List<OutputDevice> outputDeviceList = outputDeviceResponse.getOutputDevices();
                        Platform.runLater(() -> {
                            int index = -1;
                            for (OutputDevice outputDevice : outputDeviceList) {
                                // Remove the old item from the list if it already exists
                                for (int i = 0; i < _outputDevices.size(); i++) {
                                    OutputDevice tod = _outputDevices.get(i);
                                    if (tod.getId() == outputDevice.getId()) {
                                        index = i;
                                        _outputDevices.remove(tod);
                                        break;
                                    }
                                }
                                // Add the new item to the list
                                if (index != -1) {
                                    _outputDevices.add(index, outputDevice);
                                } else {
                                    _outputDevices.add(outputDevice);
                                }
                            }
                        });
                    }
                }
                _requests.remove(request);
                break;
            }
        }
    }
}
