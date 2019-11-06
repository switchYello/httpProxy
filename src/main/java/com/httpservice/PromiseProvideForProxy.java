package com.httpservice;

import com.handlers.TransferHandler;
import com.start.PromiseProvide;
import com.utils.ChannelUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

/**
 * 用于代理服务，生成子连接
 */
public class PromiseProvideForProxy implements PromiseProvide {

    private static Bootstrap b = new Bootstrap();

    static {
        b.channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    @Override
    public Promise<Channel> createPromise(InetSocketAddress addr, final ChannelHandlerContext ctx) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        b.clone(ctx.channel().eventLoop())
                .remoteAddress(addr)
                .handler(new TransferHandler(ctx.channel()))
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) {
                        if (channelFuture.isSuccess()) {
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            ChannelUtil.closeOnFlush(ctx.channel());
                            channelFuture.cancel(false);
                        }
                    }
                });
        return promise;
    }


}
