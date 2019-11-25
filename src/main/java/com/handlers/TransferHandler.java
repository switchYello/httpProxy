package com.handlers;

import com.utils.ChannelUtil;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据转发，接收到数据，直接传输到outChannel中
 */
public class TransferHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(TransferHandler.class);
    private Channel outChannel;

    public TransferHandler(Channel outChannel) {
        this.outChannel = outChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (outChannel.isActive()) {
            outChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (future.isSuccess()) {
                        ctx.read();
                    }
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtil.closeOnFlush(outChannel);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug("", cause);
        super.exceptionCaught(ctx, cause);
    }
}
