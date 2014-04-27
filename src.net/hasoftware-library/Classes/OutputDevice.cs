//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 28/04/2014 1:27:52 AM
//

using System;
using System.Collections.Generic;
using hasoftware.Cdef;
using hasoftware.Api;

namespace hasoftware.Classes {
   public class OutputDevice {
      public int Id { get; set; }
      public string Name { get; set; }
      public string Description { get; set; }
      public string Address { get; set; }
      public string DeviceTypeCode { get; set; }
      public string SerialNumber { get; set; }
      public long CreatedOn { get; set; }
      public long UpdatedOn { get; set; }

      public OutputDevice() {
      }

      public OutputDevice(CdefMessage cdefMessage) {
         Id = cdefMessage.GetInt();
         Name = cdefMessage.GetString();
         Description = cdefMessage.GetString();
         Address = cdefMessage.GetString();
         DeviceTypeCode = cdefMessage.GetString();
         SerialNumber = cdefMessage.GetString();
         CreatedOn = cdefMessage.GetLong();
         UpdatedOn = cdefMessage.GetLong();
      }

      public void Encode(CdefMessage cdefMessage) {
         cdefMessage.PutInt(Id);
         cdefMessage.PutString(Name);
         cdefMessage.PutString(Description);
         cdefMessage.PutString(Address);
         cdefMessage.PutString(DeviceTypeCode);
         cdefMessage.PutString(SerialNumber);
         cdefMessage.PutLong(CreatedOn);
         cdefMessage.PutLong(UpdatedOn);
      }
   }
}
