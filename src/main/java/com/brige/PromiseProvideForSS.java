package com.brige;

import com.handlers.ExceptionHandler;
import com.handlers.TransferHandler;
import com.start.Context;
import com.start.PromiseProvide;
import com.utils.ContextSSLFactory;
import io.netty.channel.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class PromiseProvideForSS implements PromiseProvide {

    private static Logger log = LoggerFactory.getLogger(PromiseProvideForSS.class);
    private static SslContext CONTEXT = ContextSSLFactory.getSslContextClient();
    private String remoteHost = Context.getEnvironment().getRemoteHost();
    private int remotePort = Context.getEnvironment().getRemotePort();
    private Context context = Context.getNow();

    @Override
    public Promise<Channel> createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        final long startTime = System.currentTimeMillis();
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
                        p.addLast(ExceptionHandler.INSTANSE);
                    }
                })
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) {
                        if (log.isDebugEnabled()) {
                            log.debug("远程服务器:{},目标地址:{},耗时:{},是否成功:{}", remoteHost, address, System.currentTimeMillis() - startTime, future.isSuccess());
                        }
                        if (future.isSuccess()) {
                            //连接成功了
                        } else {
                            promise.cancel(true);
                            future.cancel(false);
                            if (future.cause() != null) {
                                throw new RuntimeException(future.cause());
                            }
                        }
                    }
                });

        return promise;
    }
}
