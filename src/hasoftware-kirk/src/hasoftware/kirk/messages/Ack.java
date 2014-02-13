package hasoftware.kirk.messages;

import hasoftware.comms.IPort;
import hasoftware.comms.Serial;
import hasoftware.kirk.KirkProtocol;

public class Ack {

    private final byte[] _buffer;

    public Ack() {
        _buffer = new byte[2];
        _buffer[0] = 0;
        _buffer[1] = Serial.Ack;
    }

    public void setId(byte id) {
        _buffer[0] = id;
    }

    public boolean send(IPort port) {
        KirkProtocol.debugBuffer("TX: ACK", _buffer, 2);
        return port.write(_buffer, 0, 2);
    }
}
