//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Api;
using hasoftware.Classes;
using hasoftware.Cdef;

namespace hasoftware.Messages {
   public class LoginResponse : Message {
      public LoginResponse(int transactionNumber) : base(Api.FunctionCode.Login, transactionNumber, CdefSystemFlags.Response) {
      }

      public LoginResponse(CdefMessage cdefMessage) : base(cdefMessage) {
      }

      public override void Encode(CdefMessage cdefMessage) {
         base.Encode(cdefMessage);
      }
   }
}
