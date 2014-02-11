package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;

public class LoginResponse extends Message {

    public LoginResponse(int transactionNumber) {
        super(FunctionCode.Login, transactionNumber, CDEFSystemFlags.Response);
    }

    public LoginResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
    }
}
