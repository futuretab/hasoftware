package hasoftware.can;

public class CANMessageId {
    public static final int Timestamp = 0x001;
    public static final int IdentifyRequest = 0x002;
    public static final int IdentifyResponse = 0x003;
    public static final int ProgramRequest = 0x006;
    public static final int ProgramResponse = 0x007;
    public static final int AddressRequest = 0x008;
    public static final int AddressResponse = 0x009;
    public static final int WriteRequest = 0x00A;
    public static final int WriteResponse = 0x00B;
    public static final int ReadRequest = 0x00C;
    public static final int ReadResponse = 0x00D;
    public static final int Reboot = 0x00E;
    public static final int CallNotification = 0x100;
    public static final int AnalogNotification = 0x101;
    public static final int CancelNotification = 0x200;
    public static final int DebugNotification = 0x300;
    public static final int DeviceCommand = 0x400;    
}
