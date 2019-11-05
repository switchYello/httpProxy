package com.brige;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.concurrent.Promise;

import java.util.List;

//服务器端返回0，表示连接成功，如果返回的不是0则断开连接
public class EnSuccessReplay extends ReplayingDecoder {

    private Promise<Channel> promise;

    public EnSuccessReplay(Promise<Channel> promise) {
        this.promise = promise;
    }
	
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        boolean i = byteBuf.readBoolean();
        if (i) {
            channelHandlerContext.pipeline().remove(this);
            promise.setSuccess(channelHandlerContext.channel());
        } else {
            channelHandlerContext.close();
        }
    }

}
