using hasoftware.Util;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Cdef
{
    public class CdefMessage : MemArray
    {
        public CdefMessage()
            : base()
        {
        }

        public CdefMessage(byte[] buffer, int length)
            : base(buffer, length)
        {
        }

        public int PutBoolean(bool value)
        {
            return PutBoolean(value, -1);
        }

        public int PutU8(int value)
        {
            return PutU8(value, -1);
        }

        public int PutU16(int value)
        {
            return PutU16(value, -1);
        }

        public int PutU32(int value)
        {
            return PutU32(value, -1);
        }

        public int PutS64(long value)
        {
            return PutS64(value, -1);
        }

        public int PutAsciiL(String value)
        {
            return PutAsciiL(value, -1);
        }

        public int PutAsciiN(String value, int length)
        {
            return PutAsciiN(value, length, -1);
        }

        public bool GetBoolean()
        {
            return GetBoolean(-1);
        }

        public int GetU8()
        {
            return GetU8(-1);
        }

        public int GetU16()
        {
            return GetU16(-1);
        }

        public int GetU32()
        {
            return GetU32(-1);
        }

        public long GetS64()
        {
            return GetS64(-1);
        }

        public String GetAsciiL()
        {
            return GetAsciiL(-1);
        }

        public String GetAsciiN(int length)
        {
            return getAsciiN(length, -1);
        }

        public int PutBoolean(bool value, int offset)
        {
            if (offset > -1)
            {
                _index = offset;
            }
            CheckLength(1);
            _array[_index] = (byte)((value == true) ? 1 : 0);
            _index += 1;
            Terminate();
            return _index;
        }

        public int PutU8(int value, int offset)
        {
            if (offset > -1)
            {
                _index = offset;
            }
            CheckLength(1);
            _array[_index] = (byte)(value & 0xFF);
            _index += 1;
            Terminate();
            return _index;
        }

        public int PutU16(int value, int offset)
        {
            if (offset > -1)
            {
                _index = offset;
            }
            CheckLength(2);
            _array[_index] = (byte)(value & 0xFF);
            _array[_index + 1] = (byte)((value >> 8) & 0xFF);
            _index += 2;
            Terminate();
            return _index;
        }

        public int PutU32(int value, int offset)
        {
            if (offset > -1)
            {
                _index = offset;
            }
            CheckLength(4);
            _array[_index] = (byte)(value & 0xFF);
            _array[_index + 1] = (byte)((value >> 8) & 0xFF);
            _array[_index + 2] = (byte)((value >> 16) & 0xFF);
            _array[_index + 3] = (byte)((value >> 24) & 0xFF);
            _index += 4;
            Terminate();
            return _index;
        }

        public int PutS64(long value, int offset)
        {
            if (offset > -1)
            {
                _index = offset;
            }
            CheckLength(8);
            _array[_index] = (byte)(value & 0xFF);
            _array[_index + 1] = (byte)((value >> 8) & 0xFF);
            _array[_index + 2] = (byte)((value >> 16) & 0xFF);
            _array[_index + 3] = (byte)((value >> 24) & 0xFF);
            _array[_index + 4] = (byte)((value >> 32) & 0xFF);
            _array[_index + 5] = (byte)((value >> 40) & 0xFF);
            _array[_index + 6] = (byte)((value >> 48) & 0xFF);
            _array[_index + 7] = (byte)((value >> 56) & 0xFF);
            _index += 8;
            Terminate();
            return _index;
        }

        public int PutAsciiL(String value, int offset)
        {
            if (offset > -1)
            {
                _index = offset;
            }
            int length = (value == null) ? 0 : (value.Length >= 255) ? 255 : value.Length;     // AsciiL only supports strings up to 255 characters
            CheckLength(1 + length);
            _array[_index] = (byte)(length);
            for (int i = 0; i < length; i++)
            {
                _array[_index + 1 + i] = (byte)value[i];
            }
            _index += (1 + length);
            Terminate();
            return _index;
        }

        public int PutAsciiN(String value, int length, int offset)
        {
            if (offset > -1)
            {
                _index = offset;
            }
            CheckLength(length);
            for (int i = 0; i < length; i++)
            {
                if (i < value.Length)
                {
                    _array[_index + i] = (byte)value[i];
                }
                else
                {
                    _array[_index + i] = (byte)' ';
                }
            }
            _index += length;
            Terminate();
            return _index;
        }

        public bool GetBoolean(int offset)
        {
            bool result = false;
            if (offset > -1)
            {
                _index = offset;
            }
            if (_index + 1 <= _length)
            {
                result = _array[_index] != 0;
                _index += 1;
            }
            return result;
        }

        public int GetU8(int offset)
        {
            int result = 0;
            if (offset > -1)
            {
                _index = offset;
            }
            if (_index + 1 <= _length)
            {
                result = (int)_array[_index] & 0xFF;
                _index += 1;
            }
            return result;
        }

        public int GetU16(int offset)
        {
            int result = 0;
            if (offset > -1)
            {
                _index = offset;
            }
            if (_index + 1 <= _length)
            {
                result = (int)_array[_index] & 0xFF
                        | (((int)_array[_index + 1] & 0xFF) << 8);
                _index += 2;
            }
            return result;
        }

        public int GetU32(int offset)
        {
            int result = 0;
            if (offset > -1)
            {
                _index = offset;
            }
            if (_index + 4 <= _length)
            {
                result = (int)_array[_index] & 0xFF
                        | (((int)_array[_index + 1] & 0xFF) << 8)
                        | (((int)_array[_index + 2] & 0xFF) << 16)
                        | (((int)_array[_index + 3] & 0xFF) << 24);
                _index += 4;
            }
            return result;
        }

        public long GetS64(int offset)
        {
            long result = 0;
            if (offset > -1)
            {
                _index = offset;
            }
            if (_index + 8 <= _length)
            {
                result = (long)_array[_index] & 0xFF
                        | (((long)_array[_index + 1] & 0xFF) << 8)
                        | (((long)_array[_index + 2] & 0xFF) << 16)
                        | (((long)_array[_index + 3] & 0xFF) << 24)
                        | (((long)_array[_index + 4] & 0xFF) << 32)
                        | (((long)_array[_index + 5] & 0xFF) << 40)
                        | (((long)_array[_index + 6] & 0xFF) << 48)
                        | (((long)_array[_index + 7] & 0xFF) << 56);
                _index += 8;
            }
            return result;
        }

        public String GetAsciiL(int offset)
        {
            String result = null;
            if (offset > -1)
            {
                _index = offset;
            }
            if (_index + 1 <= _length)
            {
                int length = (int)_array[_index];
                if (_index + 1 + length <= _length)
                {
                    result = Encoding.ASCII.GetString(_array, _index + 1, length);
                    _index += (1 + length);
                }
            }
            return result;
        }

        public String getAsciiN(int length, int offset)
        {
            String result = null;
            if (offset > -1)
            {
                _index = offset;
            }
            if (_index + length <= _length)
            {
                result = Encoding.ASCII.GetString(_array, _index, length);
                _index += (length);
            }
            return result;
        }
    }
}
