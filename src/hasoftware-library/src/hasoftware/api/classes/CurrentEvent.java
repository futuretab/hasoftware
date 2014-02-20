package hasoftware.api.classes;

import hasoftware.cdef.CDEFMessage;

public class CurrentEvent {

    private int _id;
    private int _pointId;
    private TimeUTC _createdOn;
    private TimeUTC _updatedOn;

    public CurrentEvent() {
    }

    public CurrentEvent(int id, int pointId, TimeUTC createdOn, TimeUTC updatedOn) {
        _id = id;
        _pointId = pointId;
        _createdOn = createdOn;
        _updatedOn = updatedOn;
    }

    public CurrentEvent(CDEFMessage cdefMessage) {
        _id = cdefMessage.getU32();
        _pointId = cdefMessage.getU32();
        _createdOn = new TimeUTC(cdefMessage.getS64());
        _updatedOn = new TimeUTC(cdefMessage.getS64());
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.putU32(_id);
        cdefMessage.putU32(_pointId);
        cdefMessage.putS64(_createdOn == null ? 0 : _createdOn.getTimeUTC());
        cdefMessage.putS64(_updatedOn == null ? 0 : _updatedOn.getTimeUTC());
    }

    public int getId() {
        return _id;
    }

    public int getPointId() {
        return _pointId;
    }

    public TimeUTC getCreatedOn() {
        return _createdOn;
    }

    public TimeUTC getUpdatedOn() {
        return _updatedOn;
    }
}
