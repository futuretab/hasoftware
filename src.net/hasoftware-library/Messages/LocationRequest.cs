using hasoftware.Api;
using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class LocationRequest : Message
    {
        public CdefAction Action { get; set; }
        public int ParentId { get; set; }

        public LocationRequest()
            : this(CdefAction.None)
        {
        }

        public LocationRequest(CdefAction action)
            :base(Api.FunctionCode.Location, 0)
        {
            Action = action;
        }

        public LocationRequest(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            ParentId = cdefMessage.GetU32();
        }

        public LocationResponse CreateResponse() {
            return new LocationResponse(TransactionNumber, Action);
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU32(ParentId);
        }
    }
}
