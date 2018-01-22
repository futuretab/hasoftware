//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Cdef;
using hasoftware.Api;

namespace hasoftware.Classes {
   public class OutputMessage {
      public int Id { get; set; }
      public string DeviceTypeCode { get; set; }
      public string Data { get; set; }
      public long CreatedOn { get; set; }

      public OutputMessage() {
      }

      public OutputMessage(CdefMessage cdefMessage) {
         Id = cdefMessage.GetInt();
         DeviceTypeCode = cdefMessage.GetString();
         Data = cdefMessage.GetString();
         CreatedOn = cdefMessage.GetLong();
      }

      public void Encode(CdefMessage cdefMessage) {
         cdefMessage.PutInt(Id);
         cdefMessage.PutString(DeviceTypeCode);
         cdefMessage.PutString(Data);
         cdefMessage.PutLong(CreatedOn);
      }
   }
}
