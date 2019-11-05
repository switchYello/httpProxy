package com.httpservice;

import com.handlers.ExceptionHandler;
import com.start.HttpService;
import com.handlers.LoginHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

/*
 * 作为普通http(s)代理服务器时初始化
 * */
public class ProxyServiceInit extends ChannelInitializer<Channel> {

    private HttpService httpService = new HttpService(new PromiseProvideForProxy());

    @Override
    protected void initChannel(Channel channel) {
        ChannelPipeline p = channel.pipeline();
        //连接超时
        p.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        //http解码，解码出ip 端口，或者处理https
        p.addLast("httpcode", new HttpServerCodec());
        //聚合http请求
        p.addLast("objectAggregator", new HttpObjectAggregator(1024 * 1024));
        //登录检测
        p.addLast(LoginHandler.INSTANCE);
        //真实处理类
        p.addLast("httpservice", httpService);
        //处理超时事件
        p.addLast("heartbeat", ExceptionHandler.INSTANSE);
    }
}
