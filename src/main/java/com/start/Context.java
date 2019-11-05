package com.start;

import com.brige.BrigeInit;
import com.httpservice.ProxyServiceInit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 表示整个程序上下文
 */
public class Context {
    private static Logger log = LoggerFactory.getLogger(Context.class);
    private static final String DEFAULT_ENVIRONMENT_PARAM = "param.properties";
    private Environment environment;
    private static Context contextHolder;
    private static boolean init;

    public Context() {
        this(DEFAULT_ENVIRONMENT_PARAM);
    }

    public Context(String param) {
        if (init) {
            throw new RuntimeException("alread existence Context");
        }
        log.info("param fileName:{}", param);
        this.environment = new Environment(param);
        contextHolder = this;
        init = true;
    }

    public static Context getNow() {
        if (!init) {
            throw new RuntimeException("not find contxt");
        }
        return contextHolder;
    }

    public void start() {
        start(1);
    }

    public void start(int threadCount) {
        log.info("start thread count:{}", threadCount);
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(threadCount);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .childHandler(getInitializer());
            ChannelFuture f = b.bind(environment.getLocalPort()).sync();
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


    private ChannelInitializer<Channel> getInitializer() {
        log.info("proxy Type:{}", environment.getProxyType());
        if (environment.getProxyType() == Environment.ProxyType.proxy) {
            return new ProxyServiceInit();
        }
        if (environment.getProxyType() == Environment.ProxyType.brige) {
            return new BrigeInit();
        }
        throw new RuntimeException("未知配置");
    }


}
