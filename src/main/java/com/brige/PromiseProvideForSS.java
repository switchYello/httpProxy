package com.brige;

import com.handlers.TransferHandler;
import com.start.Context;
import com.start.PromiseProvide;
import com.utils.ChannelUtil;
import com.utils.ContextSSLFactory;
import io.netty.channel.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

public class PromiseProvideForSS implements PromiseProvide {

    private static SslContext CONTEXT = ContextSSLFactory.getSslContextClient();
    private String remoteHost = Context.getEnvironment().getRemoteHost();
    private int remotePort = Context.getEnvironment().getRemotePort();
    private Context context = Context.getNow();

    @Override
    public Promise<Channel> createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        context.createBootStrap(ctx.channel().eventLoop())
                .remoteAddress(remoteHost, remotePort)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new SslHandler(CONTEXT.newEngine(channel.alloc())));
                        p.addLast(new EnSuccessHandler(promise, address));
                        p.addLast(new TransferHandler(ctx.channel()));
                    }
                })
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (!future.isSuccess()) {
                            ChannelUtil.closeOnFlush(ctx.channel());
                            future.cancel(false);
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
