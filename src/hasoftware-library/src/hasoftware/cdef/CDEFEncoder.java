package hasoftware.cdef;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CDEFEncoder extends MessageToByteEncoder<CDEFMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, CDEFMessage message, ByteBuf out) throws Exception {
        byte[] data = message.getBytes();
        int length = message.getLength();
        out.writeInt(length);
        out.writeBytes(data, 0, length);
    }
}
