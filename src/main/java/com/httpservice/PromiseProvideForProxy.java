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
                        //这里将连接服务器的channel和连接client的channel进行绑定，如果一个断开另一个也断开
                        if (channelFuture.isSuccess()) {
                            Channel webChannel = channelFuture.channel();
                            Channel clientChannel = ctx.channel();
                            bindClose(webChannel, clientChannel);
                            ctx.pipeline().addLast(new TransferHandler(webChannel));
                            promise.setSuccess(webChannel);
                        } else {
                            ctx.close();
                            channelFuture.cancel(true);
                        }
                    }
                });
        return promise;
    }

    private void bindClose(final Channel c1, final Channel c2) {
        c1.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                ChannelUtil.closeOnFlush(c2);
            }
        });
        c2.closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                ChannelUtil.closeOnFlush(c1);
            }
        });
    }

}
