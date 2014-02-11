package hasoftware.util;

public class MemArray {

    static private final int INITIAL_SIZE = 128;
    static private final int INCREMENT_SIZE = 512;
    static private final int TERMINATOR_SIZE = 2;
    protected byte[] _array;
    protected int _index;
    protected int _length;
    protected int _arrayLength;

    public MemArray() {
        _index = 0;
        _length = 0;
        _array = new byte[INITIAL_SIZE];
        _arrayLength = _array.length - TERMINATOR_SIZE;
        terminate();
    }

    public MemArray(MemArray other) {
        _index = 0;
        _arrayLength = other._arrayLength;
        _length = other._length;
        _array = new byte[other._arrayLength + TERMINATOR_SIZE];
        System.arraycopy(other._array, 0, _array, 0, other._array.length);
        terminate();
    }

    public MemArray(byte[] array, int length) {
        _index = 0;
        _arrayLength = length;
        _length = length;
        _array = new byte[length + TERMINATOR_SIZE];
        System.arraycopy(array, 0, _array, 0, length);
        terminate();
    }

    public int getOffset() {
        return _index;
    }

    public int getLength() {
        return _length;
    }

    public void clear() {
        _index = 0;
        _length = 0;
    }

    protected void checkLength(int size) {
        int newLength = _index + size;
        if (newLength >= _arrayLength) {
            int newArrayLength = _arrayLength;
            while (newArrayLength - TERMINATOR_SIZE < newLength) {
                newArrayLength += INCREMENT_SIZE;
            }
            byte[] tmpArray = new byte[newArrayLength];
            System.arraycopy(_array, 0, tmpArray, 0, _length);
            _array = tmpArray;
            _arrayLength = newArrayLength - TERMINATOR_SIZE;
        }
    }

    protected final void terminate() {
        if (_index > _length) {
            _length = _index;
        }
        _array[_length] = 0;
        _array[_length + 1] = 0;
    }

    public byte[] getBytes() {
        return _array;
    }
}
