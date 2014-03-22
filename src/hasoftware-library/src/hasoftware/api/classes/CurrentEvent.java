package hasoftware.api.classes;

import hasoftware.cdef.CDEFMessage;

public class CurrentEvent {

    private int _id;
    private Point _point;
    private TimeUTC _createdOn;
    private TimeUTC _updatedOn;

    public CurrentEvent() {
    }

    public CurrentEvent(int id, Point point, TimeUTC createdOn, TimeUTC updatedOn) {
        _id = id;
        _point = point;
        _createdOn = createdOn;
        _updatedOn = updatedOn;
    }

    public CurrentEvent(CDEFMessage cdefMessage) {
        _id = cdefMessage.getU32();
        _point = new Point(cdefMessage);
        _createdOn = new TimeUTC(cdefMessage.getS64());
        _updatedOn = new TimeUTC(cdefMessage.getS64());
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.putU32(_id);
        _point.encode(cdefMessage);
        cdefMessage.putS64(_createdOn == null ? 0 : _createdOn.getTimeUTC());
        cdefMessage.putS64(_updatedOn == null ? 0 : _updatedOn.getTimeUTC());
    }

    public int getId() {
        return _id;
    }

    public Point getPoint() {
        return _point;
    }

    public TimeUTC getCreatedOn() {
        return _createdOn;
    }

    public TimeUTC getUpdatedOn() {
        return _updatedOn;
    }
}
