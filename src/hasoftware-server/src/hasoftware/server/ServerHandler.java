package hasoftware.server;

import hasoftware.api.ErrorCode;
import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.MessageFactory;
import hasoftware.api.NotLoggedInException;
import hasoftware.api.Permission;
import hasoftware.api.PermissionException;
import hasoftware.api.classes.CurrentEvent;
import hasoftware.api.classes.InputMessage;
import hasoftware.api.classes.Location;
import hasoftware.api.classes.OutputDevice;
import hasoftware.api.classes.OutputMessage;
import hasoftware.api.classes.Point;
import hasoftware.api.classes.TimeUTC;
import hasoftware.api.messages.CurrentEventRequest;
import hasoftware.api.messages.CurrentEventResponse;
import hasoftware.api.messages.ErrorResponse;
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
import hasoftware.cdef.CDEFMessage;
import hasoftware.server.data.ActiveEvent;
import hasoftware.server.data.DataManager;
import hasoftware.server.data.Device;
import hasoftware.server.data.DeviceType;
import hasoftware.server.data.InputEvent;
import hasoftware.server.data.Node;
import hasoftware.server.data.OutputEvent;
import hasoftware.server.data.User;
import hasoftware.util.StringUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<CDEFMessage> {

    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private static int HandlerId = 1;

    private final DataManager _dm;
    private ChannelHandlerContext _context;
    private int _handlerId;
    private User _user;

    public ServerHandler() {
        _context = null;
        _handlerId = 0;
        _dm = DataManager.instance();
    }

    public int getHandlerId() {
        return _handlerId;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        _context = context;
        _handlerId = HandlerId++;
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        Notifications.remove(this);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CDEFMessage message) throws Exception {
        Message request = MessageFactory.decode(message);
        Message response = null;
        int transactionNumber = request.getTransactionNumber();
        try {

            if (request.isRequest()) {                                              // Process request messages
                int functionCode = request.getFunctionCode();
                switch (functionCode) {
                    case FunctionCode.Notify:
                        response = handleNotifyRequest((NotifyRequest) request);
                        break;
                    case FunctionCode.InputMessage:
                        response = handleInputMessageRequest((InputMessageRequest) request);
                        break;
                    case FunctionCode.OutputMessage:
                        response = handleOutputMessageRequest((OutputMessageRequest) request);
                        break;
                    case FunctionCode.OutputDevice:
                        response = handleOutputDeviceRequest((OutputDeviceRequest) request);
                        break;
                    case FunctionCode.Login:
                        response = handleLoginRequest((LoginRequest) request);
                        break;
                    case FunctionCode.Location:
                        response = handleLocationRequest((LocationRequest) request);
                        break;
                    case FunctionCode.Point:
                        response = handlePointRequest((PointRequest) request);
                        break;
                    case FunctionCode.CurrentEvent:
                        response = handleCurrentEventRequest((CurrentEventRequest) request);
                        break;
                    default:
                        response = handleUnknownRequest(request);
                        break;
                }
            } else {
                response = createErrorResponse(request, 1, ErrorCode.General, "Message is not a request");
            }
        } catch (NotLoggedInException | PermissionException ex) {
            response = createErrorResponse(request, 1, ErrorCode.General, ex.getMessage());
        }
        if (response != null) {
            response.setTransactionNumber(transactionNumber);                   // Copy transaction numbers
            CDEFMessage cdefMessage = new CDEFMessage();
            response.encode(cdefMessage);                                       // Encode response into a CDEFMessage
            ctx.writeAndFlush(cdefMessage);
        }
    }

    private ErrorResponse createErrorResponse(Message request, int number, int code, String message) {
        ErrorResponse errorResponse = request.createErrorResponse();
        errorResponse.addError(number, code, message);
        return errorResponse;
    }

    public void send(Message message) {
        CDEFMessage cdefMessage = new CDEFMessage();
        message.encode(cdefMessage);                                            // Encode response into a CDEFMessage
        _context.writeAndFlush(cdefMessage);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        context.close();
    }

    private Message handleLoginRequest(LoginRequest request) {
        logger.debug("[H:{} TN:{}] RECV LoginRequest", _handlerId, request.getTransactionNumber());
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
        _user = user;
        LoginResponse response = new LoginResponse(request.getTransactionNumber());
        return response;
    }

    private void checkLoggedIn() throws NotLoggedInException {
        if (_user == null) {
            throw new NotLoggedInException();
        }
    }

    private void checkPermission(String permission) throws PermissionException {
        if (!_dm.doesUserHavePermission(_user, permission)) {
            throw new PermissionException(permission);
        }
    }

    private Message handleNotifyRequest(NotifyRequest request) {
        logger.debug("[H:{} TN:{}] RECV NotifyRequest", _handlerId, request.getTransactionNumber());
        Notifications.add(this, request.getFunctionCodes());
        return null; // No response
    }

    private Message handleCurrentEventRequest(CurrentEventRequest request) throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV handleCurrentEventRequest ({})", _handlerId, request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn();
        CurrentEventResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new ActiveEvents and notify
            case CDEFAction.Create:
                checkPermission(Permission.CreateActiveEvent);
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
                checkPermission(Permission.ListActiveEvent);
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
                    Point point = new Point(device.getId(),
                            device.getNode().getId(),
                            device.getName(),
                            device.getAddress(),
                            device.getDeviceType().getCode(),
                            device.getMessage1(),
                            device.getMessage2(),
                            device.getPriority(),
                            new TimeUTC(device.getCreatedOn()),
                            new TimeUTC(device.getUpdatedOn()));
                    response.getCurrentEvents().add(
                            new CurrentEvent(activeEvent.getId(),
                                    point,
                                    new TimeUTC(activeEvent.getCreatedOn()),
                                    new TimeUTC(activeEvent.getUpdatedOn())));
                }
                break;

            // Delete existing ActiveEvents and notify
            case CDEFAction.Delete:
                checkPermission(Permission.DeleteActiveEvent);
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

    private Message handlePointRequest(PointRequest request) throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV handlePointRequest ({})", _handlerId, request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn();
        PointResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            case CDEFAction.List:
                checkPermission(Permission.ListPoint);
                response = request.createResponse();
                if (request.getNodeId() != 0) {
                    Node node = _dm.getNodeById(request.getNodeId());
                    List<Device> devices = _dm.getDeviceByNode(node);
                    for (Device device : devices) {
                        response.getPoints().add(
                                new Point(device.getId(),
                                        device.getNode().getId(),
                                        device.getName(),
                                        device.getAddress(),
                                        device.getDeviceType().getCode(),
                                        device.getMessage1(),
                                        device.getMessage2(),
                                        device.getPriority(),
                                        new TimeUTC(device.getCreatedOn()),
                                        new TimeUTC(device.getUpdatedOn())));
                    }
                } else {
                    List<Device> devices = _dm.getDeviceByAddress(request.getAddress());
                    for (Device device : devices) {
                        Point point = new Point(device.getId(),
                                device.getNode().getId(),
                                device.getName(),
                                device.getAddress(),
                                device.getDeviceType().getCode(),
                                device.getMessage1(),
                                device.getMessage2(),
                                device.getPriority(),
                                new TimeUTC(device.getCreatedOn()),
                                new TimeUTC(device.getUpdatedOn()));
                        for (hasoftware.server.data.OutputDevice outputDevice : device.getOutputDevices()) {
                            point.getOutputDevices().add(new OutputDevice(outputDevice.getId(),
                                    outputDevice.getName(),
                                    outputDevice.getDescription(),
                                    outputDevice.getAddress(),
                                    outputDevice.getDeviceType().getCode(),
                                    outputDevice.getSerialNumber(),
                                    new TimeUTC(outputDevice.getCreatedOn()),
                                    new TimeUTC(outputDevice.getUpdatedOn())));
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

    private Message handleLocationRequest(LocationRequest request) throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV LocationRequest ({})", _handlerId, request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn();
        LocationResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            case CDEFAction.List:
                checkPermission(Permission.ListLocation);
                response = request.createResponse();
                Node parent = null;
                if (request.getParentId() != 0) {
                    parent = _dm.getNodeById(request.getParentId());
                }
                List<Node> nodes = _dm.getNodeByParent(parent);
                for (Node node : nodes) {
                    int parentId = (node.getParent() == null) ? 0 : node.getParent().getId();
                    response.getLocations().add(
                            new Location(node.getId(),
                                    parentId,
                                    node.getName(),
                                    new TimeUTC(node.getCreatedOn()),
                                    new TimeUTC(node.getUpdatedOn())));
                }
                break;
        }
        if (!ids.isEmpty()) {
            Notifications.notify(request.getFunctionCode(), request.getAction(), ids);
        }
        return response;
    }

    private Message handleOutputDeviceRequest(OutputDeviceRequest request) throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV OutputDeviceRequest ({})", _handlerId, request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn();
        OutputDeviceResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new OutputDevices and notify
            case CDEFAction.Create:
                checkPermission(Permission.CreateOutputDevice);
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
                checkPermission(Permission.UpdateOutputDevice);
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
                checkPermission(Permission.ListOutputDevice);
                response = request.createResponse();
                List<hasoftware.server.data.OutputDevice> outputDevices;
                if (request.getIds().isEmpty()) {
                    outputDevices = _dm.getOutputDevices();
                } else {
                    outputDevices = _dm.getOutputDevices(request.getIds());
                }
                for (hasoftware.server.data.OutputDevice outputDevice : outputDevices) {
                    response.getOutputDevices().add(
                            new OutputDevice(outputDevice.getId(),
                                    outputDevice.getName(),
                                    outputDevice.getDescription(),
                                    outputDevice.getAddress(),
                                    outputDevice.getDeviceType().getCode(),
                                    outputDevice.getSerialNumber(),
                                    new TimeUTC(outputDevice.getCreatedOn()),
                                    new TimeUTC(outputDevice.getUpdatedOn())));
                }
                break;

            // Delete existing InputEvents and notify
            case CDEFAction.Delete:
                checkPermission(Permission.DeleteOutputDevice);
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

    private Message handleOutputMessageRequest(OutputMessageRequest request) throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV OutputMessageRequest ({})", _handlerId, request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn();
        OutputMessageResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new OutputEvents and notify
            case CDEFAction.Create:
                checkPermission(Permission.CreateOutputMessage);
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
                checkPermission(Permission.ListOutputMessage);
                response = new OutputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.List);
                List<OutputEvent> outputEvents;
                if (request.getIds().isEmpty()) {
                    outputEvents = _dm.getOutputEvents();
                } else {
                    outputEvents = _dm.getOutputEvents(request.getIds());
                }
                for (OutputEvent outputEvent : outputEvents) {
                    response.getOutputMessages().add(
                            new OutputMessage(outputEvent.getId(),
                                    outputEvent.getDeviceType().getCode(),
                                    outputEvent.getData(),
                                    new TimeUTC(outputEvent.getCreatedOn())));
                }
                break;

            // Delete existing InputEvents and notify
            case CDEFAction.Delete:
                checkPermission(Permission.DeleteOutputMessage);
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

    private Message handleInputMessageRequest(InputMessageRequest request) throws NotLoggedInException, PermissionException {
        logger.debug("[H:{} TN:{}] RECV InputMessageRequest ({})", _handlerId, request.getTransactionNumber(), CDEFAction.getActionStr(request.getAction()));
        checkLoggedIn();
        InputMessageResponse response = null;
        List<Integer> ids = new LinkedList<>();
        int action = request.getAction();
        switch (action) {
            // Create new InputEvents and notify
            case CDEFAction.Create:
                checkPermission(Permission.CreateInputMessage);
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
                checkPermission(Permission.ListInputMessage);
                response = new InputMessageResponse(request.getTransactionNumber());
                response.setAction(CDEFAction.List);
                List<InputEvent> inputEvents;
                if (request.getIds().isEmpty()) {
                    inputEvents = _dm.getInputEvents();
                } else {
                    inputEvents = _dm.getInputEvents(request.getIds());
                }
                for (InputEvent inputEvent : inputEvents) {
                    response.getInputMessages().add(
                            new InputMessage(inputEvent.getId(),
                                    inputEvent.getDeviceType().getCode(),
                                    inputEvent.getData(),
                                    new TimeUTC(inputEvent.getCreatedOn())));
                }
                break;

            // Delete existing InputEvents and notify
            case CDEFAction.Delete:
                checkPermission(Permission.DeleteInputMessage);
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

    private Message handleUnknownRequest(Message request) {
        logger.debug("[H:{} TN:{}] RECV UnknownRequest[{}]", _handlerId, request.getTransactionNumber(), request.getFunctionCode());
        return null;
    }
}
