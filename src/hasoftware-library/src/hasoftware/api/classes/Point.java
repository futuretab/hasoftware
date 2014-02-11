package hasoftware.api.classes;

import hasoftware.cdef.CDEFMessage;
import java.util.Collection;
import java.util.LinkedList;

public class Point {

    private int _id;
    private int _nodeId;
    private String _name;
    private String _address;
    private String _deviceTypeCode;
    private String _message1;
    private String _message2;
    private TimeUTC _createdOn;
    private TimeUTC _updatedOn;
    private Collection<OutputDevice> _outputDevices = new LinkedList<>();

    public Point(int id, int nodeId, String name, String address, String deviceTypeCode, String message1, String message2, TimeUTC createdOn, TimeUTC updatedOn) {
        _id = id;
        _nodeId = nodeId;
        _name = name;
        _address = address;
        _deviceTypeCode = deviceTypeCode;
        _message1 = message1;
        _message2 = message2;
        _createdOn = createdOn;
        _updatedOn = updatedOn;
    }

    public Point(CDEFMessage cdefMessage) {
        _id = cdefMessage.getU32();
        _nodeId = cdefMessage.getU32();
        _name = cdefMessage.getAsciiL();
        _address = cdefMessage.getAsciiL();
        _deviceTypeCode = cdefMessage.getAsciiL();
        _message1 = cdefMessage.getAsciiL();
        _message2 = cdefMessage.getAsciiL();
        _createdOn = new TimeUTC(cdefMessage.getS64());
        _updatedOn = new TimeUTC(cdefMessage.getS64());
        int counter = cdefMessage.getU8();
        for (int index = 0; index < counter; index++) {
            _outputDevices.add(new OutputDevice(cdefMessage));
        }
    }

    public void encode(CDEFMessage cdefMessage) {
        cdefMessage.putU32(_id);
        cdefMessage.putU32(_nodeId);
        cdefMessage.putAsciiL(_name);
        cdefMessage.putAsciiL(_address);
        cdefMessage.putAsciiL(_deviceTypeCode);
        cdefMessage.putAsciiL(_message1);
        cdefMessage.putAsciiL(_message2);
        cdefMessage.putS64(_createdOn == null ? 0 : _createdOn.getTimeUTC());
        cdefMessage.putS64(_updatedOn == null ? 0 : _updatedOn.getTimeUTC());
        cdefMessage.putU8(_outputDevices.size());
        for (OutputDevice outputDevice : _outputDevices) {
            outputDevice.encode(cdefMessage);
        }
    }

    public int getId() {
        return _id;
    }

    public int getNodeId() {
        return _nodeId;
    }

    public String getName() {
        return _name;
    }

    public String getAddress() {
        return _address;
    }

    public String getDeviceTypeCode() {
        return _deviceTypeCode;
    }

    public String getMessage1() {
        return _message1;
    }

    public String getMessage2() {
        return _message2;
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

    public void setNodeId(int nodeId) {
        _nodeId = nodeId;
    }

    public void setName(String name) {
        _name = name;
    }

    public void setAddress(String address) {
        _address = address;
    }

    public void setDeviceTypeCode(String deviceTypeCode) {
        _deviceTypeCode = deviceTypeCode;
    }

    public void setMessage1(String message1) {
        _message1 = message1;
    }

    public void setMessage2(String message2) {
        _message2 = message2;
    }

    public void setCreatedOn(TimeUTC createdOn) {
        _createdOn = createdOn;
    }

    public void setUpdatedOn(TimeUTC updatedOn) {
        _updatedOn = updatedOn;
    }

    public Collection<OutputDevice> getOutputDevices() {
        return _outputDevices;
    }
}
