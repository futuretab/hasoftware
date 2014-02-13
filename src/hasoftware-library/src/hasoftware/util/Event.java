package hasoftware.util;

import hasoftware.api.Message;

public class Event {

    private final EventType _type;
    private long _time;
    private Message _message;

    public Event(EventType type) {
        this(type, null);
    }

    public Event(EventType type, Message message) {
        _type = type;
        _message = message;
        _time = System.currentTimeMillis();
    }

    public EventType getType() {
        return _type;
    }

    public void setTime(long time) {
        _time = time;
    }

    public long getTime() {
        return _time;
    }

    public Message getMessage() {
        return _message;
    }

    public void setMessage(Message message) {
        _message = message;
    }
}
