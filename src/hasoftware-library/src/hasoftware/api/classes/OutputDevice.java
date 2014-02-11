package hasoftware.api.classes;

import hasoftware.cdef.CDEFMessage;

public class OutputDevice {

    private int _id;
    private String _name;
    private String _description;
    private String _address;
    private String _deviceTypeCode;
    private String _serialNumber;
    private TimeUTC _createdOn;
    private TimeUTC _updatedOn;

    public OutputDevice(int id, String name, String description, String address, String deviceTypeCode, String serialNumber, TimeUTC createdOn, TimeUTC updatedOn) {
        _id = id;
        _name = name;
        _description = description;
        _address = address;
        _deviceTypeCode = deviceTypeCode;
        _serialNumber = serialNumber;
        _createdOn = createdOn;
        _updatedOn = updatedOn;
    }

    public OutputDevice(CDEFMessage cdefMessage) {
        _id = cdefMessage.getU32();
        _name = cdefMessage.getAsciiL();
        _description = cdefMessage.getAsciiL();
        _address = cdefMessage.getAsciiL();
        _deviceTypeCode = cdefMessage.getAsciiL();
        _serialNumber = cdefMessage.getAsciiL();
        _createdOn = new TimeUTC(cdefMessage.getS64());
        _updatedOn = new TimeUTC(cdefMessage.getS64());
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.putU32(_id);
        cdefMessage.putAsciiL(_name);
        cdefMessage.putAsciiL(_description);
        cdefMessage.putAsciiL(_address);
        cdefMessage.putAsciiL(_deviceTypeCode);
        cdefMessage.putAsciiL(_serialNumber);
        cdefMessage.putS64(_createdOn == null ? 0 : _createdOn.getTimeUTC());
        cdefMessage.putS64(_updatedOn == null ? 0 : _updatedOn.getTimeUTC());
    }

    public int getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public String getDescription() {
        return _description;
    }

    public String getAddress() {
        return _address;
    }

    public String getDeviceTypeCode() {
        return _deviceTypeCode;
    }

    public String getSerialNumber() {
        return _serialNumber;
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

    public void setName(String name) {
        _name = name;
    }

    public void setDescription(String description) {
        _description = description;
    }

    public void setAddress(String address) {
        _address = address;
    }

    public void setDeviceTypeCode(String deviceTypeCode) {
        _deviceTypeCode = deviceTypeCode;
    }

    public void setSerialNumber(String serialNumber) {
        _serialNumber = serialNumber;
    }

    public void setCreatedOn(TimeUTC createdOn) {
        _createdOn = createdOn;
    }

    public void setUpdatedOn(TimeUTC updatedOn) {
        _updatedOn = updatedOn;
    }
}
