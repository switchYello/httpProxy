package com.start;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

/**
 * 根据ip 端口 创建连接
 */
public interface PromiseProvide {

    ChannelFuture createPromise(InetSocketAddress addr, ChannelHandlerContext ctx);

}
