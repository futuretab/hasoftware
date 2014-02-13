package hasoftware.util;

import java.util.concurrent.LinkedBlockingQueue;

public interface IEventCreator {

    boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue);
}
