using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Classes
{
    public class Point
    {
        public int Id;
        public int NodeId;
        public string Name;
        public string Address;
        public string DeviceTypeCode;
        public string Message1;
        public string Message2;
        public int Priority;
        public TimeUtc CreatedOn;
        public TimeUtc UpdatedOn;
        public List<OutputDevice> OutputDevices;

        public Point(int id, int nodeId, String name, String address, String deviceTypeCode, String message1, String message2, int priority, TimeUtc createdOn, TimeUtc updatedOn)
        {
            Id = id;
            NodeId = nodeId;
            Name = name;
            Address = address;
            DeviceTypeCode = deviceTypeCode;
            Message1 = message1;
            Message2 = message2;
            Priority = priority;
            CreatedOn = createdOn;
            UpdatedOn = updatedOn;
            OutputDevices = new List<OutputDevice>();
        }

        public Point(CdefMessage cdefMessage)
        {
            Id = cdefMessage.GetU32();
            NodeId = cdefMessage.GetU32();
            Name = cdefMessage.GetAsciiL();
            Address = cdefMessage.GetAsciiL();
            DeviceTypeCode = cdefMessage.GetAsciiL();
            Message1 = cdefMessage.GetAsciiL();
            Message2 = cdefMessage.GetAsciiL();
            Priority = cdefMessage.GetU8();
            CreatedOn = new TimeUtc(cdefMessage.GetS64());
            UpdatedOn = new TimeUtc(cdefMessage.GetS64());
            OutputDevices = new List<OutputDevice>();
            int counter = cdefMessage.GetU8();
            for (int index = 0; index < counter; index++)
            {
                OutputDevices.Add(new OutputDevice(cdefMessage));
            }
        }

        public void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.PutU32(Id);
            cdefMessage.PutU32(NodeId);
            cdefMessage.PutAsciiL(Name);
            cdefMessage.PutAsciiL(Address);
            cdefMessage.PutAsciiL(DeviceTypeCode);
            cdefMessage.PutAsciiL(Message1);
            cdefMessage.PutAsciiL(Message2);
            cdefMessage.PutU8(Priority);
            cdefMessage.PutS64(CreatedOn == null ? 0 : CreatedOn.Time);
            cdefMessage.PutS64(UpdatedOn == null ? 0 : UpdatedOn.Time);
            cdefMessage.PutU8(OutputDevices.Count);
            foreach (var outputDevice in OutputDevices)
            {
                outputDevice.Encode(cdefMessage);
            }
        }
    }
}
