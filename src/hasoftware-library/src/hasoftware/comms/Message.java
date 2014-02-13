package hasoftware.comms;

public class Message {
    private final int MessageSize = 32;
    
    private int _length;
    private int _offset;
    private byte[] _data;
    
    public Message() {
        _length = 0;
        _offset = 0;
        _data = new byte[MessageSize];
    }
    
    public Message(byte[] data, int length) {
        _length = length;
        _offset = 0;
        _data = new byte[MessageSize];
        System.arraycopy(data, 0, _data, 0, length);
    }
    
    public void reset() {
        _offset = 0;
        _length = 0;
    }
    
    public int getLength() {
        return _length;
    }
    
    public int getOffset() {
        return _offset;
    }
    
    public void setOffste(int offset) {
        _offset = offset;
    }
    
    public void addByte(byte b) {
        addByte(_offset, b);
    }
    
    public void addByte(int offset, byte b) {
        _offset = offset;
        _data[_offset++] = b;
        if (_offset > _length) {
            _length = _offset;
        }
    }
    
    public byte getByte(int offset) {
        return _data[offset];
    }
}
