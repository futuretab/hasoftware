//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Api;
using hasoftware.Classes;
using hasoftware.Cdef;

namespace hasoftware.Messages {
   public class NotifyResponse : Message {
      public int NotifyFunctionCode { get; set; }
      public int Action { get; set; }
      public List<int> Ids { get; set; }

      public NotifyResponse(int transactionNumber) : base(Api.FunctionCode.Notify, transactionNumber, CdefSystemFlags.Response) {
         Ids = new List<int>();
      }

      public NotifyResponse(CdefMessage cdefMessage) : base(cdefMessage) {
         NotifyFunctionCode = cdefMessage.GetInt();
         Action = cdefMessage.GetInt();
         {
            Ids = new List<int>();
            int c = cdefMessage.GetInt();
            for (int i=0; i<c; i++) {
               Ids.Add(cdefMessage.GetInt());
            }
         }
      }

      public override void Encode(CdefMessage cdefMessage) {
         base.Encode(cdefMessage);
         cdefMessage.PutInt(NotifyFunctionCode);
         cdefMessage.PutInt(Action);
         cdefMessage.PutInt(Ids.Count);
         foreach (var obj in Ids) {
            cdefMessage.PutInt(obj);
         }
      }
   }
}
