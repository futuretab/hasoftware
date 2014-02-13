package hasoftware.cdef;

import hasoftware.api.Message;
import hasoftware.api.MessageFactory;
import hasoftware.util.Event;
import hasoftware.util.EventType;
import hasoftware.util.IEventCreator;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.net.ConnectException;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable
public class CDEFClientHandler extends SimpleChannelInboundHandler<CDEFMessage> implements IEventCreator {

    private final static Logger logger = LoggerFactory.getLogger(CDEFClientHandler.class);

    private final CDEFClient _cdefClient;
    private LinkedBlockingQueue<Event> _eventQueue;
    private ChannelHandlerContext _context;

    public CDEFClientHandler(CDEFClient cdefClient) {
        _cdefClient = cdefClient;
        _context = null;
    }

    @Override
    public void channelActive(ChannelHandlerContext context) {
        _context = context;
        _eventQueue.add(new Event(EventType.Connect));
    }

    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        _context = null;
        _eventQueue.add(new Event(EventType.Disconnect));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        if (cause instanceof ConnectException) {
            logger.error("Connect Exception: {}", cause.getMessage());
        }
        context.close();
    }

    public boolean send(Message message) {
        if (_context == null) {
            return false;
        }
        CDEFMessage request = new CDEFMessage();
        message.encode(request);
        _context.write(request);
        _context.flush();
        return true;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, CDEFMessage message) {
        Message response = MessageFactory.decode(message);
        if (response.isResponse()) {                                            // Process response messages
            //if (response.isError()) {
            //    ErrorResponse errorResponse = (ErrorResponse) response;
            //    logger.error("CDEFClientHandler received ErrorResponse");
            //    for (int i = 0; i < errorResponse.getErrors().size(); i++) {
            //        hasoftware.api.AnError error = errorResponse.getErrors().get(i);
            //        logger.error("  {} - {} {}", error.getNumber(), error.getCode(), error.getMessage());
            //    }
            //} else {
            Event event = new Event(EventType.ReceiveMessage);
            event.setMessage(response);
            _eventQueue.add(event);
            //}
        } else {
            logger.error("RECV request!");
        }
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        _eventQueue = eventQueue;
        return true;
    }
}
