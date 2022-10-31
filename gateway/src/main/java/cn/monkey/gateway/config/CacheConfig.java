package cn.monkey.gateway.config;

import cn.monkey.gateway.cache.InMemoryWechatCache;
import cn.monkey.gateway.cache.WechatCache;
import cn.monkey.gateway.handler.http.WechatRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean
    WechatCache wechatCache(WechatRequestHandler wechatRequestHandler) {
        return new InMemoryWechatCache(wechatRequestHandler);
    }
}
