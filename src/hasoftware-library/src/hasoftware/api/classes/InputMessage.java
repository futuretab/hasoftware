package hasoftware.api.classes;

import hasoftware.cdef.CDEFMessage;

public class InputMessage {

    private int _id;
    private String _deviceTypeCode;
    private String _data;
    private TimeUTC _createdOn;

    public InputMessage() {
    }

    public InputMessage(int id, String deviceTypeCode, String data, TimeUTC createdOn) {
        _id = id;
        _deviceTypeCode = deviceTypeCode;
        _data = data;
        _createdOn = createdOn;
    }

    public InputMessage(CDEFMessage cdefMessage) {
        _id = cdefMessage.getU32();
        _deviceTypeCode = cdefMessage.getAsciiL();
        _data = cdefMessage.getAsciiL();
        _createdOn = new TimeUTC(cdefMessage.getS64());
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.putU32(_id);
        cdefMessage.putAsciiL(_deviceTypeCode);
        cdefMessage.putAsciiL(_data);
        cdefMessage.putS64(_createdOn == null ? 0 : _createdOn.getTimeUTC());
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public String getDeviceTypeCode() {
        return _deviceTypeCode;
    }

    public void setDeviceTypeCode(String deviceTypeCode) {
        _deviceTypeCode = deviceTypeCode;
    }

    public String getData() {
        return _data;
    }

    public void setData(String data) {
        _data = data;
    }

    public TimeUTC getCreatedOn() {
        return _createdOn;
    }

    public void setCreatedOn(TimeUTC createdOn) {
        _createdOn = createdOn;
    }
}
