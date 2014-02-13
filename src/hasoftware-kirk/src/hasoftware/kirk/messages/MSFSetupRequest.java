package hasoftware.kirk.messages;

import hasoftware.comms.IPort;
import hasoftware.comms.Serial;
import hasoftware.kirk.KirkProtocol;

public class MSFSetupRequest extends MSFEvent {

    private final byte _subEvent;
    private byte _setupSpec1;
    private String _localNo;
    private String _message;
    private String _callbackNo;
    private byte _setupSpec2;
    private byte _setupSpec3;
    private byte _alertPattern;
    private byte _alertTone;
    private byte _alertTimeout;
    private byte _displayTimeout;
    private String _dateTime;

    private byte[] _encoded;
    private int _encodedLength;

    public MSFSetupRequest() {
        _subEvent = MSFSubEventType.MSFSetupReq;
    }

    public void setSetupSpec1(int setupSpec1) {
        _setupSpec1 = (byte) setupSpec1;
    }

    public void setLocalNo(String localNo) {
        _localNo = localNo;
    }

    public void setMessage(String message) {
        _message = message;
    }

    public void setCallbackNo(String callbackNo) {
        _callbackNo = callbackNo;
    }

    public void setSetupSpec2(int setupSpec2) {
        _setupSpec2 = (byte) setupSpec2;
    }

    public void setSetupSpec3(int setupSpec3) {
        _setupSpec3 = (byte) setupSpec3;
    }

    public void setAlertPattern(int alertPattern) {
        _alertPattern = (byte) alertPattern;
    }

    public void setAlertTone(int alertTone) {
        _alertTone = (byte) alertTone;
    }

    public void setAlertTimeout(int alertTimeout) {
        _alertTimeout = (byte) alertTimeout;
    }

    public void setDisplayTimeout(int displayTimeout) {
        _displayTimeout = (byte) displayTimeout;
    }

    public void setDateTime(String dateTime) {
        _dateTime = dateTime;
    }

    // <seq> <msf> <setupreq> <len> <ss1>
    //  00    21       20      0F    02    30 00 7C 00 7C 00 0D 0C 00 02 00 00 00 10 1E 95
    public boolean send(IPort port, byte sequenceNumber) {
        if (_encoded == null) {
            int length = 1 + _localNo.length() + _message.length() + _callbackNo.length() + 7 + 7;
            byte buffer[] = new byte[length];
            int index = 0;
            buffer[index++] = sequenceNumber;
            buffer[index++] = _event;
            buffer[index++] = _subEvent;
            buffer[index++] = (byte) (length - 3);
            buffer[index++] = _setupSpec1;
            for (int j = 0; j < _localNo.length(); j++, index++) {
                buffer[index] = (byte) _localNo.charAt(j);
            }
            buffer[index++] = 0;
            for (int j = 0; j < _message.length(); j++, index++) {
                buffer[index] = (byte) _message.charAt(j);
            }
            buffer[index++] = 0;
            for (int j = 0; j < _callbackNo.length(); j++, index++) {
                buffer[index] = (byte) _callbackNo.charAt(j);
            }
            buffer[index++] = 0;
            buffer[index++] = _setupSpec2;
            buffer[index++] = _setupSpec3;
            buffer[index++] = _alertPattern;
            buffer[index++] = _alertTone;
            buffer[index++] = _alertTimeout;
            buffer[index++] = _displayTimeout;
            for (int j = 0; j < _dateTime.length(); j++, index++) {
                buffer[index] = (byte) _dateTime.charAt(j);
            }
            buffer[index++] = 0;
            encodeMessage(buffer);
            KirkProtocol.debugBuffer("TX: MSFSetupReq", _encoded, _encodedLength);
        }
        return port.write(_encoded, 0, _encodedLength);
    }

    private byte[] encodeMessage(byte[] message) {
        _encodedLength = 3;
        for (int i = 0; i < message.length; i++) {
            if (message[i] == Serial.Dle) {
                _encodedLength++;
            }
            _encodedLength++;
        }
        _encoded = new byte[_encodedLength + 1];
        int index = 0;
        int checksum = 0;
        for (int i = 0; i < message.length; i++) {
            checksum = (checksum + message[i]);
            _encoded[index++] = message[i];
            if (message[i] == Serial.Dle) {
                _encoded[index++] = Serial.Dle;
            }
        }
        checksum = checksum & 0xFF;
        _encoded[index++] = Serial.Dle;
        _encoded[index++] = Serial.Rs;
        _encoded[index++] = (byte) checksum;
        if (checksum == Serial.Dle) {
            _encoded[index++] = Serial.Dle;
            _encodedLength++;
        }
        return _encoded;
    }
}
