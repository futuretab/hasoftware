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
    public class InputMessageRequest : Message
    {
        public CdefAction Action { get; private set; }
        public List<int> Ids { get; private set; }
        public List<InputMessage> InputMessages { get; private set; }

        public InputMessageRequest()
            :this(CdefAction.None)
        {
        }

        public InputMessageRequest(CdefAction action)
            :base(Api.FunctionCode.InputMessage, 0)
        {
            Action = action;
            Ids = new List<int>();
            InputMessages = new List<InputMessage>();
        }

        public InputMessageRequest(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            Ids = new List<int>();
            int countIds = cdefMessage.GetU8();
            for (int index = 0; index < countIds; index++) {
                Ids.Add(cdefMessage.GetU32());
            }
            int countInputMessages = cdefMessage.GetU8();
            for (int index = 0; index < countInputMessages; index++) {
                InputMessages.Add(new InputMessage(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(Ids.Count);
            foreach (var id in Ids) {
                cdefMessage.PutU32(id);
            }
            cdefMessage.PutU8(InputMessages.Count);
            foreach (var inputMessage in InputMessages) {
                inputMessage.Encode(cdefMessage);
            }
        }
    }
}
