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
    public class OutputMessageResponse : Message
    {
        public CdefAction Action { get; private set; }
        public List<OutputMessage> OutputMessages { get; private set; }

        public OutputMessageResponse(int transactionNumber)
            :base(Api.FunctionCode.OutputMessage, transactionNumber, CdefSystemFlags.Response)
        {
            Action = CdefAction.None;
            OutputMessages = new List<OutputMessage>();
        }

        public OutputMessageResponse(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            int countOutputMessages = cdefMessage.GetU8();
            for (int index = 0; index < countOutputMessages; index++) {
                OutputMessages.Add(new OutputMessage(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(OutputMessages.Count);
            foreach (var outputMessage in OutputMessages) {
                outputMessage.Encode(cdefMessage);
            }
        }
    }
}
