using hasoftware.Cdef;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Classes
{
    public class OutputMessage
    {
        public int Id { get; set; }
        public string DeviceTypeCode { get; set; }
        public string Data { get; set; }
        public TimeUtc CreatedOn { get; set; }

        public OutputMessage()
        {
        }

        public OutputMessage(int id, String deviceTypeCode, String data, TimeUtc createdOn)
        {
            Id = id;
            DeviceTypeCode = deviceTypeCode;
            Data = data;
            CreatedOn = createdOn;
        }

        public OutputMessage(CdefMessage cdefMessage)
        {
            Id = cdefMessage.GetU32();
            DeviceTypeCode = cdefMessage.GetAsciiL();
            Data = cdefMessage.GetAsciiL();
            CreatedOn = new TimeUtc(cdefMessage.GetS64());
        }

        public void Encode(CdefMessage cdefMessage)
        {
            cdefMessage.PutU32(Id);
            cdefMessage.PutAsciiL(DeviceTypeCode);
            cdefMessage.PutAsciiL(Data);
            cdefMessage.PutS64(CreatedOn == null ? 0 : CreatedOn.Time);
        }
    }
}
