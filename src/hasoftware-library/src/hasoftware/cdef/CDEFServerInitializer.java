package hasoftware.cdef;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class CDEFServerInitializer extends ChannelInitializer<SocketChannel> {

    private final ICDEFServerHandlerFactory _handlerFactory;

    public CDEFServerInitializer(ICDEFServerHandlerFactory handlerFactory) {
        _handlerFactory = handlerFactory;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        //pipeline.addLast("deflator", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        //pipeline.addLast("inflator", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        pipeline.addLast("decoder", new CDEFDecoder());
        pipeline.addLast("encoder", new CDEFEncoder());
        pipeline.addLast("handler", _handlerFactory.create());
    }
}
