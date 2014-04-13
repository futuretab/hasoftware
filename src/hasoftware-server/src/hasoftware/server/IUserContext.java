package hasoftware.server;

import hasoftware.server.data.User;

public interface IUserContext {

    User getUser();

    void setUser(User user);

    INotificationTarget getTarget();
}
