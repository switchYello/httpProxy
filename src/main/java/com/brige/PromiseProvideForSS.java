package com.brige;

import com.handlers.TransferHandler;
import com.start.PromiseProvide;
import com.utils.ChannelUtil;
import com.utils.ContextSSLFactory;
import com.utils.PropertiesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

public class PromiseProvideForSS implements PromiseProvide {

    private static final SslContext context;

    static {
        context = ContextSSLFactory.getSslContextClient();
    }

    @Override
    public Promise<Channel> createPromise(final InetSocketAddress address, final ChannelHandlerContext ctx) {
        final Promise<Channel> promise = ctx.executor().newPromise();
        Bootstrap b = new Bootstrap();
        b.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .remoteAddress(PropertiesUtil.getStrProp("service.host"), PropertiesUtil.getIntProp("service.port"))
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        ChannelPipeline p = channel.pipeline();

                        p.addLast(new SslHandler(context.newEngine(channel.alloc())));
                        p.addLast(new EnSuccessReplay(promise));
                        p.addLast(new TransferHandler(ctx.channel()));
                    }
                })
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000)
                .connect()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) {
                        if (channelFuture.isSuccess()) {
                            //绑定连接服务器的和连接浏览器的handler之间，要断都断开
                            final Channel webChannel = channelFuture.channel();//连接服务器的channel
                            Channel clientChannel = ctx.channel();
                            webChannel.closeFuture().addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) {
                                    ChannelUtil.closeOnFlush(ctx.channel());
                                }
                            });
                            clientChannel.closeFuture().addListener(new ChannelFutureListener() {
                                @Override
                                public void operationComplete(ChannelFuture future) {
                                    ChannelUtil.closeOnFlush(webChannel);
                                }
                            });
                            String host = address.getHostName();
                            int port = address.getPort();
                            ByteBuf buffer = Unpooled.buffer(4 + host.length());
                            buffer.writeShort(host.length());
                            buffer.writeCharSequence(host, CharsetUtil.UTF_8);
                            buffer.writeShort(port);
                            webChannel.writeAndFlush(buffer)
                                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                        } else {
                            ctx.close();
                            channelFuture.cancel(true);
                        }
                    }
                });
        return promise;
    }
}
