using hasoftware.Cdef;
using hasoftware.Messages;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace hasoftware.Api
{
    public class MessageFactory
    {
        public static Message Decode(CdefMessage cdefMessage)
        {
            Message message = null;
            int functionCode = cdefMessage.GetU16(0);
            int systemFlags = cdefMessage.GetU8(6);
            if (systemFlags == (CdefSystemFlags.Error | CdefSystemFlags.Response))
            {
                message = new ErrorResponse(cdefMessage);
            }
            else
            {
                bool isRequest = ((systemFlags & CdefSystemFlags.Response) == 0);
                switch (functionCode)
                {
                    case FunctionCode.Notify:
                        if (isRequest) message = new NotifyRequest(cdefMessage);
                        else message = new NotifyResponse(cdefMessage);
                        break;

                    case FunctionCode.InputMessage:
                        if (isRequest) message = new InputMessageRequest(cdefMessage);
                        else message = new InputMessageResponse(cdefMessage);
                        break;

                    case FunctionCode.OutputMessage:
                        if (isRequest) message = new OutputMessageRequest(cdefMessage);
                        else message = new OutputMessageResponse(cdefMessage);
                        break;

                    case FunctionCode.OutputDevice:
                        if (isRequest) message = new OutputDeviceRequest(cdefMessage);
                        else message = new OutputDeviceResponse(cdefMessage);
                        break;

                    case FunctionCode.Login:
                        if (isRequest) message = new LoginRequest(cdefMessage);
                        else message = new LoginResponse(cdefMessage);
                        break;

                    case FunctionCode.Location:
                        if (isRequest) message = new LocationRequest(cdefMessage);
                        else message = new LocationResponse(cdefMessage);
                        break;

                    case FunctionCode.Point:
                        if (isRequest) message = new PointRequest(cdefMessage);
                        else message = new PointResponse(cdefMessage);
                        break;

                    case FunctionCode.CurrentEvent:
                        if (isRequest) message = new CurrentEventRequest(cdefMessage);
                        else message = new CurrentEventResponse(cdefMessage);
                        break;
                }
            }
            return message;
        }
    }
}
