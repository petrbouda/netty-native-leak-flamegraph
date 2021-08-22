package pbouda.netty.leak;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.util.ReferenceCountUtil;

public class HttpRequestReleasingHandler extends AbstractHttpRequestHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpMessage message) {
            try {
                writeResponse(message, ctx, "Releasing");
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
