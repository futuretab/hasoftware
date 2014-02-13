package hasoftware.comms;

public interface IPortReader {
    void onReceive(byte[] buffer, int len);
}
