package hasoftware.kirk.messages;

public class MSFRelInd extends CCFPMessage {

    private final byte _sequenceNumber;
    private final byte _event;
    private final byte _subEvent;
    private String _localNo;
    private final byte _reason;

    public MSFRelInd(byte[] buffer) {
        super();
        // [80 21 26 4 31 32 0 0 10 1e ae]
        int index = 0;
        _sequenceNumber = buffer[index++];
        _event = buffer[index++];
        _subEvent = buffer[index++];
        index++; // length
        _localNo = "";
        while (buffer[index] != 0x00) {
            _localNo = _localNo + (char) (buffer[index++]);
        }
        index++;
        _reason = buffer[index++];
    }

    public byte getSequenceNumber() {
        return _sequenceNumber;
    }

    public String getLocalNo() {
        return _localNo;
    }

    public byte getReason() {
        return _reason;
    }
}
