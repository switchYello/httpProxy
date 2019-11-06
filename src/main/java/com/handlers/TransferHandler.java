package com.handlers;

import com.utils.ChannelUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 数据转发，接收到数据，直接传输到outChannel中
 */
public class TransferHandler extends ChannelInboundHandlerAdapter {

    private Channel outChannel;

    public TransferHandler(Channel outChannel) {
        this.outChannel = outChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        outChannel.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        outChannel.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtil.closeOnFlush(outChannel);
        super.channelInactive(ctx);
    }
}
