package hasoftware.mobile;

public class PhoneMessageInfo {

    private final int _id;
    private final String _phoneNumber;
    private final String _message;
    private boolean _sent;
    private long _sentAt;
    private int _retries;

    public PhoneMessageInfo(int id, String phoneNumber, String message) {
        _id = id;
        _phoneNumber = phoneNumber;
        _message = message;
        _sent = false;
        _sentAt = 0;
        _retries = 0;
    }

    public int getId() {
        return _id;
    }

    public String getPhoneNumber() {
        return _phoneNumber;
    }

    public String getMessage() {
        return _message;
    }

    public void setSent(boolean sent) {
        _sent = sent;
        if (sent) {
            _sentAt = System.currentTimeMillis();
        } else {
            _sentAt = 0;
        }
    }

    public boolean isSent() {
        return _sent;
    }

    public long getSentAt() {
        return _sentAt;
    }

    public boolean canRetry(int maxRetries) {
        _retries++;
        return (_retries < maxRetries);
    }
}
