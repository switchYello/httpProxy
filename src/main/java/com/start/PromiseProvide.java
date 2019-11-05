package com.start;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

public interface PromiseProvide {

    Promise<Channel> createPromise(InetSocketAddress addr, ChannelHandlerContext ctx);

}
