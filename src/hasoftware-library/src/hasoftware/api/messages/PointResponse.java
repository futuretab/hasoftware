package hasoftware.api.messages;

import hasoftware.api.FunctionCode;
import hasoftware.api.Message;
import hasoftware.api.classes.Point;
import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.CDEFSystemFlags;
import java.util.LinkedList;
import java.util.List;

public class PointResponse extends Message {

    private int _action;

    private final List<Point> _points = new LinkedList<>();

    public PointResponse(int transactionNumber, int action) {
        super(FunctionCode.Point, transactionNumber, CDEFSystemFlags.Response);
        _action = action;
    }

    public PointResponse(CDEFMessage cdefMessage) {
        super(cdefMessage);
        _action = cdefMessage.getU8();
        int countPoints = cdefMessage.getU8();
        for (int index = 0; index < countPoints; index++) {
            _points.add(new Point(cdefMessage));
        }
    }

    @Override
    public void encode(CDEFMessage cdefMessage) {
        super.encode(cdefMessage);
        cdefMessage.putU8(_action);
        cdefMessage.putU8(_points.size());
        for (Point point : _points) {
            point.encode(cdefMessage);
        }
    }

    public int getAction() {
        return _action;
    }

    public void setAction(int action) {
        _action = action;
    }

    public List<Point> getPoints() {
        return _points;
    }
}
