package hasoftware.server;

import hasoftware.api.ErrorCode;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.NotLoggedInException;
import hasoftware.api.Permission;
import hasoftware.api.PermissionException;
import hasoftware.api.classes.CurrentEvent;
import hasoftware.api.classes.ErrorMessage;
import hasoftware.api.classes.InputMessage;
import hasoftware.api.classes.Location;
import hasoftware.api.classes.OutputDevice;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.classes.Point;
import hasoftware.api.messages.CurrentEventRequest;
import hasoftware.api.messages.CurrentEventResponse;
import hasoftware.api.messages.ErrorResponse;
import hasoftware.api.messages.HeartbeatRequest;
import hasoftware.api.messages.HeartbeatResponse;
import hasoftware.api.messages.InputMessageRequest;
import hasoftware.api.messages.InputMessageResponse;
import hasoftware.api.messages.LocationRequest;
import hasoftware.api.messages.LocationResponse;
import hasoftware.api.messages.LoginRequest;
import hasoftware.api.messages.LoginResponse;
import hasoftware.api.messages.NotifyRequest;
import hasoftware.api.messages.OutputDeviceRequest;
import hasoftware.api.messages.OutputDeviceResponse;
import hasoftware.api.messages.OutputMessageRequest;
import hasoftware.api.messages.OutputMessageResponse;
import hasoftware.api.messages.PointRequest;
import hasoftware.api.messages.PointResponse;
import hasoftware.cdef.CDEFAction;
import hasoftware.server.data.ActiveEvent;
import hasoftware.server.data.DataManager;
import hasoftware.server.data.Device;
import hasoftware.server.data.DeviceType;
import hasoftware.server.data.InputEvent;
import hasoftware.server.data.Node;
import hasoftware.server.data.OutputEvent;
import hasoftware.server.data.User;
import hasoftware.util.StringUtil;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLogic {

    private final static Logger logger = LoggerFactory.getLogger(ServerLogic.class);

    private static final ServerLogic _instance = new ServerLogic();

    public static ServerLogic getInstance() {
        return _instance;
    }

    private final DataManager _dm;

    private ServerLogic() {
        _dm = DataManager.instance();
    }

    public Message process(final IUserContext userContext, final Message request) {
        Message response;
        try {

            if (request.isRequest()) {                                              // Process request messages
                int functionCode = request.getFunctionCode();
                switch (functionCode) {
                    case FunctionCode.Heartbeat:
                        response = handleHeartbeatRequest(userContext, (HeartbeatRequest) request);
                        break;
                    case FunctionCode.Notify:
                        response = handleNotifyRequest(userContext, (NotifyRequest) request);
                        break;
                    case FunctionCode.InputMessage:
                        response = handleInputMessageRequest(userContext, (InputMessageRequest) request);
                        break;
                    case FunctionCode.OutputMessage:
                        response = handleOutputMessageRequest(userContext, (OutputMessageRequest) request);
                        break;
                    case FunctionCode.OutputDevice:
                        response = handleOutputDeviceRequest(userContext, (OutputDeviceRequest) request);
                        break;
                    case FunctionCode.Login:
                        response = handleLoginRequest(userContext, (LoginRequest) request);
                        break;
                    case FunctionCode.Location:
                        response = handleLocationRequest(userContext, (LocationRequest) request);
                        break;
                    case FunctionCode.Point:
                        response = handlePointRequest(userContext, (PointRequest) request);
                        break;
                    case FunctionCode.CurrentEvent:
                        response = handleCurrentEventRequest(userContext, (CurrentEventRequest) request);
                        break;
                    default:
                        response = handleUnknownRequest(userContext, request);
                        break;
                }
            } else {
                response = createErrorResponse(request, 1, ErrorCode.General, "Message is not a request");
            }
        } catch (NotLoggedInException | PermissionException ex) {
            response = createErrorResponse(request, 1, ErrorCode.General, ex.getMessage());
        }
        return response;
    }

    private ErrorResponse createErrorResponse(Message request, int number, int code, String message) {
        ErrorResponse errorResponse = request.createErrorResponse();
        errorResponse.getErrorMessages().add(createErrorMessage(number, code, message));
        return errorResponse;
    }

    private ErrorMessage createErrorMessage(int number, int code, String message) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setCode(code);
        errorMessage.setNumber(number);
        errorMessage.setMessage(message);
        return errorMessage;
    }

    private Message handleHeartbeatRequest(final IUserContext userContext, HeartbeatRequest request) {
        logger.debug("[H:{} TN:{}] RECV HeartbeatRequest", userContext.getTarget().getId(), request.getTransactionNumber());
        HeartbeatResponse response = new HeartbeatResponse(request.getTransactionNumber());
        return response;
    }

    private Message handleLoginRequest(final IUserContext userContext, LoginRequest request) {
        logger.debug("[H:{} TN:{}] RECV LoginRequest", userContext.getTarget().getId(), request.getTransactionNumber());
        // Sanity checks
        String username = request.getUsername();
        String password = request.getPassword();
        if (StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(password)) {
            return createErrorResponse(request, 1, ErrorCode.General, "Invalid message content");
        }
        User user = _dm.getUserByUsernamePassword(username, password);
        if (user == null) {
            return createErrorResponse(request, 1, ErrorCode.General, "Invalid username or password");
        }
        userContext.setUser(user);
        LoginResponse response = new LoginResponse(request.getTransactionNumber());
        return response;
    }

    private void checkLoggedIn(final IUserContext userContext) throws NotLoggedInException {
        if (userContext == null) {
            throw new NotLoggedInException();
        }
    }

    private void checkPermission(final IUserContext userContext, final String permission)
            throws PermissionException {
        if (!_dm.doesUserHavePermission(userContext.getUser(), permission)) {
            throw new PermissionException(permission);
        }
    }

    private Message handleNotifyRequest(final IUserContext userContext, final NotifyRequest request) {
        logger.debug("[H:{} TN:{}] RECV NotifyRequest", userContext.getTarget().getId(), request.getTransactionNumber());
        Notifications.add(userContext.getTarget(), request.getFunctionCodes());
        return null; // No response
    }

    private Message handleCurrentEventRequest(final IUserContext userContext, final CurrentEventRequest request)
            throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV handleCurrentEventRequest ({})", userContext.getTarget().getId(), request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn(userContext);
        CurrentEventResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new ActiveEvents and notify
            case CDEFAction.Create:
                checkPermission(userContext, Permission.CreateActiveEvent);
                response = request.createResponse();
                for (CurrentEvent currentEvent : request.getCurrentEvents()) {
                    Device device = _dm.getDeviceById(currentEvent.getPoint().getId());
                    if (device == null) {
                        // TODO Handle errors
                        logger.error("Create CurrentEvent for unknown Device [{}]", currentEvent.getPoint().getId());
                    } else {
                        ActiveEvent obj = _dm.createActiveEvent(device);
                        ids.add(obj.getId());
                    }
                }
                break;

            // Return all or some CurrentEvents
            case CDEFAction.List:
                checkPermission(userContext, Permission.ListActiveEvent);
                response = new CurrentEventResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.List);
                List<ActiveEvent> activeEvents;
                if (request.getIds().isEmpty()) {
                    activeEvents = _dm.getActiveEvents();
                } else {
                    activeEvents = _dm.getActiveEvents(request.getIds());
                }
                for (ActiveEvent activeEvent : activeEvents) {
                    Device device = activeEvent.getDevice();
                    Point point = internalCreatePoint(device);
                    response.getCurrentEvents().add(internalCreateCurrentEvent(activeEvent, point));
                }
                break;

            // Delete existing ActiveEvents and notify
            case CDEFAction.Delete:
                checkPermission(userContext, Permission.DeleteActiveEvent);
                response = request.createResponse();
                for (Integer id : request.getIds()) {
                    ActiveEvent activeEvent = _dm.getActiveEventById(id);
                    if (activeEvent != null) {
                        ids.add(activeEvent.getId());
                        _dm.deleteActiveEvent(activeEvent);
                    }
                }
                break;
        }
        if (!ids.isEmpty()) {
            Notifications.notify(request.getFunctionCode(), request.getAction(), ids);
        }
        return response;
    }

    private Point internalCreatePoint(Device device) {
        Point point = new Point();
        point.setId(device.getId());
        point.setNodeId(device.getNode().getId());
        point.setName(device.getName());
        point.setAddress(device.getAddress());
        point.setDeviceTypeCode(device.getDeviceType().getCode());
        point.setMessage1(device.getMessage1());
        point.setMessage2(device.getMessage2());
        point.setPriority(device.getPriority());
        point.setCreatedOn(device.getCreatedOn());
        point.setUpdatedOn(device.getUpdatedOn());
        return point;
    }

    private CurrentEvent internalCreateCurrentEvent(ActiveEvent activeEvent, Point point) {
        CurrentEvent currentEvent = new CurrentEvent();
        currentEvent.setId(activeEvent.getId());
        currentEvent.setPoint(point);
        currentEvent.setCreatedOn(activeEvent.getCreatedOn());
        currentEvent.setUpdatedOn(activeEvent.getUpdatedOn());
        return currentEvent;
    }

    private OutputDevice internalCreateOutputDevice(hasoftware.server.data.OutputDevice outputDevice) {
        OutputDevice od = new OutputDevice();
        od.setName(outputDevice.getName());
        od.setDescription(outputDevice.getDescription());
        od.setAddress(outputDevice.getAddress());
        od.setDeviceTypeCode(outputDevice.getDeviceType().getCode());
        od.setSerialNumber(outputDevice.getSerialNumber());
        od.setCreatedOn(outputDevice.getCreatedOn());
        od.setUpdatedOn(outputDevice.getUpdatedOn());
        return od;
    }

    private Location internalCreateLocation(Node node, int parentId) {
        Location location = new Location();
        location.setId(node.getId());
        location.setParentId(parentId);
        location.setName(node.getName());
        location.setCreatedOn(node.getCreatedOn());
        location.setUpdatedOn(node.getUpdatedOn());
        return location;
    }

    private OutputMessage internalCreateOutputMessage(OutputEvent outputEvent) {
        OutputMessage outputMessage = new OutputMessage();
        outputMessage.setId(outputEvent.getId());
        outputMessage.setDeviceTypeCode(outputEvent.getDeviceType().getCode());
        outputMessage.setData(outputEvent.getData());
        outputMessage.setCreatedOn(outputEvent.getCreatedOn());
        return outputMessage;
    }

    private InputMessage internalCreateInputMessage(InputEvent inputEvent) {
        InputMessage inputMessage = new InputMessage();
        inputMessage.setId(inputEvent.getId());
        inputMessage.setDeviceTypeCode(inputEvent.getDeviceType().getCode());
        inputMessage.setData(inputEvent.getData());
        inputMessage.setCreatedOn(inputEvent.getCreatedOn());
        return inputMessage;
    }

    private Message handlePointRequest(final IUserContext userContext, final PointRequest request)
            throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV handlePointRequest ({})", userContext.getTarget().getId(), request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn(userContext);
        PointResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            case CDEFAction.List:
                checkPermission(userContext, Permission.ListPoint);
                response = request.createResponse();
                if (request.getNodeId() != 0) {
                    Node node = _dm.getNodeById(request.getNodeId());
                    List<Device> devices = _dm.getDeviceByNode(node);
                    for (Device device : devices) {
                        response.getPoints().add(internalCreatePoint(device));
                    }
                } else {
                    List<Device> devices = _dm.getDeviceByAddress(request.getAddress());
                    for (Device device : devices) {
                        Point point = internalCreatePoint(device);
                        for (hasoftware.server.data.OutputDevice outputDevice : device.getOutputDevices()) {
                            point.getOutputDevices().add(internalCreateOutputDevice(outputDevice));
                        }
                        response.getPoints().add(point);
                    }
                }

                break;
        }
        if (!ids.isEmpty()) {
            Notifications.notify(request.getFunctionCode(), request.getAction(), ids);
        }
        return response;
    }

    private Message handleLocationRequest(final IUserContext userContext, final LocationRequest request)
            throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV LocationRequest ({})", userContext.getTarget().getId(), request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn(userContext);
        LocationResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            case CDEFAction.List:
                checkPermission(userContext, Permission.ListLocation);
                response = request.createResponse();
                Node parent = null;
                if (request.getParentId() != 0) {
                    parent = _dm.getNodeById(request.getParentId());
                }
                List<Node> nodes = _dm.getNodeByParent(parent);
                for (Node node : nodes) {
                    int parentId = (node.getParent() == null) ? 0 : node.getParent().getId();
                    response.getLocations().add(internalCreateLocation(node, parentId));
                }
                break;
        }
        if (!ids.isEmpty()) {
            Notifications.notify(request.getFunctionCode(), request.getAction(), ids);
        }
        return response;
    }

    private Message handleOutputDeviceRequest(final IUserContext userContext, final OutputDeviceRequest request)
            throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV OutputDeviceRequest ({})", userContext.getTarget().getId(), request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn(userContext);
        OutputDeviceResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new OutputDevices and notify
            case CDEFAction.Create:
                checkPermission(userContext, Permission.CreateOutputDevice);
                response = request.createResponse();
                for (OutputDevice outputDevice : request.getOutputDevices()) {
                    DeviceType deviceType = _dm.getDeviceTypeByCode(outputDevice.getDeviceTypeCode());
                    if (deviceType == null) {
                        // TODO Handle errors
                        logger.error("Create OutputDevice for unknown DeviceType [{}]", outputDevice.getDeviceTypeCode());
                    } else {
                        hasoftware.server.data.OutputDevice obj = _dm.createOutputDevice(
                                outputDevice.getName(),
                                outputDevice.getDescription(),
                                outputDevice.getAddress(),
                                deviceType,
                                outputDevice.getSerialNumber());
                        ids.add(obj.getId());
                    }
                }
                break;

            case CDEFAction.Update:
                checkPermission(userContext, Permission.UpdateOutputDevice);
                response = request.createResponse();
                for (OutputDevice outputDevice : request.getOutputDevices()) {
                    DeviceType dt = _dm.getDeviceTypeByCode(outputDevice.getDeviceTypeCode());
                    if (dt == null) {
                        // TODO Handle errors
                        logger.error("Update OutputDevice for unknown DeviceType [{}]", outputDevice.getDeviceTypeCode());
                    } else {
                        hasoftware.server.data.OutputDevice obj = _dm.updateOutputDevice(
                                outputDevice.getId(),
                                outputDevice.getName(),
                                outputDevice.getDescription(),
                                outputDevice.getAddress(),
                                dt,
                                outputDevice.getSerialNumber());
                        if (obj == null) {
                            // TODO Handle errors
                        } else {
                            ids.add(obj.getId());
                        }
                    }
                }
                break;

            // Return all or some OutputEvents
            case CDEFAction.List:
                checkPermission(userContext, Permission.ListOutputDevice);
                response = request.createResponse();
                List<hasoftware.server.data.OutputDevice> outputDevices;
                if (request.getIds().isEmpty()) {
                    outputDevices = _dm.getOutputDevices();
                } else {
                    outputDevices = _dm.getOutputDevices(request.getIds());
                }
                for (hasoftware.server.data.OutputDevice outputDevice : outputDevices) {
                    response.getOutputDevices().add(internalCreateOutputDevice(outputDevice));
                }
                break;

            // Delete existing InputEvents and notify
            case CDEFAction.Delete:
                checkPermission(userContext, Permission.DeleteOutputDevice);
                response = request.createResponse();
                for (Integer id : request.getIds()) {
                    hasoftware.server.data.OutputDevice outputDevice = _dm.getOutputDeviceById(id);
                    if (outputDevice != null) {
                        ids.add(outputDevice.getId());
                        _dm.deleteOutputDevice(outputDevice);
                    }
                }
                break;
        }
        if (!ids.isEmpty()) {
            Notifications.notify(request.getFunctionCode(), request.getAction(), ids);
        }
        return response;
    }

    private Message handleOutputMessageRequest(final IUserContext userContext, final OutputMessageRequest request)
            throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV OutputMessageRequest ({})", userContext.getTarget().getId(), request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn(userContext);
        OutputMessageResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new OutputEvents and notify
            case CDEFAction.Create:
                checkPermission(userContext, Permission.CreateOutputMessage);
                response = new OutputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.Create);
                for (OutputMessage outputMessage : request.getOutputMessages()) {
                    DeviceType deviceType = _dm.getDeviceTypeByCode(outputMessage.getDeviceTypeCode());
                    if (deviceType == null) {
                        // TODO Handle errors
                        logger.error("Create OutputEvent for unknown DeviceType [{}]", outputMessage.getDeviceTypeCode());
                    } else {
                        OutputEvent outputEvent = _dm.createOutputEvent(deviceType, outputMessage.getData());
                        ids.add(outputEvent.getId());
                    }
                }
                break;

            // Return all or some OutputEvents
            case CDEFAction.List:
                checkPermission(userContext, Permission.ListOutputMessage);
                response = new OutputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.List);
                List<OutputEvent> outputEvents;
                if (request.getIds().isEmpty()) {
                    outputEvents = _dm.getOutputEvents();
                } else {
                    outputEvents = _dm.getOutputEvents(request.getIds());
                }
                for (OutputEvent outputEvent : outputEvents) {
                    response.getOutputMessages().add(internalCreateOutputMessage(outputEvent));
                }
                break;

            // Delete existing InputEvents and notify
            case CDEFAction.Delete:
                checkPermission(userContext, Permission.DeleteOutputMessage);
                response = new OutputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.Delete);
                for (Integer id : request.getIds()) {
                    OutputEvent outputEvent = _dm.getOutputEventById(id);
                    if (outputEvent != null) {
                        ids.add(outputEvent.getId());
                        _dm.deleteOutputEvent(outputEvent);
                    }
                }
                break;
        }
        if (!ids.isEmpty()) {
            Notifications.notify(request.getFunctionCode(), request.getAction(), ids);
        }
        return response;
    }

    private Message handleInputMessageRequest(final IUserContext userContext, final InputMessageRequest request)
            throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV InputMessageRequest ({})", userContext.getTarget().getId(), request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn(userContext);
        InputMessageResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new InputEvents and notify
            case CDEFAction.Create:
                checkPermission(userContext, Permission.CreateInputMessage);
                response = new InputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.Create);
                for (InputMessage inputMessage : request.getInputMessages()) {
                    DeviceType deviceType = _dm.getDeviceTypeByCode(inputMessage.getDeviceTypeCode());
                    if (deviceType == null) {
                        // TODO Handle errors
                        logger.error("Create InputEvent for unknown DeviceType [{}]", inputMessage.getDeviceTypeCode());
                    }
                    InputEvent inputEvent = _dm.createInputEvent(deviceType, inputMessage.getData());
                    ids.add(inputEvent.getId());
                }
                break;

            // Return all or some InputEvents
            case CDEFAction.List:
                checkPermission(userContext, Permission.ListInputMessage);
                response = new InputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.List);
                List<InputEvent> inputEvents;
                if (request.getIds().isEmpty()) {
                    inputEvents = _dm.getInputEvents();
                } else {
                    inputEvents = _dm.getInputEvents(request.getIds());
                }
                for (InputEvent inputEvent : inputEvents) {
                    response.getInputMessages().add(internalCreateInputMessage(inputEvent));
                }
                break;

            // Delete existing InputEvents and notify
            case CDEFAction.Delete:
                checkPermission(userContext, Permission.DeleteInputMessage);
                response = new InputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.Delete);
                for (Integer id : request.getIds()) {
                    InputEvent inputEvent = _dm.getInputEventById(id);
                    if (inputEvent != null) {
                        ids.add(inputEvent.getId());
                        _dm.deleteInputEvent(inputEvent);
                    }
                }
                break;
        }
        if (!ids.isEmpty()) {
            Notifications.notify(request.getFunctionCode(), request.getAction(), ids);
        }
        return response;
    }

    private Message handleUnknownRequest(final IUserContext userContext, final Message request) {
        logger.debug("[H:{} TN:{}] RECV UnknownRequest[{}]", userContext.getTarget().getId(), request.getTransactionNumber(), request.getFunctionCode());
        return null;
    }
}
