package hasoftware.api;

import hasoftware.cdef.CDEFMessage;

public class AnError {

    private final int _number;
    private final int _code;
    private final String _message;

    public AnError(int number, int code, String message) {
        _number = number;
        _code = code;
        _message = message;
    }

    public AnError(CDEFMessage cdefMessage) {
        _number = cdefMessage.getU8();
        _code = cdefMessage.getU32();
        _message = cdefMessage.getAsciiL();
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.putU8(_number);
        cdefMessage.putU32(_code);
        cdefMessage.putAsciiL(_message);
    }

    public int getNumber() {
        return _number;
    }

    public int getCode() {
        return _code;
    }

    public String getMessage() {
        return _message;
    }
}
