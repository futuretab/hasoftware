package hasoftware.api;

public enum DeviceType {

    UNKNNOWN(0, "UNKNOWN"),
    NASCOCG(260, "NASCOCG"), //     NASCO CAN Gatway
    POINT(200, "POINT"), //         Point
    NASCOCAIM(201, "NASCOCAIM"), // NASCO CAN Analog Input Module
    NASCOCOM(202, "NASCOCOM"), //   NASCO CAN Output Module
    KIRKDECT(261, "KIRKDECT"), //   Kirk DECT phone
    EMAIL(263, "EMAIL"), //         Email Address
    ANDROID(265, "ANDROID"), //     Andriod Modile Phone (via Notify My Android)
    SMS(262, "SMS"), //             Mobile Phone (via SMS Web Service)
    SMS2(268, "SMS2"), //           Mobile Phone (via F1103)
    PAGER(264, "PAGER"), //         Alphanumeric Pager
    TEMP(266, "TEMP"), //           Temperature Sensor
    SENSOR(267, "SENSOR"); //       Sensor

    private final int _id;
    private final String _code;

    DeviceType(int id, String code) {
        _id = id;
        _code = code;
    }

    public int getId() {
        return _id;
    }

    public String getCode() {
        return _code;
    }

    private static DeviceType[] allDeviceTypes;

    public static DeviceType fromId(int id) {
        if (allDeviceTypes == null) {
            allDeviceTypes = DeviceType.values();
        }
        for (int i = 0; i < allDeviceTypes.length; i++) {
            if (allDeviceTypes[i]._id == id) {
                return allDeviceTypes[i];
            }
        }
        return DeviceType.UNKNNOWN;
    }
}
