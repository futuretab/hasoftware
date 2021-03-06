package hasoftware.server.data.auto;

import java.util.List;

import org.apache.cayenne.CayenneDataObject;

import hasoftware.server.data.Device;
import hasoftware.server.data.DeviceType;

/**
 * Class _OutputDevice was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _OutputDevice extends CayenneDataObject {

    public static final String ADDRESS_PROPERTY = "address";
    public static final String CREATED_ON_PROPERTY = "createdOn";
    public static final String DESCRIPTION_PROPERTY = "description";
    public static final String NAME_PROPERTY = "name";
    public static final String SERIAL_NUMBER_PROPERTY = "serialNumber";
    public static final String UPDATED_ON_PROPERTY = "updatedOn";
    public static final String DEVICE_TYPE_PROPERTY = "deviceType";
    public static final String DEVICES_PROPERTY = "devices";

    public static final String ID_PK_COLUMN = "ID";

    public void setAddress(String address) {
        writeProperty(ADDRESS_PROPERTY, address);
    }
    public String getAddress() {
        return (String)readProperty(ADDRESS_PROPERTY);
    }

    public void setCreatedOn(Long createdOn) {
        writeProperty(CREATED_ON_PROPERTY, createdOn);
    }
    public Long getCreatedOn() {
        return (Long)readProperty(CREATED_ON_PROPERTY);
    }

    public void setDescription(String description) {
        writeProperty(DESCRIPTION_PROPERTY, description);
    }
    public String getDescription() {
        return (String)readProperty(DESCRIPTION_PROPERTY);
    }

    public void setName(String name) {
        writeProperty(NAME_PROPERTY, name);
    }
    public String getName() {
        return (String)readProperty(NAME_PROPERTY);
    }

    public void setSerialNumber(String serialNumber) {
        writeProperty(SERIAL_NUMBER_PROPERTY, serialNumber);
    }
    public String getSerialNumber() {
        return (String)readProperty(SERIAL_NUMBER_PROPERTY);
    }

    public void setUpdatedOn(Long updatedOn) {
        writeProperty(UPDATED_ON_PROPERTY, updatedOn);
    }
    public Long getUpdatedOn() {
        return (Long)readProperty(UPDATED_ON_PROPERTY);
    }

    public void setDeviceType(DeviceType deviceType) {
        setToOneTarget(DEVICE_TYPE_PROPERTY, deviceType, true);
    }

    public DeviceType getDeviceType() {
        return (DeviceType)readProperty(DEVICE_TYPE_PROPERTY);
    }


    public void addToDevices(Device obj) {
        addToManyTarget(DEVICES_PROPERTY, obj, true);
    }
    public void removeFromDevices(Device obj) {
        removeToManyTarget(DEVICES_PROPERTY, obj, true);
    }
    @SuppressWarnings("unchecked")
    public List<Device> getDevices() {
        return (List<Device>)readProperty(DEVICES_PROPERTY);
    }


}
