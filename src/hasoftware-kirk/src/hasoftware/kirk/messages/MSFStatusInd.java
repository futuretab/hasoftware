package hasoftware.kirk.messages;

public class MSFStatusInd extends CCFPMessage {

    private final byte _sequenceNumber;
    private final byte _event;
    private final byte _subEvent;
    private String _localNo;
    private final byte _statusType;
    private final byte[] _statusBytes;

    public MSFStatusInd(byte[] buffer) {
        super();
        // 00 21 38 05 31 32 00 0B 02 10 1E CE
        int index = 0;
        _sequenceNumber = buffer[index++];
        _event = buffer[index++];
        _subEvent = buffer[index++];
        int length = buffer[index++];
        _localNo = "";
        while (buffer[index] != 0x00) {
            _localNo = _localNo + (char) (buffer[index++]);
        }
        index++;
        _statusType = buffer[index++];
        int remaining = (length + 4 - index);
        _statusBytes = new byte[remaining];
        for (int i = 0; i < remaining; i++) {
            _statusBytes[i] = buffer[index++];
        }

    }

    public byte getSequenceNumber() {
        return _sequenceNumber;
    }

    public String getLocalNo() {
        return _localNo;
    }

    public byte getStatusType() {
        return _statusType;
    }

    public byte[] getStatusBytes() {
        return _statusBytes;
    }
}
