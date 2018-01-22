//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Api;
using hasoftware.Classes;
using hasoftware.Cdef;

namespace hasoftware.Messages {
   public class OutputMessageRequest : Message {
      public int Action { get; set; }
      public List<int> Ids { get; set; }
      public List<OutputMessage> OutputMessages { get; set; }

      public OutputMessageRequest() {
         FunctionCode = Api.FunctionCode.OutputMessage;
         Ids = new List<int>();
         OutputMessages = new List<OutputMessage>();
      }

      public OutputMessageRequest(CdefMessage cdefMessage) : base(cdefMessage) {
         Action = cdefMessage.GetInt();
         {
            Ids = new List<int>();
            int c = cdefMessage.GetInt();
            for (int i=0; i<c; i++) {
               Ids.Add(cdefMessage.GetInt());
            }
         }
         {
            OutputMessages = new List<OutputMessage>();
            int c = cdefMessage.GetInt();
            for (int i=0; i<c; i++) {
               OutputMessages.Add(new OutputMessage(cdefMessage));
            }
         }
      }

      public override void Encode(CdefMessage cdefMessage) {
         base.Encode(cdefMessage);
         cdefMessage.PutInt(Action);
         cdefMessage.PutInt(Ids.Count);
         foreach (var obj in Ids) {
            cdefMessage.PutInt(obj);
         }
         cdefMessage.PutInt(OutputMessages.Count);
         foreach (var obj in OutputMessages) {
            obj.Encode(cdefMessage);
         }
      }

      public OutputMessageResponse CreateResponse() {
         OutputMessageResponse response = new OutputMessageResponse(TransactionNumber);
         return response;
      }
   }
}
