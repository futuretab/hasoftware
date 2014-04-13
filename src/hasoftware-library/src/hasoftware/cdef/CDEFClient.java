package hasoftware.cdef;

import hasoftware.api.Message;
import hasoftware.configuration.Configuration;
import hasoftware.util.AbstractController;
import hasoftware.util.Event;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDEFClient extends AbstractController {

    private final static Logger logger = LoggerFactory.getLogger(CDEFClient.class);
    private final static String ConfigurationSectionServer = "Server";

    private final int ConnectDelay = 2;

    private final EventLoopGroup _eventLoopGroup;
    private final CDEFClientHandler _clientHandler;
    private LinkedBlockingQueue<Event> _eventQueue;
    private final LinkedList<Message> _messageQueue;
    private final String _host;
    private final int _port;
    private boolean _connected;

    public CDEFClient(Configuration configuration) {
        _host = configuration.getSectionString(ConfigurationSectionServer, "Host", null);
        _port = configuration.getSectionInt(ConfigurationSectionServer, "Port", -1);
        _eventLoopGroup = new NioEventLoopGroup();
        _clientHandler = new CDEFClientHandler(this);
        _messageQueue = new LinkedList<>();
        _connected = false;
    }

    @Override
    public boolean startUp() {
        logger.debug("startUp");
        if (_eventQueue == null) {
            logger.error("EventQueue not set");
            return false;
        }

        if (_port == -1 || _host == null) {
            logger.error("[Server] section details not set Host:{} Port:{}", _host, _port);
            return false;
        }

        _eventLoopGroup.schedule(new Runnable() {
            @Override
            public void run() {
                connect();
            }
        }, ConnectDelay, TimeUnit.SECONDS);
        return true;
    }

    private boolean connect() {
        logger.debug("Connecting to server {}:{}", _host, _port);
        final ChannelFuture channelFuture = configureBootstrap(new Bootstrap(), _eventLoopGroup).connect();
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    final EventLoop eventLoop = future.channel().eventLoop();
                    final ChannelFutureListener parent = this;
                    eventLoop.schedule(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("Connecting to server {}:{}", _host, _port);
                            configureBootstrap(new Bootstrap(), eventLoop).connect().addListener(parent);
                        }
                    }, ConnectDelay, TimeUnit.SECONDS);
                }
            }
        });
        return true;
    }

    @Override
    public boolean readyToShutDown() {
        logger.debug("readyToShutDown");
        return true;
    }

    @Override
    public boolean shutDown() {
        logger.debug("shutDown");
        _eventLoopGroup.shutdownGracefully();
        return true;
    }

    @Override
    public boolean setEventQueue(LinkedBlockingQueue<Event> eventQueue) {
        logger.debug("setEventQueue");
        _eventQueue = eventQueue;
        _clientHandler.setEventQueue(_eventQueue);
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        switch (event.getType()) {
            case TimeCheck:
                break;

            case Connect:
                _connected = true;
                trySendingMessages();
                break;

            case SendMessage:
                _messageQueue.add(event.getMessage());
                trySendingMessages();
                break;

            case Disconnect:
                _connected = false;
                _eventLoopGroup.schedule(new Runnable() {
                    @Override
                    public void run() {
                        connect();
                    }
                }, ConnectDelay, TimeUnit.SECONDS);
                break;
        }
        return true;
    }

    private void trySendingMessages() {
        if (_connected) {
            while (!_messageQueue.isEmpty() && _clientHandler.send(_messageQueue.peekFirst())) {
                _messageQueue.removeFirst();
            }
        }
    }

    public Bootstrap configureBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoop) {
        bootstrap.group(eventLoop)
                .channel(NioSocketChannel.class
                )
                .remoteAddress(_host, _port)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("decoder", new CDEFDecoder());
                        pipeline.addLast("encoder", new CDEFEncoder());
                        pipeline.addLast("handler", _clientHandler);
                    }
                }
                );
        return bootstrap;
    }
}
