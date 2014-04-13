using hasoftware.Api;
using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class LoginRequest : Message
    {
        public string Username {get;set;}
        public string Password {get;set;}

        public LoginRequest()
            :base(Api.FunctionCode.Login, 0)
        {
        }

        public LoginRequest(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Username = cdefMessage.GetAsciiL();
            Password = cdefMessage.GetAsciiL();
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutAsciiL(Username);
            cdefMessage.PutAsciiL(Password);
        }
    }
}
