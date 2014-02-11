package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.OutputMessage;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class OutputMessageResponse extends Message {

    private int _action;
    private final List<OutputMessage> _outputMessages = new LinkedList<>();

    public OutputMessageResponse(int transactionNumber) {
        super(FunctionCode.OutputMessage, transactionNumber, CDEFSystemFlags.Response);
        _action = CDEFAction.None;
    }

    public OutputMessageResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countOutputMessages = cdefMessage.getU8();
        for (int index = 0; index < countOutputMessages; index++) {
            _outputMessages.add(new OutputMessage(cdefMessage));
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_outputMessages.size());
        for (OutputMessage outputMessage : _outputMessages) {
            outputMessage.encode(cdefMessage);
        }
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public List<OutputMessage> getOutputMessages() {
        return _outputMessages;
    }
}
