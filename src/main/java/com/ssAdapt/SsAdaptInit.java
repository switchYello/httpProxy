package com.ssAdapt;

import com.handlers.IdleStateHandlerImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.TimeUnit;

/**
 * 初始化handler
 */
public class SsAdaptInit extends ChannelInitializer<Channel> {

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandlerImpl(30, 0, 0, TimeUnit.SECONDS));
        p.addLast(new LoggingHandler("连接客户端"));
        p.addLast(SSService.INSTANCE);
    }


}
