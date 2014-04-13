using hasoftware.Api;
using hasoftware.Cdef;
using hasoftware.Classes;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Messages
{
    public class OutputDeviceResponse : Message
    {
        public CdefAction Action { get; set; }
        public List<OutputDevice> OutputDevices { get;private set; }

        public OutputDeviceResponse(int transactionNumber)
            : base(Api.FunctionCode.OutputDevice, transactionNumber, CdefSystemFlags.Response)
        {
            Action = CdefAction.None;
            OutputDevices = new List<OutputDevice>();
        }

        public OutputDeviceResponse(CdefMessage cdefMessage)
            : base (cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            OutputDevices = new List<OutputDevice>();
            int countOutputDevices = cdefMessage.GetU8();
            for (int index = 0; index < countOutputDevices; index++) {
                OutputDevices.Add(new OutputDevice(cdefMessage));
            }
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(OutputDevices.Count);
            foreach (var outputDevice in OutputDevices) {
                outputDevice.Encode(cdefMessage);
            }
        }
    }
}
