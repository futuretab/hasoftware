package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.cdef.CDEFMessage;
import java.util.ArrayList;
import java.util.List;

public class NotifyRequest extends Message {

    private final List<Integer> _functionCodes = new ArrayList<>();

    public NotifyRequest() {
        super(FunctionCode.Notify, 0, 0);
    }

    public NotifyRequest(CDEFMessage cdefMessage) {
        super(cdefMessage);
        int count = cdefMessage.getU8();
        for (int i = 0; i < count; i++) {
            int functionCode = cdefMessage.getU16();
            _functionCodes.add(functionCode);
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_functionCodes.size());
        for (int count = 0; count < _functionCodes.size(); count++) {
            cdefMessage.putU16(_functionCodes.get(count));
        }
    }

    public List<Integer> getFunctionCodes() {
        return _functionCodes;
    }
}
