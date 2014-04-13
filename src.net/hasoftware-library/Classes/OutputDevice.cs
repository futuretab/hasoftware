using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Classes
{
    public class OutputDevice
    {
        public int Id { get; private set; }
        public String Name { get; set; }
        public String Description { get; set; }
        public String Address { get; set; }
        public String DeviceTypeCode { get; set; }
        public String SerialNumber { get; set; }
        public TimeUtc CreatedOn { get; set; }
        public TimeUtc UpdatedOn { get; set; }

        public OutputDevice(int id, String name, String description, String address, String deviceTypeCode, String serialNumber, TimeUtc createdOn, TimeUtc updatedOn)
        {
            Id = id;
            Name = name;
            Description = description;
            Address = address;
            DeviceTypeCode = deviceTypeCode;
            SerialNumber = serialNumber;
            CreatedOn = createdOn;
            UpdatedOn = updatedOn;
        }

        public OutputDevice(CdefMessage cdefMessage)
        {
            Id = cdefMessage.GetU32();
            Name = cdefMessage.GetAsciiL();
            Description = cdefMessage.GetAsciiL();
            Address = cdefMessage.GetAsciiL();
            DeviceTypeCode = cdefMessage.GetAsciiL();
            SerialNumber = cdefMessage.GetAsciiL();
            CreatedOn = new TimeUtc(cdefMessage.GetS64());
            UpdatedOn = new TimeUtc(cdefMessage.GetS64());
        }

        public void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.PutU32(Id);
            cdefMessage.PutAsciiL(Name);
            cdefMessage.PutAsciiL(Description);
            cdefMessage.PutAsciiL(Address);
            cdefMessage.PutAsciiL(DeviceTypeCode);
            cdefMessage.PutAsciiL(SerialNumber);
            cdefMessage.PutS64(CreatedOn == null ? 0 : CreatedOn.Time);
            cdefMessage.PutS64(UpdatedOn == null ? 0 : UpdatedOn.Time);
        }
    }
}
