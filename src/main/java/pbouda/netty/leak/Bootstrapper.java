package pbouda.netty.leak;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.ImmediateEventExecutor;

public class Bootstrapper implements AutoCloseable {

    private final ServerBootstrap bootstrap;
    private final EventLoopGroup bossEventLoopGroup;
    private final EventLoopGroup workerEventLoopGroup;

    public Bootstrapper(int port, boolean releasing) {
        this.bossEventLoopGroup = new EpollEventLoopGroup(1, new NamedThreadFactory("server-accept"));
        this.workerEventLoopGroup = new EpollEventLoopGroup(0, new NamedThreadFactory("server-io"));

        this.bootstrap = new ServerBootstrap()
                .channel(EpollServerSocketChannel.class)
                .group(workerEventLoopGroup)
                .localAddress(port)
                // .handler(new LoggingHandler(LogLevel.INFO))
                // .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)
                // .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                // .childOption(ChannelOption.SO_BACKLOG, 256)
                .childHandler(new RouterChannelInitializer(releasing));

        /*
         * The maximum queue length for incoming connection indications
         * (a request to connect) is set to the backlog parameter. If
         * a connection indication arrives when the queue is full,
         * the connection is refused.
         */
        // bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        // bootstrap.handler(new LoggingHandler(LogLevel.INFO));


        // Receive and Send Buffer - always be able to fill in an entire entity.
        // bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);
    }

    public Channel start(GenericFutureListener<? extends Future<? super Void>> listener) {
        ChannelFuture serverBindFuture = bootstrap.bind();
        serverBindFuture.addListener(listener);
        // Wait for the binding is completed
        serverBindFuture.syncUninterruptibly();
        return serverBindFuture.channel();
    }

    @Override
    public void close() {
        Future<?> boss = bossEventLoopGroup.shutdownGracefully();
        Future<?> workers = workerEventLoopGroup.shutdownGracefully();
        boss.syncUninterruptibly();
        workers.syncUninterruptibly();
    }
}
