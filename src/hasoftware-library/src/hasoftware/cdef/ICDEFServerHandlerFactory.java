package hasoftware.cdef;

import io.netty.channel.SimpleChannelInboundHandler;

public interface ICDEFServerHandlerFactory {

    SimpleChannelInboundHandler<CDEFMessage> create();
}
