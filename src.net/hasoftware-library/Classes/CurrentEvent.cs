//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Cdef;
using hasoftware.Api;

namespace hasoftware.Classes {
   public class CurrentEvent {
      public int Id { get; set; }
      public Point Point { get; set; }
      public long CreatedOn { get; set; }
      public long UpdatedOn { get; set; }

      public CurrentEvent() {
      }

      public CurrentEvent(CdefMessage cdefMessage) {
         Id = cdefMessage.GetInt();
         Point = new Point(cdefMessage);
         CreatedOn = cdefMessage.GetLong();
         UpdatedOn = cdefMessage.GetLong();
      }

      public void Encode(CdefMessage cdefMessage) {
         cdefMessage.PutInt(Id);
         Point.Encode(cdefMessage);
         cdefMessage.PutLong(CreatedOn);
         cdefMessage.PutLong(UpdatedOn);
      }
   }
}
