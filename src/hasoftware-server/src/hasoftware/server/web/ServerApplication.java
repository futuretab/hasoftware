package hasoftware.server.web;

import hasoftware.api.Message;
import hasoftware.api.MessageFactory;
import hasoftware.server.Notifications;
import hasoftware.server.ServerLogic;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApplication extends WebSocketApplication {

    private final static Logger logger = LoggerFactory.getLogger(ServerApplication.class);

    @Override
    public WebSocket createSocket(ProtocolHandler protocolHandler, HttpRequestPacket httpRequestPacket, WebSocketListener... listeners) {
        return new ServerWebSocket(protocolHandler, httpRequestPacket, listeners);
    }

    @Override
    public void onMessage(WebSocket webSocket, String data) {
        ServerWebSocket serverWebSocket = (ServerWebSocket) webSocket;
        Message request = MessageFactory.decodeJson(data);
        Message response = ServerLogic.getInstance().process(serverWebSocket, request);
        if (response != null) {
            response.setTransactionNumber(request.getTransactionNumber());      // Copy transaction numbers
            serverWebSocket.send(MessageFactory.encodeJson(response));
        }
    }

    @Override
    public void onConnect(WebSocket webSocket) {
    }

    @Override
    public void onClose(WebSocket webSocket, DataFrame frame) {
        ServerWebSocket serverWebSocket = (ServerWebSocket) webSocket;
        Notifications.remove(serverWebSocket);
    }
}
