package hasoftware.can;

public class Command {
    public static final byte Ok = 0x08;
    public static final byte Error = 0x09;
    public static final byte Status = 0x0E;
    public static final byte GoOnline = 0x32;
    public static final byte GoOffline = 0x33;
    public static final byte SendMessage = 0x34;
    public static final byte ReceivedMessage = 0x35;
}
