using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Classes
{
    public class Location
    {
        public int Id { get; private set; }
        public int ParentId { get; private set; }
        public string Name { get; set; }
        public TimeUtc CreatedOn { get; set; }
        public TimeUtc UpdatedOn { get; set; }

        public Location(int id, int parentId, string name, TimeUtc createdOn, TimeUtc updatedOn)
        {
            Id = id;
            ParentId = parentId;
            Name = name;
            CreatedOn = createdOn;
            UpdatedOn = updatedOn;
        }

        public Location(CdefMessage cdefMessage)
        {
            Id = cdefMessage.GetU32();
            ParentId = cdefMessage.GetU32();
            Name = cdefMessage.GetAsciiL();
            CreatedOn = new TimeUtc(cdefMessage.GetS64());
            UpdatedOn = new TimeUtc(cdefMessage.GetS64());
        }

        public void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.PutU32(Id);
            cdefMessage.PutU32(ParentId);
            cdefMessage.PutAsciiL(Name);
            cdefMessage.PutS64(CreatedOn == null ? 0 : CreatedOn.Time);
            cdefMessage.PutS64(UpdatedOn == null ? 0 : UpdatedOn.Time);
        }
    }
}
