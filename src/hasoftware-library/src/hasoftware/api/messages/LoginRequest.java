package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.cdef.CDEFMessage;

public class LoginRequest extends Message {

    private String _username;
    private String _password;

    public LoginRequest() {
        super(FunctionCode.Login, 0);
    }

    public LoginRequest(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _username = cdefMessage.getAsciiL();
        _password = cdefMessage.getAsciiL();
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putAsciiL(_username);
        cdefMessage.putAsciiL(_password);
    }

    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }
}
