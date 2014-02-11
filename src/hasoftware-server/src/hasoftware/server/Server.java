package hasoftware.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import hasoftware.cdef.CDEFServerInitializer;
import hasoftware.configuration.Configuration;
import hasoftware.server.data.DataManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private final static String ApplicationName = "server";
    private static Logger logger;

    public static void main(String[] args) {
        // Reconfigure and reload the default logger configuration
        System.setProperty("app_name", ApplicationName);
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        ContextInitializer ci = new ContextInitializer(lc);
        lc.reset();
        try {
            ci.autoConfig();
        } catch (JoranException je) {
            System.err.println(je.getMessage());
        }
        Server.logger = LoggerFactory.getLogger(Server.class);
        new Server().run();
    }

    private final static String ConfigurationFilename = "hasoftware.ini";
    private final static String ConfigurationSection = "Server";
    private final static int DefaultPort = 6969;

    public Server() {
    }

    public void run() {
        try {
            logger.info("Server initializing");
            Configuration config = new Configuration();
            if (!config.open(ConfigurationFilename)) {
                logger.error("Can't load configuration file - " + ConfigurationFilename);
                return;
            }
            DataManager dataManager = DataManager.instance();
            if (!dataManager.initialize()) {
                logger.error("Can't initialize DataManager");
                return;
            }
            config.setSection(ConfigurationSection);
            int listenPort = config.getInt("Port", DefaultPort);
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                logger.info("Running ...");
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new CDEFServerInitializer(new ServerHandlerFactory()));
                b.bind(listenPort).sync().channel().closeFuture().sync();
            } finally {
                logger.info("Closing ...");
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        logger.info("Server complete");
    }
}
