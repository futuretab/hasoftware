package hasoftware.server.data.auto;

import org.apache.cayenne.CayenneDataObject;

import hasoftware.server.data.Device;

/**
 * Class _ActiveEvent was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _ActiveEvent extends CayenneDataObject {

    public static final String CREATED_ON_PROPERTY = "createdOn";
    public static final String UPDATED_ON_PROPERTY = "updatedOn";
    public static final String DEVICE_PROPERTY = "device";

    public static final String ID_PK_COLUMN = "ID";

    public void setCreatedOn(Long createdOn) {
        writeProperty(CREATED_ON_PROPERTY, createdOn);
    }
    public Long getCreatedOn() {
        return (Long)readProperty(CREATED_ON_PROPERTY);
    }

    public void setUpdatedOn(Long updatedOn) {
        writeProperty(UPDATED_ON_PROPERTY, updatedOn);
    }
    public Long getUpdatedOn() {
        return (Long)readProperty(UPDATED_ON_PROPERTY);
    }

    public void setDevice(Device device) {
        setToOneTarget(DEVICE_PROPERTY, device, true);
    }

    public Device getDevice() {
        return (Device)readProperty(DEVICE_PROPERTY);
    }


}
