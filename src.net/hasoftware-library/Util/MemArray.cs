using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Util
{
    public class MemArray
    {
        private static readonly int INITIAL_SIZE = 128;
        private static readonly int INCREMENT_SIZE = 512;
        private static readonly int TERMINATOR_SIZE = 2;

        protected byte[] _array;
        protected int _index;
        protected int _length;
        protected int _arrayLength;

        public MemArray()
        {
            _index = 0;
            _length = 0;
            _array = new byte[INITIAL_SIZE];
            _arrayLength = _array.Length - TERMINATOR_SIZE;
            Terminate();
        }

        public MemArray(MemArray other)
        {
            _index = 0;
            _arrayLength = other._arrayLength;
            _length = other._length;
            _array = new byte[other._arrayLength + TERMINATOR_SIZE];
            Array.Copy(other._array, 0, _array, 0, other._array.Length);
            Terminate();
        }

        public MemArray(byte[] array, int length)
        {
            _index = 0;
            _arrayLength = length;
            _length = length;
            _array = new byte[length + TERMINATOR_SIZE];
            Array.Copy(array, 0, _array, 0, length);
            Terminate();
        }

        public int Offset
        {
            get { return _index; }
        }

        public int Length
        {
            get { return _length; }
        }

        public byte[] Bytes
        {
            get { return _array; }
        }

        public void Clear()
        {
            _index = 0;
            _length = 0;
        }

        protected void CheckLength(int size)
        {
            int newLength = _index + size;
            if (newLength >= _arrayLength)
            {
                int newArrayLength = _arrayLength;
                while (newArrayLength - TERMINATOR_SIZE < newLength)
                {
                    newArrayLength += INCREMENT_SIZE;
                }
                byte[] tmpArray = new byte[newArrayLength];
                Array.Copy(_array, 0, tmpArray, 0, _length);
                _array = tmpArray;
                _arrayLength = newArrayLength - TERMINATOR_SIZE;
            }
        }

        protected void Terminate() {
            if (_index > _length) {
                _length = _index;
            }
            _array[_length] = 0;
            _array[_length + 1] = 0;
        }
    }
}
