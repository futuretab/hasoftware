using hasoftware.Api;
using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class PointRequest : Message
    {
        public CdefAction Action { get; set; }
        public int NodeId { get; set; }
        public String Address { get; set; }

        public PointRequest()
            :this(CdefAction.None)
        {
        }

        public PointRequest(CdefAction action)
            :base(Api.FunctionCode.Point, 0)
        {
            Action = action;
            Address = null;
        }

        public PointRequest(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            NodeId = cdefMessage.GetU32();
            Address = cdefMessage.GetAsciiL();
        }

        public PointResponse CreateResponse() {
            return new PointResponse(TransactionNumber, Action);
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU32(NodeId);
            cdefMessage.PutAsciiL(Address);
        }
    }
}
