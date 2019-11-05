package com.brige;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class ClientSslHandlerOutbond extends ChannelOutboundHandlerAdapter {


	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		ByteBuf b = (ByteBuf) msg;
		int i = b.readableBytes();

		ByteBuf buffer = Unpooled.buffer(11 + i);//创建一个新的buffer



		buffer.writeByte(0x16);//固定表示是首次的hello

		buffer.writeByte(0x03);//tls1.2
		buffer.writeByte(0x03);

		buffer.writeShort(6 + i);//两个字节的长度
		buffer.writeByte(0x01);//client hello

		buffer.writeMedium(2 + i);//数据长度

		buffer.writeByte(0x03); //tls 1.2
		buffer.writeByte(0x04);

		ByteBuf wrap = Unpooled.wrappedBuffer(buffer, b.readSlice(i));
		super.write(ctx, wrap, promise);

	}


}
