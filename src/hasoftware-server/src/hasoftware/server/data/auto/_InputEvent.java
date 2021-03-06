package hasoftware.server.data.auto;

import org.apache.cayenne.CayenneDataObject;

import hasoftware.server.data.DeviceType;

/**
 * Class _InputEvent was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _InputEvent extends CayenneDataObject {

    public static final String CREATED_ON_PROPERTY = "createdOn";
    public static final String DATA_PROPERTY = "data";
    public static final String DEVICE_TYPE_PROPERTY = "deviceType";

    public static final String ID_PK_COLUMN = "ID";

    public void setCreatedOn(long createdOn) {
        writeProperty(CREATED_ON_PROPERTY, createdOn);
    }
    public long getCreatedOn() {
        Object value = readProperty(CREATED_ON_PROPERTY);
        return (value != null) ? (Long) value : 0;
    }

    public void setData(String data) {
        writeProperty(DATA_PROPERTY, data);
    }
    public String getData() {
        return (String)readProperty(DATA_PROPERTY);
    }

    public void setDeviceType(DeviceType deviceType) {
        setToOneTarget(DEVICE_TYPE_PROPERTY, deviceType, true);
    }

    public DeviceType getDeviceType() {
        return (DeviceType)readProperty(DEVICE_TYPE_PROPERTY);
    }


}
