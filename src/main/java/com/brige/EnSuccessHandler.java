package com.brige;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;

//服务器端返回0，表示连接成功，如果返回的不是0则断开连接
public class EnSuccessHandler extends ChannelInboundHandlerAdapter {

    private Promise<Channel> promise;

    public EnSuccessHandler(Promise<Channel> promise) {
        this.promise = promise;
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
            ctx.close();
        }

    }


}
