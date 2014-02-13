package hasoftware.kirk.messages;

import hasoftware.comms.IPort;
import hasoftware.comms.Serial;
import hasoftware.kirk.KirkProtocol;

public class Enquire {

    private final byte[] _buffer;

    public Enquire(int id) {
        _buffer = new byte[2];
        _buffer[0] = Serial.Enq;
        _buffer[1] = (byte) id;
    }

    public boolean send(IPort port) {
        if (_buffer[1] == 0x32) {
            KirkProtocol.debugBuffer("TX: ENQ", _buffer, 2);
        }
        return port.write(_buffer, 0, 2);
    }
}
