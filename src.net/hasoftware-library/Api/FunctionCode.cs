using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Api
{
    public class FunctionCode : CdefFunctionCode
    {
        public const int InputMessage = 0x1000;
        public const int OutputMessage = 0x1001;
        public const int Login = 0x1002;
        public const int Logout = 0x1003;
        public const int OutputDevice = 0x1003;
        public const int Location = 0x1004;
        public const int Point = 0x1005;
        public const int CurrentEvent = 0x1006;
    }
}
