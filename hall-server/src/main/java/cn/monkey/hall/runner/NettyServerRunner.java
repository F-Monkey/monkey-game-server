package cn.monkey.hall.runner;

import cn.monkey.server.SessionManager;
import cn.monkey.server.netty.protobuf.ProtobufDispatcher;
import cn.monkey.server.netty.protobuf.ProtobufFilter;
import cn.monkey.server.netty.session.NettSessionManager;
import cn.monkey.server.netty.session.SimpleNettySessionManager;
import cn.monkey.server.netty.tcp.NettyWebSocketServer;
import cn.monkey.server.netty.tcp.ProtoBufWebSocketChannelHandler;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class NettyServerRunner implements ApplicationContextAware, ApplicationRunner {

    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.startServer();
    }

    private void startServer() {
        Map<String, ProtobufFilter> filterMap = this.applicationContext.getBeansOfType(ProtobufFilter.class);
        List<ProtobufFilter> filters = null;
        if (CollectionUtils.isEmpty(filterMap)) {
            filters = new ArrayList<>(filterMap.values());
        }
        ProtobufDispatcher dispatcher = this.applicationContext.getBean(ProtobufDispatcher.class);
        NettSessionManager sessionManager = this.applicationContext.getBean(NettSessionManager.class);
        ProtoBufWebSocketChannelHandler protoBufWebSocketChannelHandler = new ProtoBufWebSocketChannelHandler(sessionManager, filters, dispatcher);
        final int port = 8082;
        NettyWebSocketServer nettyWebSocketServer = new NettyWebSocketServer(protoBufWebSocketChannelHandler, "hall", port, 2, 4);
        nettyWebSocketServer.start();

    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
