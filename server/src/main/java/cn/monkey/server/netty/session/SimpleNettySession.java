package cn.monkey.server.netty.session;

import cn.monkey.server.Session;
import cn.monkey.server.SessionUtil;
import com.google.protobuf.MessageLite;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SimpleNettySession implements Session {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    protected final ChannelHandlerContext ctx;
    private final ConcurrentMap<String, AttributeKey<Object>> keyMap;

    public SimpleNettySession(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.keyMap = new ConcurrentHashMap<>();
    }

    @Override
    public String id() {
        return SessionUtil.getId(this.ctx);
    }

    @Override
    public Object setAttribute(String key, Object val) {
        AttributeKey<Object> attrKey = this.keyMap.compute(key, (k, v) -> {
            if (v == null) {
                v = AttributeKey.newInstance(k);
            }
            return v;
        });
        return this.ctx.channel().attr(attrKey).getAndSet(val);
    }

    @Override
    public Object getAttribute(String key) {
        AttributeKey<Object> attributeKey = this.keyMap.get(key);
        if (attributeKey == null) {
            throw new IllegalArgumentException("key:" + key + " is not exists");
        }
        return this.ctx.channel().attr(attributeKey).get();
    }

    @Override
    public void write(Object data) {
        if (null == data) {
            return;
        }
        if (data instanceof BinaryWebSocketFrame) {
            this.ctx.writeAndFlush(data);
            return;
        }
        if (data instanceof MessageLite) {
            byte[] bytes = ((MessageLite) data).toByteArray();
            BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(bytes));
            this.ctx.writeAndFlush(binaryWebSocketFrame);
            return;
        }
        log.error("invalid data type: {}", data.getClass());
    }

    @Override
    public boolean isActive() {
        return this.ctx.channel().isActive();
    }

    @Override
    public void close() throws IOException {
        this.ctx.channel().close();
    }
}
