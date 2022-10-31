package cn.monkey.server.netty.session;

import cn.monkey.server.SessionFactory;
import io.netty.channel.ChannelHandlerContext;

public interface NettySessionFactory extends SessionFactory<ChannelHandlerContext> {
}
