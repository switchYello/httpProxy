package com.brige;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ClientSslHandlerInbond extends ReplayingDecoder {


	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		in.skipBytes(3);//忽略两个字节
		short length = in.readShort();//获取数据长度
		ByteBuf byteBuf = in.readRetainedSlice(length);//读取切片
		byteBuf.skipBytes(6);
		out.add(byteBuf);
	}


}
