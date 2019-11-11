package com.ssAdapt;

import com.handlers.TransferHandler;
import com.start.Environment;
import io.netty.channel.*;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public class SSService extends ChannelInboundHandlerAdapter {

    public static SSService INSTANCE = new SSService();
    private static PromiseProvideForAdapt provide = PromiseProvideForAdapt.INSTANCE;
    private String remoteHost = Environment.getRemoteHost();
    private int remotePort = Environment.getRemotePort();

    //尝试连接服务器。连接成功后进行读取
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        ChannelFuture promise = provide.createPromise(InetSocketAddress.createUnresolved(remoteHost, remotePort), ctx);
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ctx.pipeline().addAfter("ssservice", "transfer", new TransferHandler(future.channel()));
                    ctx.read();
                } else {
                    ctx.close();
                }
            }
        });
    }

}
