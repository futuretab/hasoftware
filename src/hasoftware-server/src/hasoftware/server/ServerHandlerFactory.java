package hasoftware.server;

import hasoftware.cdef.CDEFMessage;
import hasoftware.cdef.ICDEFServerHandlerFactory;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandlerFactory implements ICDEFServerHandlerFactory {

    @Override
    public SimpleChannelInboundHandler<CDEFMessage> create() {
        return new ServerHandler();
    }
}
