package hasoftware.util;

public enum EventType {

    None(0, "None"),
    TimeCheck(1, "Timecheck"),
    Shutdown(2, "ShutDown"),
    Connect(3, "Connect"),
    Disconnect(4, "Disconnect"),
    ClientConnect(5, "Client Connect"),
    ClientDisconnect(6, "Client Disconnect"),
    SendMessage(7, "Send Message"),
    ReceiveMessage(8, "Recveive Message");

    private final int _id;
    private final String _code;

    EventType(int id, String code) {
        _id = id;
        _code = code;
    }

    public int getId() {
        return _id;
    }

    public String getCode() {
        return _code;
    }
}
