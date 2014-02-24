package hasoftware.api;

import hasoftware.api.classes.CurrentEvent;
import hasoftware.api.classes.OutputDevice;
import hasoftware.api.classes.Point;
import hasoftware.api.messages.CurrentEventRequest;
import hasoftware.api.messages.CurrentEventResponse;
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

    public static boolean JavaFxApplication = false;

    private static final LocalModel _instance = new LocalModel();

    public static LocalModel getInstance() {
        return _instance;
    }

    private LinkedBlockingQueue<Event> _eventQueue;
    private final ObservableList<String> _outputDeviceTypes;
    private final ObservableList<OutputDevice> _outputDevices;
    private final ObservableList<CurrentEvent> _currentEvents;
    private final LinkedList<OutstandingRequest<Integer>> _requests;

    private LocalModel() {
        _outputDeviceTypes = FXCollections.observableArrayList();
        _outputDeviceTypes.addAll(
                DeviceType.KIRKDECT.getCode(),
                DeviceType.SMS.getCode(),
                DeviceType.EMAIL.getCode(),
                DeviceType.ANDROID.getCode());

        _outputDevices = FXCollections.observableArrayList();

        _currentEvents = FXCollections.observableArrayList();

        _requests = new LinkedList<>();
    }

    private void runLater(Runnable runnable) {
        if (JavaFxApplication) {
            Platform.runLater(runnable);
        } else {
            runnable.run();
        }
    }

    public ObservableList<String> getOutputDeviceTypes() {
        return _outputDeviceTypes;
    }

    public ObservableList<OutputDevice> getOutputDevices() {
        return _outputDevices;
    }

    public ObservableList<CurrentEvent> getCurrentEvents() {
        return _currentEvents;
    }

    public void addFunctionCodes(List<Integer> functionCodeList) {
        functionCodeList.add(FunctionCode.OutputDevice);
        functionCodeList.add(FunctionCode.CurrentEvent);
    }

    public CurrentEvent getCurrentEventByPoint(Point point) {
        for (CurrentEvent currentEvent : _currentEvents) {
            if (currentEvent.getPointId() == point.getId()) {
                return currentEvent;
            }
        }
        return null;
    }

    public void createCurrentEvent(Point point) {
        if (point == null) {
            throw new IllegalArgumentException("point can not be null");
        }
        // Should really add it to _currentEvents in some eager caching fashion?
        CurrentEventRequest currentEventRequest = new CurrentEventRequest(CDEFAction.Create);
        currentEventRequest.getCurrentEvents().add(new CurrentEvent(0, point.getId(), null, null));
        _eventQueue.add(new Event(EventType.SendMessage, currentEventRequest));
    }

    public void deleteCurrentEvent(CurrentEvent currentEvent) {
        if (currentEvent == null) {
            throw new IllegalArgumentException("currentEvent can not be null");
        }
        // Should really delete it from _currentEvents in some eager caching fashion?
        CurrentEventRequest currentEventRequest = new CurrentEventRequest(CDEFAction.Delete);
        currentEventRequest.getIds().add(currentEvent.getId());
        _eventQueue.add(new Event(EventType.SendMessage, currentEventRequest));
    }

    public void onShown() {
        {
            OutputDeviceRequest request = new OutputDeviceRequest(CDEFAction.List);
            _requests.add(new OutstandingRequest<>(request.getTransactionNumber(), 0));
            Event event = new Event(EventType.SendMessage, request);
            _eventQueue.add(event);
        }

        {
            CurrentEventRequest request = new CurrentEventRequest(CDEFAction.List);
            _requests.add(new OutstandingRequest<>(request.getTransactionNumber(), 0));
            Event event = new Event(EventType.SendMessage, request);
            _eventQueue.add(event);
        }
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
                if (!message.isError()) {
                    if (message.getFunctionCode() == FunctionCode.Notify) {
                        handleNotifyResponse((NotifyResponse) message);
                    } else if (message.getFunctionCode() == FunctionCode.OutputDevice) {
                        handleOutputDeviceResponse((OutputDeviceResponse) message);
                    } else if (message.getFunctionCode() == FunctionCode.CurrentEvent) {
                        handleCurrentEventResponse((CurrentEventResponse) message);
                    }
                }
                break;
        }
        return true;
    }

    private void handleNotifyResponse(NotifyResponse notifyResponse) {
        if (notifyResponse.getNotifyFunctionCode() == FunctionCode.OutputDevice) {
            handleOutputDeviceNotify(notifyResponse.getAction(), notifyResponse.getIds());
        } else if (notifyResponse.getNotifyFunctionCode() == FunctionCode.CurrentEvent) {
            handleCurrentEventNotify(notifyResponse.getAction(), notifyResponse.getIds());
        }
    }

    private void handleCurrentEventNotify(final int action, final List<Integer> ids) {
        switch (action) {
            case CDEFAction.Create:
            case CDEFAction.Update:
                CurrentEventRequest request = new CurrentEventRequest(CDEFAction.List);
                request.getIds().addAll(ids);
                _requests.add(new OutstandingRequest<>(request.getTransactionNumber(), 1));
                _eventQueue.add(new Event(EventType.SendMessage, request));
                break;

            case CDEFAction.Delete:
                runLater(() -> {
                    for (int id : ids) {
                        for (CurrentEvent currentEvent : _currentEvents) {
                            if (currentEvent.getId() == id) {
                                _currentEvents.remove(currentEvent);
                                break;
                            }
                        }
                    }
                });
                break;
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
                runLater(() -> {
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

    private void handleCurrentEventResponse(CurrentEventResponse currentEventResponse) {
        for (OutstandingRequest<Integer> request : _requests) {
            if (request.transactionNumber == currentEventResponse.getTransactionNumber()) {
                if (!currentEventResponse.isError()) {
                    if (currentEventResponse.getAction() == CDEFAction.List) {
                        final List<CurrentEvent> currentEventList = currentEventResponse.getCurrentEvents();
                        runLater(() -> {
                            int index = -1;
                            for (CurrentEvent currentEvent : currentEventList) {
                                // Remove the old item from the list if it already exists
                                for (int i = 0; i < _currentEvents.size(); i++) {
                                    CurrentEvent tce = _currentEvents.get(i);
                                    if (tce.getId() == currentEvent.getId()) {
                                        index = i;
                                        _currentEvents.remove(tce);
                                        break;
                                    }
                                }
                                // Add the new item to the list
                                if (index != -1) {
                                    _currentEvents.add(index, currentEvent);
                                } else {
                                    _currentEvents.add(currentEvent);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private void handleOutputDeviceResponse(OutputDeviceResponse outputDeviceResponse) {
        for (OutstandingRequest<Integer> request : _requests) {
            if (request.transactionNumber == outputDeviceResponse.getTransactionNumber()) {
                if (!outputDeviceResponse.isError()) {
                    if (outputDeviceResponse.getAction() == CDEFAction.List) {
                        final List<OutputDevice> outputDeviceList = outputDeviceResponse.getOutputDevices();
                        runLater(() -> {
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
