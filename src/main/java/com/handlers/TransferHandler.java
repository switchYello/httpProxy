package com.handlers;

import com.utils.ChannelUtil;
import io.netty.channel.*;

/**
 * 数据转发，接收到数据，直接传输到outChannel中
 */
public class TransferHandler extends ChannelInboundHandlerAdapter {

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
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        ctx.read();
                    } else {
                        outChannel.close();
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
}
