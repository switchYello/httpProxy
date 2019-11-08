package com.ssAdapt;

import com.handlers.TransferHandler;
import com.start.Context;
import com.utils.ChannelUtil;
import io.netty.channel.*;

import java.net.InetSocketAddress;

public class SSService extends ChannelInboundHandlerAdapter {

    private static PromiseProvideForAdapt provide = PromiseProvideForAdapt.INSTANCE;
    private String remoteHost = Context.getEnvironment().getRemoteHost();
    private int remotePort = Context.getEnvironment().getRemotePort();
    private ChannelFuture promise;
    //统计连接前 数据包的数量，如果超过限制则断开连接
    private int packageCount;

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        this.promise = provide.createPromise(InetSocketAddress.createUnresolved(remoteHost, remotePort), ctx);
        promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (!future.isSuccess()) {
                    ctx.fireExceptionCaught(future.cause());
                }
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
        //防止连接不上时有过多回掉函数
        if (packageCount++ > 10) {
            promise.cancel(true);
            ChannelUtil.closeOnFlush(ctx.channel());
        }
        promise.addListener(new ChannelFutureListener() {
            private final int id = packageCount;

            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) {
                    ChannelPipeline p = ctx.pipeline();
                    /*
                     * 初次接收到数据，则移除当前handler，添加TransferHandler，并写数据到对端
                     * */
                    if (id == 1) {
                        p.remove(SSService.this);
                        p.addLast(new TransferHandler(future.channel()));
                    }
                    ctx.fireChannelRead(msg);
                }
            }
        });
    }
}
