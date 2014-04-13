package hasoftware.server.web;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

public class ServerWebSocketServer {

    public static final int PORT = 6970;

    public static void main(String[] args) throws Exception {
        final HttpServer server = HttpServer.createSimpleServer("server/webapp/", PORT);
        server.getListener("grizzly").registerAddOn(new WebSocketAddOn());
        final WebSocketApplication serverApplication = new ServerApplication();
        WebSocketEngine.getEngine().register("/hasoftware", "/server", serverApplication);
        try {
            server.start();
            System.out.println("Press any key to shutdownNow the server...");
            System.in.read();
        } finally {
            server.shutdownNow();
        }
    }
}
