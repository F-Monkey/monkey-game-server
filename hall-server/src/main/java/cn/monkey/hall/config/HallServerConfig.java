package cn.monkey.hall.config;

import cn.monkey.commons.data.repository.RedisUserSessionRepository;
import cn.monkey.commons.data.repository.ServerRepository;
import cn.monkey.commons.data.repository.UserSessionRepository;
import cn.monkey.commons.utils.Timer;
import cn.monkey.server.netty.session.NettSessionManager;
import cn.monkey.server.netty.session.NettySessionFactory;
import cn.monkey.server.netty.session.SimpleNettySessionFactory;
import cn.monkey.server.netty.session.SimpleNettySessionManager;
import cn.monkey.server.supported.user.SimpleUserManager;
import cn.monkey.server.supported.user.UserManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class HallServerConfig {

    @Bean
    UserSessionRepository userSessionRepository(RedisTemplate<String, String> redisTemplate) {
        return new RedisUserSessionRepository(redisTemplate);
    }

    @Bean
    UserManager userManager(Timer timer,
                            UserSessionRepository userSessionRepository,
                            ServerRepository serverRepository) {
        return new SimpleUserManager(timer, userSessionRepository, serverRepository);
    }

    @Bean
    NettySessionFactory nettySessionFactory() {
        return new SimpleNettySessionFactory();
    }

    @Bean
    NettSessionManager nettSessionManager(NettySessionFactory nettySessionFactory) {
        return new SimpleNettySessionManager(nettySessionFactory);
    }
}
