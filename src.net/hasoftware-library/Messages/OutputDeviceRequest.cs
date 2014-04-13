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
    public class OutputDeviceRequest : Message
    {
        public CdefAction Action { get; set; }
        public List<int> Ids { get; private set; }
        public List<OutputDevice> OutputDevices { get; private set; }

        public OutputDeviceRequest()
            : this(CdefAction.None)
        {
        }

        public OutputDeviceRequest(CdefAction action)
           : base(Api.FunctionCode.OutputDevice, 0)
        {
            Action = action;
            Ids = new List<int>();
            OutputDevices = new List<OutputDevice>();
        }

        public OutputDeviceRequest(CdefMessage cdefMessage)
            : base(cdefMessage)
        {
            Action = (CdefAction)cdefMessage.GetU8();
            Ids = new List<int>();
            int countIds = cdefMessage.GetU8();
            for (int index = 0; index < countIds; index++) {
                Ids.Add(cdefMessage.GetU32());
            }
            OutputDevices = new List<OutputDevice>();
            int countOutputDevices = cdefMessage.GetU8();
            for (int index = 0; index < countOutputDevices; index++) {
                OutputDevices.Add(new OutputDevice(cdefMessage));
            }
        }

        public OutputDeviceResponse CreateResponse() {
            OutputDeviceResponse response = new OutputDeviceResponse(TransactionNumber);
            response.Action = Action;
            return response;
        }

        public override void Encode(CdefMessage cdefMessage) {
            base.Encode(cdefMessage);
            cdefMessage.PutU8((int)Action);
            cdefMessage.PutU8(Ids.Count);
            foreach (var id in Ids) {
                cdefMessage.PutU32(id);
            }
            cdefMessage.PutU8(OutputDevices.Count);
            foreach (var outputDevice in OutputDevices) {
                outputDevice.Encode(cdefMessage);
            }
        }
    }
}
