package com.httpservice;

import com.start.PromiseProvide;
import com.handlers.TransferHandler;
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

    @Override
    public Promise<Channel> createPromise(InetSocketAddress addr, final ChannelHandlerContext ctx) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .remoteAddress(addr)
                .handler(new TransferHandler(ctx.channel()))
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) {
                        //这里将连接服务器的channel和连接client的channel进行绑定，如果一个断开另一个也断开
                        if (channelFuture.isSuccess()) {
                            final Channel webChannel = channelFuture.channel();
                            final Channel clientChannel = ctx.channel();
                            webChannel.closeFuture().addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) {
                                    ChannelUtil.closeOnFlush(clientChannel);
                                }
                            });
                            clientChannel.closeFuture().addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) {
                                    ChannelUtil.closeOnFlush(webChannel);
                                }
                            });
                            promise.setSuccess(webChannel);
                        } else {
                            ctx.close();
                            channelFuture.cancel(true);
                        }
                    }
                });
        return promise;
    }
}
