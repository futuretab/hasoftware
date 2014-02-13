package hasoftware.kirk.messages;

public class CCFPNak extends CCFPMessage {

    private final byte _nag;

    public CCFPNak(byte nag) {
        super();
        _nag = nag;
    }

    public byte getNag() {
        return _nag;
    }
}
