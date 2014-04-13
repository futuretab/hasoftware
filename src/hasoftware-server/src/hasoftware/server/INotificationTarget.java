package hasoftware.server;

import hasoftware.api.Message;

public interface INotificationTarget {

    String getId();

    void send(Message message);
}
