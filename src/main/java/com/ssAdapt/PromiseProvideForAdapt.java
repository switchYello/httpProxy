package com.ssAdapt;


import com.dns.AsnycDns;
import com.handlers.ExceptionHandler;
import com.handlers.Rc4Handler;
import com.handlers.TransferHandler;
import com.start.PromiseProvide;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.NoSuchPaddingException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PromiseProvideForAdapt implements PromiseProvide {

    private static Logger log = LoggerFactory.getLogger(PromiseProvideForAdapt.class);
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
                        p.addLast(new Rc4Handler());
                        p.addLast(new LoggingHandler());
                        //p.addLast(new SslHandler(CONTEXT.newEngine(channel.alloc())));
                        p.addLast(new TransferHandler(ctx.channel()));
                        p.addLast(ExceptionHandler.INSTANCE);
                    }
                })
                .connect();
    }
}