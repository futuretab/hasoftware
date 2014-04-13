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
    public class PointResponse : Message
    {
        public CdefAction Action { get; private set; }
        public List<Point> Points { get; private set; }

        public PointResponse(int transactionNumber, CdefAction action)
            :base(Api.FunctionCode.Point, transactionNumber, CdefSystemFlags.Response)
        {
            Action = action;
            Points = new List<Point>();
        }

        public PointResponse(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            Points = new List<Point>();
            int countPoints = cdefMessage.GetU8();
            for (int index = 0; index < countPoints; index++) {
                Points.Add(new Point(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(Points.Count);
            foreach (var point in Points) {
                point.Encode(cdefMessage);
            }
        }
    }
}
