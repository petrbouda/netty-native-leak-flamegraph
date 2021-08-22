package pbouda.netty.leak;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpMessage;

public class HttpRequestLeakingHandler extends AbstractHttpRequestHandler {

    private long counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpMessage message) {
            if (counter % 2 == 0) {
                writeResponse(message, ctx, "Leaking");
            } else {
                ctx.fireChannelRead(message);
            }
            counter++;
        }
    }
}
