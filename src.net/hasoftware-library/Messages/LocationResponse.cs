//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 28/04/2014 1:27:52 AM
//

using System;
using System.Collections.Generic;
using hasoftware.Api;
using hasoftware.Classes;
using hasoftware.Cdef;

namespace hasoftware.Messages {
   public class LocationResponse : Message {
      public int Action { get; set; }
      public List<Location> Locations { get; set; }

      public LocationResponse(int transactionNumber) : base(Api.FunctionCode.Location, transactionNumber, CdefSystemFlags.Response) {
         Locations = new List<Location>();
      }

      public LocationResponse(CdefMessage cdefMessage) : base(cdefMessage) {
         Action = cdefMessage.GetInt();
         {
            Locations = new List<Location>();
            int c = cdefMessage.GetInt();
            for (int i=0; i<c; i++) {
               Locations.Add(new Location(cdefMessage));
            }
         }
      }

      public override void Encode(CdefMessage cdefMessage) {
         base.Encode(cdefMessage);
         cdefMessage.PutInt(Action);
         cdefMessage.PutInt(Locations.Count);
         foreach (var obj in Locations) {
            obj.Encode(cdefMessage);
         }
      }
   }
}
