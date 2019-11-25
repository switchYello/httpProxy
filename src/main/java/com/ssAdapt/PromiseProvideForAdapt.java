package com.ssAdapt;


import com.dns.AsnycDns;
import com.handlers.IdleStateHandlerImpl;
import com.handlers.TransferHandler;
import com.start.PromiseProvide;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;

import javax.crypto.NoSuchPaddingException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class PromiseProvideForAdapt implements PromiseProvide {

    public static PromiseProvideForAdapt INSTANCE = new PromiseProvideForAdapt();

    @Override
    public ChannelFuture createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        Bootstrap b = new Bootstrap();
        return b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .resolver(AsnycDns.INSTANCE)
                .remoteAddress(address)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.AUTO_READ, false)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
                        ChannelPipeline p = channel.pipeline();
                        p.addLast(new IdleStateHandlerImpl(30, 30, 0));
                        p.addLast(new LoggingHandler("连接网站"));
                        p.addLast(new TransferHandler(ctx.channel()));
                    }
                })
                .connect();
    }
}