package hasoftware.kirk.messages;

public class StatusType {

    public static final byte Ind = 0x01;
    public static final byte ReadReq = 0x02;
    public static final byte ReadCfm = 0x03;
    public static final byte Res = 0x04;
    public static final byte RfpiReq = 0x05;
    public static final byte RfpiCfm = 0x06;
    public static final byte ListenInReq = 0x07;
    public static final byte ListenInCfm = 0x08;
    public static final byte ChargerStatusReq = 0x09;
    public static final byte ChargerStatusCfm = 0x0A;
    public static final byte ChargerInd = 0x0B; // 1 byte data 0x01 in charger, 0x02 out charger
    public static final byte Alarm = 0x0C;
    public static final byte RSSITransferReq = 0x0D;
    public static final byte RSSITransferCfm = 0x0E;

    public static final byte VersionReq = 0x15;
    public static final byte VersionCfm = 0x16;
    public static final byte StationsReq = 0x17;
    public static final byte StationsCfm = 0x18;
    public static final byte EnhancedStatusReq = 0x19;
    public static final byte EnhancedStatusInd = 0x1A;
    public static final byte ShutdownReq = 0x1B;
    public static final byte ShutdownCfm = 0x1C;
    public static final byte SetAlertVolReq = 0x1D;
    public static final byte SetAlertVolCfm = 0x1E;

    public static final byte AbsentModeReq = 0x20;
    public static final byte AbsetModeCfm = 0x21;

    public static final byte AlarmKeyPressed = 0x23;
    public static final byte GenericInfo = 0x24;
    public static final byte RfpsReq = 0x25;
    public static final byte RfpsCfm = 0x26;
}
