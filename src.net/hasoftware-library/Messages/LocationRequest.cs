//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Api;
using hasoftware.Classes;
using hasoftware.Cdef;

namespace hasoftware.Messages {
   public class LocationRequest : Message {
      public int Action { get; set; }
      public int ParentId { get; set; }

      public LocationRequest() {
         FunctionCode = Api.FunctionCode.Location;
      }

      public LocationRequest(CdefMessage cdefMessage) : base(cdefMessage) {
         Action = cdefMessage.GetInt();
         ParentId = cdefMessage.GetInt();
      }

      public override void Encode(CdefMessage cdefMessage) {
         base.Encode(cdefMessage);
         cdefMessage.PutInt(Action);
         cdefMessage.PutInt(ParentId);
      }

      public LocationResponse CreateResponse() {
         LocationResponse response = new LocationResponse(TransactionNumber);
         return response;
      }
   }
}
