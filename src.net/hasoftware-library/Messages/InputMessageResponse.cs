using hasoftware.Api;
using hasoftware.Cdef;
using hasoftware.Classes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class InputMessageResponse : Message
    {
        public CdefAction Action { get; private set; }
        public List<InputMessage> InputMessages { get; private set; }

        public InputMessageResponse(int transactionNumber)
            :base(Api.FunctionCode.InputMessage, transactionNumber, CdefSystemFlags.Response)
        {
            Action = CdefAction.None;
            InputMessages = new List<InputMessage>();
        }

        public InputMessageResponse(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            int countInputMessages = cdefMessage.GetU8();
            for (int index = 0; index < countInputMessages; index++) {
                InputMessages.Add(new InputMessage(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(InputMessages.Count);
            foreach (var inputMessage in InputMessages) {
                inputMessage.Encode(cdefMessage);
            }
        }
    }
}
