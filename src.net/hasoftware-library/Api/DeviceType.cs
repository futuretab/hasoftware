using System.Collections.Generic;

namespace hasoftware.Api
{
    public class DeviceType
    {
        public static readonly DeviceType UNKNNOWN = new DeviceType(0, "UNKNOWN");
        public static readonly DeviceType NASCOCG = new DeviceType(260, "NASCOCG");     // NASCO CAN Gatway
        public static readonly DeviceType POINT = new DeviceType(200, "POINT");         // Point
        public static readonly DeviceType NASCOCAIM = new DeviceType(201, "NASCOCAIM"); // NASCO CAN Analog Input Module
        public static readonly DeviceType NASCOCOM = new DeviceType(202, "NASCOCOM");   // NASCO CAN Output Module
        public static readonly DeviceType KIRKDECT = new DeviceType(261, "KIRKDECT");   // Kirk DECT phone
        public static readonly DeviceType EMAIL = new DeviceType(263, "EMAIL");         // Email Address
        public static readonly DeviceType ANDROID = new DeviceType(265, "ANDROID");     // Andriod Modile Phone (via Notify My Android)
        public static readonly DeviceType SMS = new DeviceType(262, "SMS");             // Mobile Phone (via SMS Message)
        public static readonly DeviceType PAGER = new DeviceType(264, "PAGER");         // Alphanumeric Pager
        public static readonly DeviceType TEMP = new DeviceType(266, "TEMP");           // Temperature Sensor
        public static readonly DeviceType SENSOR = new DeviceType(267, "SENSOR");       // Sensor

        private static readonly List<DeviceType> AllDeviceTypes = new List<DeviceType>();

        public int Id { get; private set; }
        public string Code { get; private set; }

        private DeviceType(int id, string code)
        {
            Id = id;
            Code = Code;
            AllDeviceTypes.Add(this);
        }

        public static DeviceType FromId(int id)
        {
            foreach (var deviceType in AllDeviceTypes)
            {
                if (deviceType.Id == id) return deviceType;
            }
            return UNKNNOWN;
        }
    }
}
