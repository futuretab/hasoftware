using hasoftware.Api;
using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class ErrorResponse : Message
    {
        public List<AnError> Errors { get; private set; }

        public ErrorResponse(int functionCode, int transactionNumber)
            : base(functionCode, transactionNumber, CdefSystemFlags.Response | CdefSystemFlags.Error)
        {
            Errors = new List<AnError>();
        }

        public ErrorResponse(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
            Errors = new List<AnError>();
            for (int count = cdefMessage.GetU8(); count > 0; count--) {
                Errors.Add(new AnError(cdefMessage));
            }
        }

        public void AddError(int number, int code, String message) {
            Errors.Add(new AnError(number, code, message));
        }

        public override void Encode(CdefMessage cdefMessage)
        {
            base.Encode(cdefMessage);
            cdefMessage.PutU8(Errors.Count);
            foreach (var error in Errors)
            {
                error.Encode(cdefMessage);
            }
        }
    }
}
