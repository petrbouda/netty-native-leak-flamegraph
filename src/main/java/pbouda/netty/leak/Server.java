package pbouda.netty.leak;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static final Logger LOG = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) {
        String releasingParam = args.length == 1
                ? args[0]
                : Boolean.FALSE.toString();

        int port = 8081;

        var broadcaster = new Bootstrapper(port, Boolean.parseBoolean(releasingParam));

        Channel serverChannel = broadcaster.start(f -> {
            LOG.info("PID {} - Handler started on the port '{}'",
                    ProcessHandle.current().pid(), port);
        });

        Runtime.getRuntime().addShutdownHook(new Thread(broadcaster::close));

        // A blocking operation which joins current thread and waits until
        // the websocket server is not consider to be fully closed.
        serverChannel.closeFuture()
                .addListener(f -> LOG.info("Server closed"))
                .syncUninterruptibly();
    }
}
