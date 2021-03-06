//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Cdef;
using hasoftware.Api;

namespace hasoftware.Classes {
   public class Location {
      public int Id { get; set; }
      public int ParentId { get; set; }
      public string Name { get; set; }
      public long CreatedOn { get; set; }
      public long UpdatedOn { get; set; }

      public Location() {
      }

      public Location(CdefMessage cdefMessage) {
         Id = cdefMessage.GetInt();
         ParentId = cdefMessage.GetInt();
         Name = cdefMessage.GetString();
         CreatedOn = cdefMessage.GetLong();
         UpdatedOn = cdefMessage.GetLong();
      }

      public void Encode(CdefMessage cdefMessage) {
         cdefMessage.PutInt(Id);
         cdefMessage.PutInt(ParentId);
         cdefMessage.PutString(Name);
         cdefMessage.PutLong(CreatedOn);
         cdefMessage.PutLong(UpdatedOn);
      }
   }
}
