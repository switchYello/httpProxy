package com.httpservice;

import com.handlers.ExceptionHandler;
import com.handlers.TransferHandler;
import com.start.Context;
import com.start.PromiseProvide;
import io.netty.channel.*;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

/**
 * 用于代理服务，生成子连接
 */
public class PromiseProvideForProxy implements PromiseProvide {

    private Context context = Context.getNow();

    @Override
    public Promise<Channel> createPromise(InetSocketAddress addr, final ChannelHandlerContext ctx) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        context.createBootStrap(ctx.channel().eventLoop())
                .remoteAddress(addr)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new TransferHandler(ctx.channel()));
                        ch.pipeline().addLast(ExceptionHandler.INSTANSE);
                    }
                })
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) {
                        if (channelFuture.isSuccess()) {
                            promise.setSuccess(channelFuture.channel());
                        } else {
                            if (channelFuture.cause() != null) {
                                throw new RuntimeException(channelFuture.cause());
                            }
                            promise.cancel(true);
                            channelFuture.cancel(false);
                        }
                    }
                });
        return promise;
    }


}
