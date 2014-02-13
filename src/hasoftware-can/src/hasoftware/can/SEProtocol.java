package hasoftware.can;

import hasoftware.comms.IPort;
import hasoftware.comms.IPortReader;
import hasoftware.comms.Message;
import hasoftware.comms.PortFactory;
import hasoftware.comms.Serial;
import hasoftware.thread.Notifier;
import hasoftware.util.CommandResult;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SEProtocol implements IPortReader {

    private final static Logger logger = LoggerFactory.getLogger(SEProtocol.class);

    private final int RxBufferSize = 32;
    private final long DefaultTimeout = 2000;

    private IPort _port;
    private int _rxOffset;
    private byte _rxCrc;
    private boolean _rxDleFlag;
    private byte[] _rxBuffer;
    private int _txOffset;
    private byte _txCrc;
    private byte[] _txBuffer;
    private Notifier<ICANMessageReceiver> _messageNotifier;
    private CommandResult _commandResult;
    private boolean _debugRx;
    private boolean _debugTx;
    private final String _portDescription;

    public SEProtocol(ExecutorService executorService, String portDescription) {
        _portDescription = portDescription;
        _messageNotifier = new Notifier<>(executorService, ICANMessageReceiver.class);
        _commandResult = new CommandResult();
        _rxBuffer = new byte[RxBufferSize];
        _txBuffer = new byte[RxBufferSize];
        _debugRx = true;
        _debugTx = true;
    }

    public synchronized void addMessageReceiver(ICANMessageReceiver messageReceiver) {
        _messageNotifier.add(messageReceiver);
    }

    public synchronized void removeMessageReceiver(ICANMessageReceiver messageReceiver) {
        _messageNotifier.remove(messageReceiver);
    }

    public boolean open() {
        try {
            _port = PortFactory.getPort(_portDescription);
            _port.setPortReader((IPortReader) this);
            return true;
        } catch (IllegalArgumentException ex) {
            logger.debug("Failed to open port {} - {}", _portDescription, ex.getMessage());
        }
        return false;
    }

    public void close() {
        _messageNotifier = null;
        _port.close();
        _port = null;
    }

    public boolean goOnBus() {
        Message message = new Message();
        message.addByte((byte) Command.GoOnline);
        return send(message);
    }

    public boolean goOffBus() {
        Message message = new Message();
        message.addByte((byte) Command.GoOffline);
        return send(message);
    }

    public void sendTimestamp(int gatewayId) {
        Calendar cal = Calendar.getInstance();
        byte[] timestampData = new byte[8];
        timestampData[0] = (byte) ((cal.get(Calendar.YEAR) >> 8) & 0xFF);
        timestampData[1] = (byte) (cal.get(Calendar.YEAR) & 0xFF);
        timestampData[2] = (byte) (cal.get(Calendar.MONTH) + 1);
        timestampData[3] = (byte) (cal.get(Calendar.DATE));
        timestampData[4] = (byte) (cal.get(Calendar.HOUR_OF_DAY));
        timestampData[5] = (byte) (cal.get(Calendar.MINUTE));
        timestampData[6] = (byte) (cal.get(Calendar.SECOND));
        send(new CANMessage(gatewayId, CANMessageId.Timestamp, 7, timestampData));
    }

    public boolean send(CANMessage canMessage) {
        Message message = new Message();
        message.addByte((byte) Command.SendMessage);
        message.addByte((byte) (canMessage.getId() >> 8));
        message.addByte((byte) (canMessage.getId() & 0xFF));
        message.addByte((byte) (canMessage.getLength()));
        for (int i = 0; i < canMessage.getLength(); i++) {
            message.addByte(canMessage.getData()[i]);
        }
        return send(message);
    }

    public boolean send(Message message) {
        _txOffset = 0;
        _txCrc = 0;
        _txBuffer[_txOffset++] = Serial.Stx;
        for (int i = 0; i < message.getLength(); i++) {
            byte d = message.getByte(i);
            _txCrc = (byte) (_txCrc ^ d);
            if (d == Serial.Stx || d == Serial.Etx || d == Serial.Dle) {
                _txBuffer[_txOffset++] = Serial.Dle;
                d = (byte) (d + Serial.Dle);
            }
            _txBuffer[_txOffset++] = d;
        }
        if (_txCrc == Serial.Stx || _txCrc == Serial.Etx || _txCrc == Serial.Dle) {
            _txBuffer[_txOffset++] = Serial.Dle;
            _txCrc = (byte) (_txCrc + Serial.Dle);
        }
        _txBuffer[_txOffset++] = _txCrc;
        _txBuffer[_txOffset++] = Serial.Etx;
        if (_debugTx) {
            StringBuilder sb = new StringBuilder("SEND [ ");
            for (int t = 0; t < _txOffset; t++) {
                sb.append(String.format("%02X ", _txBuffer[t]));
            }
            sb.append("]");
            logger.debug(sb.toString());
        }
        synchronized (_commandResult) {
            if (_port.write(_txBuffer, 0, _txOffset) == true) {
                try {
                    _commandResult.wait(DefaultTimeout);
                    return _commandResult.result;
                } catch (InterruptedException ex) {
                    logger.error("send interrupted " + ex.getMessage());
                    return false;
                }
            }
            return false;
        }
    }

    @Override
    public void onReceive(byte[] buffer, int len) {
        for (int i = 0; i < len; i++) {
            byte b = buffer[i];
            switch (b) {
                case Serial.Stx:
                    _rxOffset = 0;
                    _rxCrc = 0;
                    break;

                case Serial.Etx:
                    _rxOffset--;
                    if (_rxCrc == 0) {
                        if (_debugRx) {
                            StringBuilder sb = new StringBuilder("RECV [ ");
                            for (int r = 0; r < _rxOffset; r++) {
                                sb.append(String.format("%02X ", _rxBuffer[r]));
                            }
                            sb.append("]");
                            logger.debug(sb.toString());
                        }
                        switch (_rxBuffer[0]) {
                            case Command.Status:
                                break;
                            case Command.ReceivedMessage:
                                int gatewayId = (_rxBuffer[1] << 8) | _rxBuffer[2];
                                int id = (_rxBuffer[3] << 8) | _rxBuffer[4];
                                int length = _rxBuffer[5];
                                byte[] data = new byte[length];
                                for (int d = 0; d < length; d++) {
                                    data[d] = _rxBuffer[6 + d];
                                }
                                _messageNotifier.getProxy().onReceive(new CANMessage(gatewayId, id, length, data));
                                break;
                            case Command.Ok:
                            case Command.Error:
                                synchronized (_commandResult) {
                                    _commandResult.result = (_rxBuffer[0] == Command.Ok);
                                    _commandResult.notifyAll();
                                }
                                break;
                        }
                    } else {
                        logger.error("RECV CRC ERROR [TODO]");
                    }
                    break;

                case Serial.Dle:
                    _rxDleFlag = true;
                    break;

                default:
                    if (_rxDleFlag) {
                        _rxDleFlag = false;
                        b = (byte) (b - Serial.Dle);
                    }
                    _rxCrc = (byte) (_rxCrc ^ b);
                    _rxBuffer[_rxOffset++] = b;
                    if (_rxOffset == RxBufferSize) {
                        _rxOffset = 0;
                    }
                    break;
            }
        }
    }
}
