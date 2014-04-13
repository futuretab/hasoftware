using hasoftware.Api;
using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class NotifyRequest : Message
    {
        public List<int> FunctionCodes { get; private set; }

        public NotifyRequest()
            : base (Api.FunctionCode.Notify, 0, 0)
        {
            FunctionCodes = new List<int>();
        }

        public NotifyRequest(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
            FunctionCodes = new List<int>();
            int count = cdefMessage.GetU8();
            for (int i = 0; i < count; i++) {
                FunctionCodes.Add(cdefMessage.GetU16());
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8(FunctionCodes.Count);
            foreach (var functionCode in FunctionCodes) {
                cdefMessage.PutU16(functionCode);
            }
        }
    }
}
