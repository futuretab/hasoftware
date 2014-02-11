package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.InputMessage;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;
import java.util.LinkedList;
import java.util.List;

public class InputMessageRequest extends Message {

    private int _action;
    private final List<Integer> _ids = new LinkedList<>();
    private final List<InputMessage> _inputMessages = new LinkedList<>();

    public InputMessageRequest() {
        this(CDEFAction.None);
    }

    public InputMessageRequest(int action) {
        super(FunctionCode.InputMessage, 0);
        _action = action;
    }

    public InputMessageRequest(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countIds = cdefMessage.getU8();
        for (int index = 0; index < countIds; index++) {
            _ids.add(cdefMessage.getU32());
        }
        int countInputMessages = cdefMessage.getU8();
        for (int index = 0; index < countInputMessages; index++) {
            _inputMessages.add(new InputMessage(cdefMessage));
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_ids.size());
        for (Integer id : _ids) {
            cdefMessage.putU32(id);
        }
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

    public List<Integer> getIds() {
        return _ids;
    }

    public List<InputMessage> getInputMessages() {
        return _inputMessages;
    }
}
