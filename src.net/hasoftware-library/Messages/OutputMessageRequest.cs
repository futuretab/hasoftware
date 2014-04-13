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
    public class OutputMessageRequest : Message
    {
        public CdefAction Action { get; private set; }
        public List<int> Ids { get; private set; }
        public List<OutputMessage> OutputMessages { get; private set; }

        public OutputMessageRequest()
            :this(CdefAction.None)
        {
        }

        public OutputMessageRequest(CdefAction action)
            :base(Api.FunctionCode.OutputMessage, 0)
        {
            Action = action;
            Ids = new List<int>();
            OutputMessages = new List<OutputMessage>();
        }

        public OutputMessageRequest(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            Ids = new List<int>();
            int countIds = cdefMessage.GetU8();
            for (int index = 0; index < countIds; index++)
            {
                Ids.Add(cdefMessage.GetU32());
            }
            int countOutputMessages = cdefMessage.GetU8();
            for (int index = 0; index < countOutputMessages; index++)
            {
                OutputMessages.Add(new OutputMessage(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(Ids.Count);
            foreach (var id in Ids) {
                cdefMessage.PutU32(id);
            }
            cdefMessage.PutU8(OutputMessages.Count);
            foreach (var outputMessage in OutputMessages)
            {
                outputMessage.Encode(cdefMessage);
            }
        }
    }
}
