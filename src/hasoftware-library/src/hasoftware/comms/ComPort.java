package hasoftware.comms;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TooManyListenersException;
import org.slf4j.LoggerFactory;

public class ComPort implements IPort {

    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ComPort.class);

    private final int Timeout = 2000;

    private SerialPort _serialPort;
    private IPortReader _portReader;

    public ComPort(String description) throws IllegalArgumentException {
        // The format of the description is
        //<port name>:<baud rate>:<data bits>:<stop bits>:<parity>:<flow control>
        String[] parts = description.split(":");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid serial port description " + description);
        }
        CommPortIdentifier portIdentifier;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(parts[0]);
        } catch (NoSuchPortException ex) {
            throw new IllegalArgumentException("Invalid serial port " + parts[0]);
        }
        if (portIdentifier.isCurrentlyOwned()) {
            throw new IllegalArgumentException("Serial port currently in use " + parts[0]);
        }
        CommPort commPort;
        try {
            commPort = portIdentifier.open(this.getClass().getName(), Timeout);
        } catch (PortInUseException ex) {
            throw new IllegalArgumentException("Serial port currently in use " + parts[0]);
        }
        if (!(commPort instanceof SerialPort)) {
            throw new IllegalArgumentException("Invalid serial port specified " + parts[0]);
        }
        _serialPort = (SerialPort) commPort;
        try {
            int baudRate = Integer.parseInt(parts[1]);
            int dataBits = Integer.parseInt(parts[2]);
            int stopBits = Integer.parseInt(parts[3]);
            int parity = Integer.parseInt(parts[4]);
            int flowControl = Integer.parseInt(parts[5]);
            _serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
            _serialPort.setFlowControlMode(flowControl);
            logger.debug("ComPort configured {} Baud:{} Databits:{} Stopbits:{} Parity:{} FlowControl:{}",
                    _serialPort.getName(), baudRate, dataBits, stopBits, parity, flowControl);
        } catch (UnsupportedCommOperationException ex) {
            throw new IllegalArgumentException("Invalid serial port properties " + description);
        }
        try {
            _serialPort.addEventListener(new ComPortReader(_serialPort.getInputStream()));
            _serialPort.notifyOnDataAvailable(true);
        } catch (TooManyListenersException | IOException ex) {
            throw new IllegalArgumentException("Unable to configure serial port");
        }
    }

    @Override
    public boolean write(byte[] buffer, int offset, int len) {
        try {
            _serialPort.getOutputStream().write(buffer, offset, len);
            return true;
        } catch (IOException ex) {
            logger.error("Error write to ComPort - " + ex.getMessage());
        }
        return false;
    }

    @Override
    public void close() {
        _portReader = null;
        _serialPort.notifyOnDataAvailable(false);
        _serialPort.removeEventListener();
        _serialPort.close();
        _serialPort = null;
    }

    @Override
    public void setPortReader(IPortReader portReader) {
        _portReader = portReader;
    }

    private class ComPortReader implements SerialPortEventListener {

        private InputStream _in;
        private byte[] _buffer;
        private int _len;

        public ComPortReader(InputStream in) {
            _in = in;
            _buffer = new byte[1024];
        }

        @Override
        public void serialEvent(SerialPortEvent spe) {
            try {
                while ((_len = _in.read(_buffer, 0, _buffer.length)) > 0) {
                    if (_portReader != null) {
                        _portReader.onReceive(_buffer, _len);
                    }
                }
            } catch (IOException ex) {
                logger.error("ComPortReader ", ex);
            }
        }
    }
}
