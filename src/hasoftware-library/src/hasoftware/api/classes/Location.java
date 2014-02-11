package hasoftware.api.classes;

import hasoftware.cdef.CDEFMessage;

public class Location {

    private int _id;
    private int _parentId;
    private String _name;
    private TimeUTC _createdOn;
    private TimeUTC _updatedOn;

    public Location(int id, int parentId, String name, TimeUTC createdOn, TimeUTC updatedOn) {
        _id = id;
        _parentId = parentId;
        _name = name;
        _createdOn = createdOn;
        _updatedOn = updatedOn;
    }

    public Location(CDEFMessage cdefMessage) {
        _id = cdefMessage.getU32();
        _parentId = cdefMessage.getU32();
        _name = cdefMessage.getAsciiL();
        _createdOn = new TimeUTC(cdefMessage.getS64());
        _updatedOn = new TimeUTC(cdefMessage.getS64());
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.putU32(_id);
        cdefMessage.putU32(_parentId);
        cdefMessage.putAsciiL(_name);
        cdefMessage.putS64(_createdOn == null ? 0 : _createdOn.getTimeUTC());
        cdefMessage.putS64(_updatedOn == null ? 0 : _updatedOn.getTimeUTC());
    }

    public int getId() {
        return _id;
    }

    public int getParentId() {
        return _parentId;
    }

    public String getName() {
        return _name;
    }

    public TimeUTC getCreatedOn() {
        return _createdOn;
    }

    public TimeUTC getUpdatedOn() {
        return _updatedOn;
    }

    public void setId(int id) {
        _id = id;
    }

    public void setParentId(int parentId) {
        _parentId = parentId;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setCreatedOn(TimeUTC createdOn) {
        _createdOn = createdOn;
    }

    public void setUpdatedOn(TimeUTC updatedOn) {
        _updatedOn = updatedOn;
    }
}
