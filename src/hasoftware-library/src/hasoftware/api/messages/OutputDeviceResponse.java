//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 11/04/2015 2:56:09 PM
//

package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.classes.*;
import hasoftware.api.Message;
import hasoftware.cdef.*;
import java.util.LinkedList;
import java.util.List;

public class OutputDeviceResponse extends Message {
   private int _action;
   private List<OutputDevice> _outputDevices = new LinkedList<>();

   public OutputDeviceResponse(int transactionNumber) {
      super(FunctionCode.OutputDevice, transactionNumber, CDEFSystemFlags.Response);
   }

   public OutputDeviceResponse(CDEFMessage cdefMessage) {
      super(cdefMessage);
      _action = cdefMessage.getInt();
      {
         int c = cdefMessage.getInt();
         for (int i=0; i<c; i++) {
            _outputDevices.add(new OutputDevice(cdefMessage));
         }
      }
   }

   @Override
   public void encode(CDEFMessage cdefMessage) {
      super.encode(cdefMessage);
      cdefMessage.putInt(_action);
      cdefMessage.putInt(_outputDevices.size());
      for (OutputDevice obj : _outputDevices) {
         obj.encode(cdefMessage);
      }
   }

   public int getAction() { return _action; }
   public void setAction(int action) { _action = action; }

   public List<OutputDevice> getOutputDevices() { return _outputDevices; }
   public void setOutputDevices(List<OutputDevice> outputDevices) { _outputDevices = outputDevices; }
}
