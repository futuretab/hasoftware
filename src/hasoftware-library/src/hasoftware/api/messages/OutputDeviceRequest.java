package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputDevice;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;
import java.util.LinkedList;
import java.util.List;

public class OutputDeviceRequest extends Message {

    private int _action;
    private final List<Integer> _ids = new LinkedList<>();
    private final List<OutputDevice> _outputDevices = new LinkedList<>();

    public OutputDeviceRequest() {
        this(CDEFAction.None);
    }

    public OutputDeviceRequest(int action) {
        super(FunctionCode.OutputDevice, 0);
        _action = action;
    }

    public OutputDeviceRequest(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countIds = cdefMessage.getU8();
        for (int index = 0; index < countIds; index++) {
            _ids.add(cdefMessage.getU32());
        }
        int countOutputDevices = cdefMessage.getU8();
        for (int index = 0; index < countOutputDevices; index++) {
            _outputDevices.add(new OutputDevice(cdefMessage));
        }
    }

    public OutputDeviceResponse createResponse() {
        OutputDeviceResponse response = new OutputDeviceResponse(getTransactionNumber());
        response.setAction(_action);
        return response;
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_ids.size());
        for (Integer id : _ids) {
            cdefMessage.putU32(id);
        }
        cdefMessage.putU8(_outputDevices.size());
        for (OutputDevice outputDevice : _outputDevices) {
            outputDevice.encode(cdefMessage);
        }
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public List<Integer> getIds() {
        return _ids;
    }

    public List<OutputDevice> getOutputDevices() {
        return _outputDevices;
    }
}
