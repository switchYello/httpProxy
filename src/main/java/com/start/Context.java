package com.start;

import com.dns.AsnycDns;
import com.ssAdapt.SsAdaptInit;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 表示整个程序上下文
 */
public class Context {
    private static Logger log = LoggerFactory.getLogger(Context.class);
    private static Context contextHolder;
    private static boolean init;
    private Environment environment;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workGroup;
    private static Bootstrap b = new Bootstrap();

    static {
        b.channel(NioSocketChannel.class).resolver(AsnycDns.INSTANCE).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
    }

    public Context(Environment environment, int threadCount) {
        if (init) {
            throw new RuntimeException("alread existence Context");
        }
        this.environment = environment;
        contextHolder = this;
        bossGroup = new NioEventLoopGroup(1);
        workGroup = new NioEventLoopGroup(threadCount);
        init = true;
    }

    public static Context getNow() {
        if (!init) {
            throw new RuntimeException("not find contxt");
        }
        return contextHolder;
    }

    public static Environment getEnvironment() {
        if (!init) {
            throw new RuntimeException("not find contxt");
        }
        return contextHolder.environment;
    }

    public void start() {
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .childHandler(new SsAdaptInit());
            log.info("start at:{}", environment.getLocalPort());
            ChannelFuture f = b.bind(environment.getLocalPort()).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public Bootstrap createBootStrap() {
        return b.clone(workGroup);
    }

    public Bootstrap createBootStrap(EventLoopGroup group) {
        return b.clone(group);
    }

}
