package hasoftware.server.web;

import hasoftware.api.Message;
import hasoftware.api.MessageFactory;
import hasoftware.server.INotificationTarget;
import hasoftware.server.IUserContext;
import hasoftware.server.data.User;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DefaultWebSocket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerWebSocket extends DefaultWebSocket implements IUserContext, INotificationTarget {

    private final static Logger logger = LoggerFactory.getLogger(ServerWebSocket.class);

    private static int HandlerId = 1;

    private final String _id;
    private User _user;

    public ServerWebSocket(ProtocolHandler protocolHandler, HttpRequestPacket httpRequestPacket, WebSocketListener... listeners) {
        super(protocolHandler, httpRequestPacket, listeners);
        _id = "WS" + HandlerId++;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public User getUser() {
        return _user;
    }

    @Override
    public void setUser(User user) {
        _user = user;
    }

    public INotificationTarget getTarget() {
        return this;
    }

    @Override
    public void send(Message message) {
        send(MessageFactory.encodeJson(message));
    }
}
