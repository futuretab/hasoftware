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
    public class LocationResponse : Message
    {
        public CdefAction Action { get; set; }
        public List<Location> Locations { get; private set; }

        public LocationResponse(int transactionNumber, CdefAction action)
            :base(Api.FunctionCode.Location, transactionNumber, CdefSystemFlags.Response)
        {
            Action = action;
            Locations = new List<Location>();
        }

        public LocationResponse(CdefMessage cdefMessage)
            :base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            int countLocations = cdefMessage.GetU8();
            for (int index = 0; index < countLocations; index++) {
                Locations.Add(new Location(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(Locations.Count);
            foreach (var location in Locations) {
                location.Encode(cdefMessage);
            }
        }
    }
}
