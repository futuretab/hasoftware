package hasoftware.cdef;

import hasoftware.util.MemArray;

public class CDEFMessage extends MemArray {

    public CDEFMessage() {
        super();
    }

    public CDEFMessage(byte[] buffer, int length) {
        super(buffer, length);
    }

    public int putBoolean(boolean value) {
        return putBoolean(value, -1);
    }

    public int putU8(int value) {
        return putU8(value, -1);
    }

    public int putU16(int value) {
        return putU16(value, -1);
    }

    public int putInt(int value) {
        return putInt(value, -1);
    }

    public int putLong(long value) {
        return putLong(value, -1);
    }

    public int putString(String value) {
        return putString(value, -1);
    }

    public int putAsciiN(String value, int length) {
        return putAsciiN(value, length, -1);
    }

    public boolean getBoolean() {
        return getBoolean(-1);
    }

    public int getU8() {
        return getU8(-1);
    }

    public int getU16() {
        return getU16(-1);
    }

    public int getInt() {
        return getInt(-1);
    }

    public long getLong() {
        return getLong(-1);
    }

    public String getString() {
        return getString(-1);
    }

    public String getAsciiN(int length) {
        return getAsciiN(length, -1);
    }

    public int putBoolean(boolean value, int offset) {
        if (offset > -1) {
            _index = offset;
        }
        checkLength(1);
        _array[_index] = (byte) ((value == true) ? 1 : 0);
        _index += 1;
        terminate();
        return _index;
    }

    public int putU8(int value, int offset) {
        if (offset > -1) {
            _index = offset;
        }
        checkLength(1);
        _array[_index] = (byte) (value & 0xFF);
        _index += 1;
        terminate();
        return _index;
    }

    public int putU16(int value, int offset) {
        if (offset > -1) {
            _index = offset;
        }
        checkLength(2);
        _array[_index] = (byte) (value & 0xFF);
        _array[_index + 1] = (byte) ((value >> 8) & 0xFF);
        _index += 2;
        terminate();
        return _index;
    }

    public int putInt(int value, int offset) {
        if (offset > -1) {
            _index = offset;
        }
        checkLength(4);
        _array[_index] = (byte) (value & 0xFF);
        _array[_index + 1] = (byte) ((value >> 8) & 0xFF);
        _array[_index + 2] = (byte) ((value >> 16) & 0xFF);
        _array[_index + 3] = (byte) ((value >> 24) & 0xFF);
        _index += 4;
        terminate();
        return _index;
    }

    public int putLong(long value, int offset) {
        if (offset > -1) {
            _index = offset;
        }
        checkLength(8);
        _array[_index] = (byte) (value & 0xFF);
        _array[_index + 1] = (byte) ((value >> 8) & 0xFF);
        _array[_index + 2] = (byte) ((value >> 16) & 0xFF);
        _array[_index + 3] = (byte) ((value >> 24) & 0xFF);
        _array[_index + 4] = (byte) ((value >> 32) & 0xFF);
        _array[_index + 5] = (byte) ((value >> 40) & 0xFF);
        _array[_index + 6] = (byte) ((value >> 48) & 0xFF);
        _array[_index + 7] = (byte) ((value >> 56) & 0xFF);
        _index += 8;
        terminate();
        return _index;
    }

    public int putString(String value, int offset) {
        if (offset > -1) {
            _index = offset;
        }
        int length = (value == null) ? 0 : (value.length() >= 255) ? 255 : value.length();     // AsciiL only supports strings up to 255 characters
        checkLength(1 + length);
        _array[_index] = (byte) (length);
        for (int i = 0; i < length; i++) {
            _array[_index + 1 + i] = (byte) value.charAt(i);
        }
        _index += (1 + length);
        terminate();
        return _index;
    }

    public int putAsciiN(String value, int length, int offset) {
        if (offset > -1) {
            _index = offset;
        }
        checkLength(length);
        for (int i = 0; i < length; i++) {
            if (i < value.length()) {
                _array[_index + i] = (byte) value.charAt(i);
            } else {
                _array[_index + i] = (byte) ' ';
            }
        }
        _index += length;
        terminate();
        return _index;
    }

    public boolean getBoolean(int offset) {
        boolean result = false;
        if (offset > -1) {
            _index = offset;
        }
        if (_index + 1 <= _length) {
            result = _array[_index] != 0;
            _index += 1;
        }
        return result;
    }

    public int getU8(int offset) {
        int result = 0;
        if (offset > -1) {
            _index = offset;
        }
        if (_index + 1 <= _length) {
            result = (int) _array[_index] & 0xFF;
            _index += 1;
        }
        return result;
    }

    public int getU16(int offset) {
        int result = 0;
        if (offset > -1) {
            _index = offset;
        }
        if (_index + 1 <= _length) {
            result = (int) _array[_index] & 0xFF
                    | (((int) _array[_index + 1] & 0xFF) << 8);
            _index += 2;
        }
        return result;
    }

    public int getInt(int offset) {
        int result = 0;
        if (offset > -1) {
            _index = offset;
        }
        if (_index + 4 <= _length) {
            result = (int) _array[_index] & 0xFF
                    | (((int) _array[_index + 1] & 0xFF) << 8)
                    | (((int) _array[_index + 2] & 0xFF) << 16)
                    | (((int) _array[_index + 3] & 0xFF) << 24);
            _index += 4;
        }
        return result;
    }

    public long getLong(int offset) {
        long result = 0;
        if (offset > -1) {
            _index = offset;
        }
        if (_index + 8 <= _length) {
            result = (long) _array[_index] & 0xFF
                    | (((long) _array[_index + 1] & 0xFF) << 8)
                    | (((long) _array[_index + 2] & 0xFF) << 16)
                    | (((long) _array[_index + 3] & 0xFF) << 24)
                    | (((long) _array[_index + 4] & 0xFF) << 32)
                    | (((long) _array[_index + 5] & 0xFF) << 40)
                    | (((long) _array[_index + 6] & 0xFF) << 48)
                    | (((long) _array[_index + 7] & 0xFF) << 56);
            _index += 8;
        }
        return result;
    }

    public String getString(int offset) {
        String result = null;
        if (offset > -1) {
            _index = offset;
        }
        if (_index + 1 <= _length) {
            int length = (int) _array[_index];
            if (_index + 1 + length <= _length) {
                result = new String(_array, _index + 1, length);
                _index += (1 + length);
            }
        }
        return result;
    }

    public String getAsciiN(int length, int offset) {
        String result = null;
        if (offset > -1) {
            _index = offset;
        }
        if (_index + length <= _length) {
            result = new String(_array, _index, length);
            _index += (length);
        }
        return result;
    }
}
