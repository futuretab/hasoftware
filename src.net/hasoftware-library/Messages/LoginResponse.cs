using hasoftware.Api;
using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class LoginResponse : Message
    {
        public LoginResponse(int transactionNumber)
            : base(Api.FunctionCode.Login, transactionNumber, CdefSystemFlags.Response)
        {
        }

        public LoginResponse(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
        }
    }
}
