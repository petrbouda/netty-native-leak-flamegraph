package pbouda.netty.leak;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class RouterChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final boolean releasing;

    public RouterChannelInitializer(boolean releasing) {
        this.releasing = releasing;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpServerCodec());

        // For Streaming of Continuation frames
        // https://tools.ietf.org/html/rfc6455#section-5.4
        // A single WebSocket frame, per RFC-6455 base framing, has a maximum size limit of 2^63 bytes
        // (9,223,372,036,854,775,807 bytes ~= 9.22 exabytes)
        // pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(64 * 1024));

        // pipeline.addLast(new IdleStateHandler(60, 30, 0));
        // children: ReadTimeoutHandler & WriteTimeoutHandler

        pipeline.addLast(new HttpRequestLeakingHandler());
        pipeline.addLast(new HttpRequestReleasingHandler());

        // Sec-WebSocket-Extensions: permessage-deflate
        // pipeline.addLast(new WebSocketServerCompressionHandler());
        // pipeline.addLast(new WebSocketServerProtocolHandler(websocketPath, null, true));
        // pipeline.addLast(new WebsocketEventHandler(channelGroup));
        // pipeline.addLast(new SlowConsumerDisconnectHandler());
    }
}
