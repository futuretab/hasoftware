package hasoftware.kirk.messages;

public class CCFPAck2 extends CCFPMessage {

    private final byte _sequenceNumber;

    public CCFPAck2(byte sequenceNumber) {
        super();
        _sequenceNumber = sequenceNumber;
    }

    public byte getSequenceNumber() {
        return _sequenceNumber;
    }
}
