package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.CurrentEvent;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class CurrentEventResponse extends Message {

    private int _action;
    private final List<CurrentEvent> _currentEvents = new LinkedList<>();

    public CurrentEventResponse(int transactionNumber) {
        super(FunctionCode.CurrentEvent, transactionNumber, CDEFSystemFlags.Response);
        _action = CDEFAction.None;
    }

    public CurrentEventResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countInputMessages = cdefMessage.getU8();
        for (int index = 0; index < countInputMessages; index++) {
            _currentEvents.add(new CurrentEvent(cdefMessage));
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_currentEvents.size());
        for (CurrentEvent currentEvent : _currentEvents) {
            currentEvent.encode(cdefMessage);
        }
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public List<CurrentEvent> getCurrentEvents() {
        return _currentEvents;
    }
}
