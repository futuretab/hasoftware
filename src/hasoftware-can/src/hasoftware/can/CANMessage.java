package hasoftware.can;

public class CANMessage {

    private int _gatewayId;
    private int _id;
    private int _length;
    private byte[] _data;

    public CANMessage() {
        _gatewayId = 0;
        _id = 0;
        _length = 0;
        _data = new byte[8];
    }

    public CANMessage(int gatewayId, int id, int length, byte[] data) {
        _gatewayId = gatewayId;
        _id = id;
        _length = length;
        _data = data;
    }

    public int getGatewayId() {
        return _gatewayId;
    }

    public void setGatewayId(int gatewayId) {
        _gatewayId = gatewayId;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public int getLength() {
        return _length;
    }

    public void setLength(int length) {
        _length = length;
    }

    public byte[] getData() {
        return _data;
    }

    public void setData(byte[] data) {
        _data = data;
    }
}
