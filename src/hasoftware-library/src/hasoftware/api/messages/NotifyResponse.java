package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.cdef.CDEFFunctionCode;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class NotifyResponse extends Message {

    private int _notifyFunctionCode;
    private int _action;
    private final List<Integer> _ids = new LinkedList<>();

    public NotifyResponse() {
        super(FunctionCode.Notify, 0, CDEFSystemFlags.Response);
        _notifyFunctionCode = CDEFFunctionCode.None;
    }

    public NotifyResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _notifyFunctionCode = cdefMessage.getU16();
        _action = cdefMessage.getU8();
        int countIds = cdefMessage.getU8();
        for (int index = 0; index < countIds; index++) {
            _ids.add(cdefMessage.getU32());
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU16(_notifyFunctionCode);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_ids.size());
        for (Integer id : _ids) {
            cdefMessage.putU32(id);
        }
    }

    public int getNotifyFunctionCode() {
        return _notifyFunctionCode;
    }

    public void setNotifyFunctionCode(int notifyFunctionCode) {
        _notifyFunctionCode = notifyFunctionCode;
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
}
