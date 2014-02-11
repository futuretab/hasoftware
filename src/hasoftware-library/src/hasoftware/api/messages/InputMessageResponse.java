package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.InputMessage;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class InputMessageResponse extends Message {

    private int _action;
    private final List<InputMessage> _inputMessages = new LinkedList<>();

    public InputMessageResponse(int transactionNumber) {
        super(FunctionCode.InputMessage, transactionNumber, CDEFSystemFlags.Response);
        _action = CDEFAction.None;
    }

    public InputMessageResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countInputMessages = cdefMessage.getU8();
        for (int index = 0; index < countInputMessages; index++) {
            _inputMessages.add(new InputMessage(cdefMessage));
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_inputMessages.size());
        for (InputMessage inputMessage : _inputMessages) {
            inputMessage.encode(cdefMessage);
        }
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public List<InputMessage> getInputMessages() {
        return _inputMessages;
    }
}
