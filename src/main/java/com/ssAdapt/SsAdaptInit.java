package com.ssAdapt;

import com.handlers.ExceptionHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/**
 * 初始化handler
 */
public class SsAdaptInit extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        p.addLast(new SSService());
        p.addLast(ExceptionHandler.INSTANCE);

    }


}
