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

public class PointResponse extends Message {
   private int _action;
   private List<Point> _points = new LinkedList<>();

   public PointResponse(int transactionNumber) {
      super(FunctionCode.Point, transactionNumber, CDEFSystemFlags.Response);
   }

   public PointResponse(CDEFMessage cdefMessage) {
      super(cdefMessage);
      _action = cdefMessage.getInt();
      {
         int c = cdefMessage.getInt();
         for (int i=0; i<c; i++) {
            _points.add(new Point(cdefMessage));
         }
      }
   }

   @Override
   public void encode(CDEFMessage cdefMessage) {
      super.encode(cdefMessage);
      cdefMessage.putInt(_action);
      cdefMessage.putInt(_points.size());
      for (Point obj : _points) {
         obj.encode(cdefMessage);
      }
   }

   public int getAction() { return _action; }
   public void setAction(int action) { _action = action; }

   public List<Point> getPoints() { return _points; }
   public void setPoints(List<Point> points) { _points = points; }
}
