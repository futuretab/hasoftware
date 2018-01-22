//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Api;
using hasoftware.Classes;
using hasoftware.Cdef;

namespace hasoftware.Messages {
   public class CurrentEventResponse : Message {
      public int Action { get; set; }
      public List<CurrentEvent> CurrentEvents { get; set; }

      public CurrentEventResponse(int transactionNumber) : base(Api.FunctionCode.CurrentEvent, transactionNumber, CdefSystemFlags.Response) {
         CurrentEvents = new List<CurrentEvent>();
      }

      public CurrentEventResponse(CdefMessage cdefMessage) : base(cdefMessage) {
         Action = cdefMessage.GetInt();
         {
            CurrentEvents = new List<CurrentEvent>();
            int c = cdefMessage.GetInt();
            for (int i=0; i<c; i++) {
               CurrentEvents.Add(new CurrentEvent(cdefMessage));
            }
         }
      }

      public override void Encode(CdefMessage cdefMessage) {
         base.Encode(cdefMessage);
         cdefMessage.PutInt(Action);
         cdefMessage.PutInt(CurrentEvents.Count);
         foreach (var obj in CurrentEvents) {
            obj.Encode(cdefMessage);
         }
      }
   }
}
