using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Classes
{
    public class CurrentEvent
    {
        public int Id { get; private set; }
        public Point Point { get; private set; }
        public TimeUtc CreatedOn { get; private set; }
        public TimeUtc UpdatedOn { get; private set; }

        public CurrentEvent()
        {
        }

        public CurrentEvent(int id, Point point, TimeUtc createdOn, TimeUtc updatedOn)
        {
            Id = id;
            Point = point;
            CreatedOn = createdOn;
            UpdatedOn = updatedOn;
        }

        public CurrentEvent(CdefMessage cdefMessage)
        {
            Id = cdefMessage.GetU32();
            Point = new Point(cdefMessage);
            CreatedOn = new TimeUtc(cdefMessage.GetS64());
            UpdatedOn = new TimeUtc(cdefMessage.GetS64());
        }

        public void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.PutU32(Id);
            Point.Encode(cdefMessage);
            cdefMessage.PutS64(CreatedOn == null ? 0 : CreatedOn.Time);
            cdefMessage.PutS64(UpdatedOn == null ? 0 : UpdatedOn.Time);
        }
    }
}
