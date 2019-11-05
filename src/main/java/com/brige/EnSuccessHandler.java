package com.brige;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

//服务器端返回0，表示连接成功，如果返回的不是0则断开连接
public class EnSuccessHandler extends ChannelInboundHandlerAdapter {

    private Promise<Channel> promise;
    private InetSocketAddress address;

    public EnSuccessHandler(Promise<Channel> promise, InetSocketAddress address) {
        this.promise = promise;
        this.address = address;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String host = address.getHostName();
        int port = address.getPort();
        ByteBuf buffer = Unpooled.buffer(4 + host.length());
        buffer.writeShort(host.length());
        buffer.writeCharSequence(host, CharsetUtil.UTF_8);
        buffer.writeShort(port);
        ctx.writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        boolean i = byteBuf.readBoolean();
        if (i) {
            ctx.pipeline().remove(this);
            promise.setSuccess(ctx.channel());
            super.channelRead(ctx, msg);
        } else {
            ReferenceCountUtil.release(msg);
            ctx.close();
        }
    }


}
