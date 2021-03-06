//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 3:02:01 PM
//

#include <stdlib.h>

#include "CdefMessages.h"
#include "FunctionCodes.h"
#include "MessageFactory.h"
#include "Messages.h"

using namespace HASoftware;

namespace Lockheed {
   MessageBase* MessageFactory::Decode(CdefMessage &cdefMessage) {
      MessageBase* message = NULL;
      int functionCode = cdefMessage.GetInt(0);
      int transactionNumber = cdefMessage.GetInt();
      int systemFlags = cdefMessage.GetInt();
      if (systemFlags == (CdefSystemFlags::Error | CdefSystemFlags::Response)) {
         message = new ErrorResponse(cdefMessage);
      } else if (functionCode == CdefFunctionCode::Heartbeat && systemFlags == CdefSystemFlags::Request) {
         message = new HeartbeatRequest(cdefMessage);
      } else if (functionCode == CdefFunctionCode::Notify && systemFlags == CdefSystemFlags::Request) {
         message = new NotifyRequest(cdefMessage);
      } else if (functionCode == CdefFunctionCode::Notify && systemFlags == CdefSystemFlags::Response) {
         message = new NotifyResponse(cdefMessage);
      } else {
         bool isRequest = ((systemFlags & CdefSystemFlags::Response) == 0);
         switch (functionCode) {
         case FunctionCode::InputMessage:
            if (isRequest) message = new InputMessageRequest(cdefMessage);
            if (!isRequest) message = new InputMessageResponse(cdefMessage);
                break;
         case FunctionCode::OutputMessage:
            if (isRequest) message = new OutputMessageRequest(cdefMessage);
            if (!isRequest) message = new OutputMessageResponse(cdefMessage);
                break;
         case FunctionCode::Login:
            if (isRequest) message = new LoginRequest(cdefMessage);
            if (!isRequest) message = new LoginResponse(cdefMessage);
                break;
         case FunctionCode::OutputDevice:
            if (isRequest) message = new OutputDeviceRequest(cdefMessage);
            if (!isRequest) message = new OutputDeviceResponse(cdefMessage);
                break;
         case FunctionCode::Location:
            if (isRequest) message = new LocationRequest(cdefMessage);
            if (!isRequest) message = new LocationResponse(cdefMessage);
                break;
         case FunctionCode::Point:
            if (isRequest) message = new PointRequest(cdefMessage);
            if (!isRequest) message = new PointResponse(cdefMessage);
                break;
         case FunctionCode::CurrentEvent:
            if (isRequest) message = new CurrentEventRequest(cdefMessage);
            if (!isRequest) message = new CurrentEventResponse(cdefMessage);
                break;
         }
      }
      return message;
    }
}
