package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.CurrentEvent;
import hasoftware.cdef.CDEFAction;
import hasoftware.cdef.CDEFMessage;
import java.util.LinkedList;
import java.util.List;

public class CurrentEventRequest extends Message {

    private int _action;
    private final List<Integer> _ids = new LinkedList<>();
    private final List<CurrentEvent> _currentEvents = new LinkedList<>();

    public CurrentEventRequest() {
        this(CDEFAction.None);
    }

    public CurrentEventRequest(int action) {
        super(FunctionCode.CurrentEvent, 0);
        _action = action;
    }

    public CurrentEventRequest(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countIds = cdefMessage.getU8();
        for (int index = 0; index < countIds; index++) {
            _ids.add(cdefMessage.getU32());
        }
        int countICurrentEvents = cdefMessage.getU8();
        for (int index = 0; index < countICurrentEvents; index++) {
            _currentEvents.add(new CurrentEvent(cdefMessage));
        }
    }

    public CurrentEventResponse createResponse() {
        CurrentEventResponse response = new CurrentEventResponse(getTransactionNumber());
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

    public List<Integer> getIds() {
        return _ids;
    }

    public List<CurrentEvent> getCurrentEvents() {
        return _currentEvents;
    }
}
