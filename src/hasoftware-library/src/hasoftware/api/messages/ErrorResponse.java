package hasoftware.api.messages;

import hasoftware.api.AnError;
import hasoftware.api.Message;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class ErrorResponse extends Message {

    private final List<AnError> _errorList;

    public ErrorResponse(int functionCode, int transactionNumber) {
        super(functionCode, transactionNumber, CDEFSystemFlags.Response | CDEFSystemFlags.Error);
        _errorList = new LinkedList<>();
    }

    public ErrorResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _errorList = new LinkedList<>();
        for (int count = cdefMessage.getU8(); count > 0; count--) {
            _errorList.add(new AnError(cdefMessage));
        }
    }

    public void addError(int number, int code, String message) {
        _errorList.add(new AnError(number, code, message));
    }

    public List<AnError> getErrors() {
        return _errorList;
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_errorList.size());
        for (int count = 0; count < _errorList.size(); count++) {
            _errorList.get(count).encode(cdefMessage);
        }
    }
}
