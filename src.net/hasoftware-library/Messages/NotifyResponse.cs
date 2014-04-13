using hasoftware.Api;
using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class NotifyResponse : Message
    {
        public int NotifyFunctionCode { get; set; }
        public CdefAction Action { get; set; }
        public List<int> Ids { get; private set; }

        public NotifyResponse()
            : base(Api.FunctionCode.Notify, 0, CdefSystemFlags.Response)
        {
            NotifyFunctionCode = CdefFunctionCode.None;
            Ids = new List<int>();
        }

        public NotifyResponse(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
            NotifyFunctionCode = cdefMessage.GetU16();
            Action = (CdefAction)cdefMessage.GetU8();
            int countIds = cdefMessage.GetU8();
            Ids = new List<int>();
            for (int index = 0; index < countIds; index++) {
                Ids.Add(cdefMessage.GetU32());
            }
        }

        public override void Encode(CdefMessage cdefMessage)
        {
            base.Encode(cdefMessage);
            cdefMessage.PutU16(NotifyFunctionCode);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(Ids.Count);
            foreach (var id in Ids)
            {
                cdefMessage.PutU32(id);
            }
        }
    }
}
