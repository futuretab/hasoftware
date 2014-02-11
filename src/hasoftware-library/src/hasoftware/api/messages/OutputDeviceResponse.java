package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputDevice;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class OutputDeviceResponse extends Message {

    private int _action;
    private final List<OutputDevice> _outputDevices = new LinkedList<>();

    public OutputDeviceResponse(int transactionNumber) {
        super(FunctionCode.OutputDevice, transactionNumber, CDEFSystemFlags.Response);
        _action = CDEFAction.None;
    }

    public OutputDeviceResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countOutputDevices = cdefMessage.getU8();
        for (int index = 0; index < countOutputDevices; index++) {
            _outputDevices.add(new OutputDevice(cdefMessage));
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
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

    public List<OutputDevice> getOutputDevices() {
        return _outputDevices;
    }
}
