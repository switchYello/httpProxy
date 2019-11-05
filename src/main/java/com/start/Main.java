package com.start;

import com.httpservice.ProxyServiceInit;
import com.brige.BrigeInit;
import com.utils.PropertiesUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;

public class Main {

    private static Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException, SSLException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workGroup = new NioEventLoopGroup(1);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 6000);
            switch (PropertiesUtil.getStrProp("brige")) {
                case "http":
                    log.info("添加http handler");
                    b.childHandler(new ProxyServiceInit());
                    break;
                case "brige":
                    log.info("添加brige handler");
                    b.childHandler(new BrigeInit());
                    break;
            }
            ChannelFuture f = b.bind(PropertiesUtil.getIntProp("start.port")).sync();
            log.info("start at " + PropertiesUtil.getIntProp("start.port") + "启动模式为:" + PropertiesUtil.getStrProp("brige"));
            f.channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }


    }


}
