package hasoftware.comms;

public interface IPort {
    void setPortReader(IPortReader portReader);
    boolean write(byte[] buffer, int offset, int len);
    void close();
}
