package com.handlers;

import com.utils.ChannelUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ConnectTimeoutException;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

/**
 * 处理超时事件
 * 处理异常
 */
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelInboundHandlerAdapter {

    public static ExceptionHandler INSTANSE = new ExceptionHandler();

    private static Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    private ExceptionHandler() {

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ChannelUtil.closeOnFlush(ctx.channel());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof UnknownHostException) {

        } else if (cause instanceof ConnectTimeoutException) {

        } else {
            log.debug("exceptionCaught", cause);
        }
        ChannelUtil.closeOnFlush(ctx.channel());
    }
}
