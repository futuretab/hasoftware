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
    public class CurrentEventResponse : Message
    {
        public CdefAction Action { get; set; }
        public List<CurrentEvent> CurrentEvents { get; private set; }

        public CurrentEventResponse(int transactionNumber)
            : base(Api.FunctionCode.CurrentEvent, transactionNumber, CdefSystemFlags.Response)
        {
            Action = CdefAction.None;
            CurrentEvents = new List<CurrentEvent>();
        }

        public CurrentEventResponse(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            CurrentEvents = new List<CurrentEvent>();
            int countInputMessages = cdefMessage.GetU8();
            for (int index = 0; index < countInputMessages; index++) {
                CurrentEvents.Add(new CurrentEvent(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(CurrentEvents.Count);
            foreach (var currentEvent in CurrentEvents) {
                currentEvent.Encode(cdefMessage);
            }
        }
    }
}
