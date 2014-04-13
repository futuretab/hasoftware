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
    public class CurrentEventRequest : Message
    {
        public CdefAction Action { get; set; }
        public List<int> Ids { get; private set; }
        public List<CurrentEvent> CurrentEvents { get; private set; }

        public CurrentEventRequest()
            : this(CdefAction.None)
        {
        }

        public CurrentEventRequest(CdefAction action)
            : base(Api.FunctionCode.CurrentEvent, 0)
        {
            Action = action;
            Ids = new List<int>();
            CurrentEvents = new List<CurrentEvent>();
        }

        public CurrentEventRequest(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            Ids = new List<int>();
            int countIds = cdefMessage.GetU8();
            for (int index = 0; index < countIds; index++)
            {
                Ids.Add(cdefMessage.GetU32());
            }
            CurrentEvents = new List<CurrentEvent>();
            int countICurrentEvents = cdefMessage.GetU8();
            for (int index = 0; index < countICurrentEvents; index++)
            {
                CurrentEvents.Add(new CurrentEvent(cdefMessage));
            }
        }

        public CurrentEventResponse CreateResponse()
        {
            CurrentEventResponse response = new CurrentEventResponse(TransactionNumber);
            response.Action = Action;
            return response;
        }

        public override void Encode(CdefMessage cdefMessage)
        {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(Ids.Count);
            foreach (var id in Ids)
            {
                cdefMessage.PutU32(id);
            }
            cdefMessage.PutU8(CurrentEvents.Count);
            foreach (var currentEvent in CurrentEvents)
            {
                currentEvent.Encode(cdefMessage);
            }
        }
    }
}
