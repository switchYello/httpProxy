package com.ssAdapt;


import com.handlers.ExceptionHandler;
import com.handlers.TransferHandler;
import com.start.Context;
import com.start.PromiseProvide;
import com.utils.ContextSSLFactory;
import io.netty.channel.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class PromiseProvideForAdapt implements PromiseProvide {

    private static Logger log = LoggerFactory.getLogger(PromiseProvideForAdapt.class);
    public static PromiseProvideForAdapt INSTANCE = new PromiseProvideForAdapt();
    private static SslContext CONTEXT = ContextSSLFactory.getSslContextClient();
    private Context context = Context.getNow();

    @Override
    public ChannelFuture createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        return context.createBootStrap(ctx.channel().eventLoop())
                .remoteAddress(address)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new SslHandler(CONTEXT.newEngine(channel.alloc())));
                        p.addLast(new TransferHandler(ctx.channel()));
                        p.addLast(ExceptionHandler.INSTANCE);
                    }
                })
                .connect();
    }
}