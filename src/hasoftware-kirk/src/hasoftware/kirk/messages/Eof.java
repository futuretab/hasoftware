package hasoftware.kirk.messages;

import hasoftware.comms.IPort;
import hasoftware.comms.Serial;
import hasoftware.kirk.KirkProtocol;

public class Eof {

    private final byte[] _buffer;

    public Eof() {
        _buffer = new byte[3];
        _buffer[0] = Serial.Dle;
        _buffer[1] = Serial.Etb;
        _buffer[2] = 0x00;
    }

    public boolean send(IPort port) {
        KirkProtocol.debugBuffer("TX: EOF", _buffer, 3);
        return port.write(_buffer, 0, 3);
    }
}
