package hasoftware.server;

import hasoftware.api.Message;
import hasoftware.api.MessageFactory;
import hasoftware.cdef.CDEFMessage;
import hasoftware.server.data.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerHandler extends SimpleChannelInboundHandler<CDEFMessage> {

    private final static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private static int HandlerId = 1;

    private ChannelHandlerContext _context;
    private SocketUserContext _userContext;

    public ServerHandler() {
        _context = null;
        _userContext = null;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        _context = context;
        _userContext = new SocketUserContext(this, "IP" + HandlerId++);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        Notifications.remove(_userContext);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CDEFMessage message) throws Exception {
        Message request = MessageFactory.decode(message);
        Message response = ServerLogic.getInstance().process(_userContext, request);
        if (response != null) {
            response.setTransactionNumber(request.getTransactionNumber());      // Copy transaction numbers
            CDEFMessage cdefMessage = new CDEFMessage();
            response.encode(cdefMessage);                                       // Encode response into a CDEFMessage
            ctx.writeAndFlush(cdefMessage);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        logger.error("Unexpected exception from downstream.", cause);
        context.close();
    }

    public void send(Message message) {
        CDEFMessage cdefMessage = new CDEFMessage();
        message.encode(cdefMessage);                                            // Encode response into a CDEFMessage
        _context.writeAndFlush(cdefMessage);
    }

    class SocketUserContext implements IUserContext, INotificationTarget {

        private final ServerHandler _serverHandler;
        private final String _id;
        private User _user;

        public SocketUserContext(ServerHandler serverHandler, String id) {
            _serverHandler = serverHandler;
            _id = id;
            _user = null;
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
            _serverHandler.send(message);
        }
    }
}
