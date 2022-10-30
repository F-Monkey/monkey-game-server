package cn.monkey.gateway.config;

import cn.monkey.commons.bean.BeanContext;
import cn.monkey.commons.data.ServerType;
import cn.monkey.commons.data.pojo.ServerInfo;
import cn.monkey.commons.data.repository.ReactiveRedisServerRepository;
import cn.monkey.commons.data.repository.ReactiveRedisUserSessionRepository;
import cn.monkey.commons.data.repository.ReactiveUserSessionRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class GatewayConfig {
    @Bean
    ReactiveUserSessionRepository reactiveUserSessionRepository(ReactiveRedisTemplate<String, String> redisTemplate) {
        return new ReactiveRedisUserSessionRepository(redisTemplate);
    }

    @Bean
    @ConfigurationProperties(prefix = "monkey.server-infos")
    List<ServerInfo> serverInfoList() {
        return new ArrayList<>();
    }

    @Bean
    ReactiveRedisServerRepository reactiveRedisServerRepository(ReactiveRedisTemplate<String, String> redisTemplate,
                                                                List<ServerInfo> serverInfoList) {
        Map<String, ServerInfo> serverInfoMap = serverInfoList.parallelStream().collect(Collectors.toMap(ServerInfo::getCode, serverInfo -> serverInfo, (s1, s2) -> s1));
        BeanContext<ServerInfo> beanContext = new BeanContext<ServerInfo>() {
            @Override
            public ServerInfo getBean(String name) {
                return serverInfoMap.get(name);
            }

            @Override
            public Collection<ServerInfo> getBeans() {
                return serverInfoList;
            }

            @Override
            public boolean containsBean(String name) {
                return serverInfoMap.containsKey(name);
            }
        };
        return new ReactiveRedisServerRepository(redisTemplate, beanContext);
    }
}
