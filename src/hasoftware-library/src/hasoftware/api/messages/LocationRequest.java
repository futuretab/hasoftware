package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;

public class LocationRequest extends Message {

    private int _action;
    private int _parentId;

    public LocationRequest() {
        this(CDEFAction.None);
    }

    public LocationRequest(int action) {
        super(FunctionCode.Location, 0);
        _action = action;
    }

    public LocationRequest(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        _parentId = cdefMessage.getU32();
    }

    public LocationResponse createResponse() {
        LocationResponse response = new LocationResponse(getTransactionNumber(), _action);
        return response;
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU32(_parentId);
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public int getParentId() {
        return _parentId;
    }

    public void setParentId(int parentId) {
        _parentId = parentId;
    }
}
