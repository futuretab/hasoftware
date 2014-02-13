package hasoftware.kirk.messages;

public class Nag {

    public static Nag Unknown = new Nag(-1, "Unknown");
    public static Nag Busy = new Nag(0, "Busy, not ready to receive data, try again later");
    public static Nag NotReady = new Nag(1, "Not ready to receive data, data must be send first");
    public static Nag TransmissionError = new Nag(2, "Transmission error");
    public static Nag Idle = new Nag(3, "Idle, no data to transmit");

    private final int _errorCode;
    private final String _description;

    private Nag(int errorCode, String description) {
        _errorCode = errorCode;
        _description = description;
    }

    public int getErrorCode() {
        return _errorCode;
    }

    public String getDescription() {
        return _description;
    }

    public static Nag find(int errorCode) {
        switch (errorCode) {
            case 0:
                return Busy;
            case 1:
                return NotReady;
            case 2:
                return TransmissionError;
            case 3:
                return Idle;
        }
        return Unknown;
    }
}
