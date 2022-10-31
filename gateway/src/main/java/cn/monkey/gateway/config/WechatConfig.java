package cn.monkey.gateway.config;

import cn.monkey.gateway.handler.http.SimpleWechatRequestHandler;
import cn.monkey.gateway.handler.http.WechatRequestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WechatConfig {
    @Bean
    WechatProperties wechatProperties() {
        return new WechatProperties();
    }

    @Bean
    WechatRequestHandler wechatRequestHandler(WechatProperties wechatProperties) {
        return new SimpleWechatRequestHandler(wechatProperties);
    }
}
