package com.dns;

import io.netty.channel.EventLoop;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.AddressResolver;
import io.netty.resolver.AddressResolverGroup;
import io.netty.resolver.dns.DnsNameResolver;
import io.netty.resolver.dns.DnsNameResolverBuilder;
import io.netty.util.concurrent.EventExecutor;

/**
 * 异步dns解析
 */
public class AsnycDns extends AddressResolverGroup {

    public static AsnycDns INSTANCE = new AsnycDns();

    @Override
    protected AddressResolver newResolver(EventExecutor executor) {
        assertTrue(EventLoop.class.isAssignableFrom(executor.getClass()), "异步dns EventExecutor 转 EventLoop 类型不匹配");
        return getResolver0((EventLoop) executor).asAddressResolver();
    }

    private DnsNameResolver getResolver0(EventLoop eventLoop) {
        return new DnsNameResolverBuilder(eventLoop)
                .channelType(NioDatagramChannel.class)
                .maxQueriesPerResolve(1)
                .optResourceEnabled(false)
                .ndots(1)
                .build();
    }

    private void assertTrue(boolean instance, String msg) {
        if (!instance) {
            throw new RuntimeException(msg);
        }
    }

}
