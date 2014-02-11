package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;

public class PointRequest extends Message {

    private int _action;
    private int _nodeId;
    private String _address;

    public PointRequest() {
        this(CDEFAction.None);
    }

    public PointRequest(int action) {
        super(FunctionCode.Point, 0);
        _action = action;
        _address = null;
    }

    public PointRequest(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        _nodeId = cdefMessage.getU32();
        _address = cdefMessage.getAsciiL();
    }

    public PointResponse createResponse() {
        PointResponse response = new PointResponse(getTransactionNumber(), _action);
        return response;
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU32(_nodeId);
        cdefMessage.putAsciiL(_address);
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public int getNodeId() {
        return _nodeId;
    }

    public void setNodeId(int nodeId) {
        _nodeId = nodeId;
    }

    public String getAddress() {
        return _address;
    }

    public void setAddress(String address) {
        _address = address;
    }
}
