package hasoftware.can;

public interface ICANMessageReceiver {
    void onReceive(CANMessage message);
}
