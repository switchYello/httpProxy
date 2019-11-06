package com.brige;

import com.start.Context;
import com.start.Environment;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.concurrent.Promise;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

//服务器端返回0，表示连接成功，如果返回的不是0则断开连接
public class EnSuccessHandler extends ReplayingDecoder<EnSuccessHandler.LoginStatus> {

    private static Logger log = LoggerFactory.getLogger(EnSuccessHandler.class);
    //连接占位符
    private Promise<Channel> promise;
    //目标地址
    private InetSocketAddress address;
    //目标host = address.host
    private String host;
    //目标port = address.port
    private int port;
    //环境
    private Environment environment = Context.getEnvironment();
    //初次提交数据的开始时间
    private long startTime;
    //解析次数
    private int encoderCount = 0;

    //登录状态
    enum LoginStatus {
        //未登录
        requestLink,
        //已发送登录指令,等待回复
        sendLogin;
    }

    //发送报文类型
    enum requestType {
        //只连接
        link((byte) 1),
        //连接并认证
        linkAndLogin((byte) 2);
        byte code;

        requestType(byte code) {
            this.code = code;
        }
    }


    public EnSuccessHandler(Promise<Channel> promise, InetSocketAddress address) {
        this.promise = promise;
        this.address = address;
        this.host = address.getHostName();
        this.port = address.getPort();
    }

    //请求类型byte + (host+port)的长度short + 数据
    //
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        startTime = System.currentTimeMillis();
        ByteBuf buffer = Unpooled.buffer(1 + 2 + ByteBufUtil.utf8MaxBytes(host) + 2);
        buffer.writeByte(requestType.link.code);
        buffer.writeShort(host.length() + 2);
        buffer.writeCharSequence(host, StandardCharsets.UTF_8);
        buffer.writeShort(port);
        ctx.writeAndFlush(buffer).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        state(LoginStatus.requestLink);
        super.channelActive(ctx);
    }


    /**
     * 1.登录成功，且连接成功
     * 2.需要验证
     * 其他情况.拒绝连接或连接不成功
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        encoderCount++;
        byte i = msg.readByte();
        if (i == 1) {
            ctx.pipeline().remove(this);
            promise.setSuccess(ctx.channel());
            log.debug("服务器连接成功,目标地址:{},解码次数:{}，耗时:{}", address, encoderCount, System.currentTimeMillis() - startTime);
            return;
        }
        switch (state()) {
            case requestLink: {
                if (i == 2) {
                    log.debug("服务器返回2，需要验证");
                    //请求类型byte + 时间戳 + token长度short + token + （host port）长度short + host + port
                    long timeMillis = System.currentTimeMillis();
                    ByteBuf b = Unpooled.buffer();
                    b.writeByte(requestType.linkAndLogin.code);
                    b.writeLong(timeMillis);
                    String token = createToken(timeMillis);
                    b.writeShort(token.length());
                    b.writeCharSequence(token, StandardCharsets.UTF_8);
                    b.writeShort(host.length() + 2);
                    b.writeCharSequence(host, StandardCharsets.UTF_8);
                    b.writeShort(port);
                    ctx.writeAndFlush(b);
                    state(LoginStatus.sendLogin);
                } else {
                    throw new RuntimeException("requestLink请求认证阶段，未知返回值:" + i);
                }
                break;
            }
            case sendLogin: {
                throw new RuntimeException("sendLogin等待认证通过阶段，未知返回值:" + i);
            }
            default:
                break;
        }
    }

    //长度时间戳md5(时间戳 + 密码)
    private String createToken(long timeMillis) {
        String salt = environment.getRemoteSalt();
        return DigestUtils.md5Hex(timeMillis + salt);
    }

}
