//
// DO NOT EDIT THIS FILE - CONSTRUCTED ON 28/04/2014 1:27:52 AM
//

package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.classes.*;
import hasoftware.api.Message;
import hasoftware.cdef.*;
import java.util.LinkedList;
import java.util.List;

public class CurrentEventResponse extends Message {
   private int _action;
   private List<CurrentEvent> _currentEvents = new LinkedList<>();

   public CurrentEventResponse(int transactionNumber) {
      super(FunctionCode.CurrentEvent, transactionNumber, CDEFSystemFlags.Response);
   }

   public CurrentEventResponse(CDEFMessage cdefMessage) {
      super(cdefMessage);
      _action = cdefMessage.getInt();
      {
         int c = cdefMessage.getInt();
         for (int i=0; i<c; i++) {
            _currentEvents.add(new CurrentEvent(cdefMessage));
         }
      }
   }

   @Override
   public void encode(CDEFMessage cdefMessage) {
      super.encode(cdefMessage);
      cdefMessage.putInt(_action);
      cdefMessage.putInt(_currentEvents.size());
      for (CurrentEvent obj : _currentEvents) {
         obj.encode(cdefMessage);
      }
   }

   public int getAction() { return _action; }
   public void setAction(int action) { _action = action; }

   public List<CurrentEvent> getCurrentEvents() { return _currentEvents; }
   public void setCurrentEvents(List<CurrentEvent> currentEvents) { _currentEvents = currentEvents; }
}
