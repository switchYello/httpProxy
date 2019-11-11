package com.handlers;

import com.utils.Rc4;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Rc4Handler extends ChannelDuplexHandler{

    private Rc4 rc4 = new Rc4("123");

    public Rc4Handler() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        ctx.close(promise);
        rc4.finshDecoder();
        rc4.finshEncoder();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf bb = (ByteBuf) msg;
        byte[] decoder = rc4.encoder(ByteBufUtil.getBytes(bb));
        ctx.write(Unpooled.wrappedBuffer(decoder).retain(), promise);
    }

    /**
     * todo   看下 @code HttpProxyHandler 类
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf bb = (ByteBuf) msg;
        byte[] decoder = rc4.decoder(ByteBufUtil.getBytes(bb));
        ctx.fireChannelRead(ctx.alloc().heapBuffer(decoder.length).writeBytes(decoder));
    }
}
