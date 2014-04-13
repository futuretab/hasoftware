using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Api
{
    public class AnError
    {
        private readonly int _number;
        private readonly int _code;
        private readonly string _message;

        public AnError(int number, int code, string message)
        {
            _number = number;
            _code = code;
            _message = message;
        }

        public AnError(CdefMessage cdefMessage)
        {
            _number = cdefMessage.GetU8();
            _code = cdefMessage.GetU32();
            _message = cdefMessage.GetAsciiL();
        }

        public void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.PutU8(_number);
            cdefMessage.PutU32(_code);
            cdefMessage.PutAsciiL(_message);
        }

        public int Nummber { get { return _number; } }
        public int Code { get { return _code; } }
        public string Message { get { return _message; } }
    }
}
