//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

using System;
using System.Collections.Generic;
using hasoftware.Cdef;
using hasoftware.Api;

namespace hasoftware.Classes {
   public class Point {
      public int Id { get; set; }
      public int NodeId { get; set; }
      public string Name { get; set; }
      public string Address { get; set; }
      public string DeviceTypeCode { get; set; }
      public string Message1 { get; set; }
      public string Message2 { get; set; }
      public int Priority { get; set; }
      public long CreatedOn { get; set; }
      public long UpdatedOn { get; set; }
      public List<OutputDevice> OutputDevices { get; set; }

      public Point() {
         OutputDevices = new List<OutputDevice>();
      }

      public Point(CdefMessage cdefMessage) : this() {
         Id = cdefMessage.GetInt();
         NodeId = cdefMessage.GetInt();
         Name = cdefMessage.GetString();
         Address = cdefMessage.GetString();
         DeviceTypeCode = cdefMessage.GetString();
         Message1 = cdefMessage.GetString();
         Message2 = cdefMessage.GetString();
         Priority = cdefMessage.GetInt();
         CreatedOn = cdefMessage.GetLong();
         UpdatedOn = cdefMessage.GetLong();
         {
            var c = cdefMessage.GetInt();
            for (var i=0; i<c; i++) {
               OutputDevices.Add(new OutputDevice(cdefMessage));
            }
         }
      }

      public void Encode(CdefMessage cdefMessage) {
         cdefMessage.PutInt(Id);
         cdefMessage.PutInt(NodeId);
         cdefMessage.PutString(Name);
         cdefMessage.PutString(Address);
         cdefMessage.PutString(DeviceTypeCode);
         cdefMessage.PutString(Message1);
         cdefMessage.PutString(Message2);
         cdefMessage.PutInt(Priority);
         cdefMessage.PutLong(CreatedOn);
         cdefMessage.PutLong(UpdatedOn);
         cdefMessage.PutInt(OutputDevices.Count);
         foreach (var obj in OutputDevices) { obj.Encode(cdefMessage); }
      }
   }
}
