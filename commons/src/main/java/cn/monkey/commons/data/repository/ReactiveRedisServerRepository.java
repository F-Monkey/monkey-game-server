package cn.monkey.commons.data.repository;

import cn.monkey.commons.bean.BeanContext;
import cn.monkey.commons.data.pojo.ServerConfig;
import cn.monkey.commons.data.pojo.ServerInfo;
import com.google.common.collect.Lists;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import reactor.core.publisher.Mono;

import java.util.List;

public class ReactiveRedisServerRepository implements ReactiveServerRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final BeanContext<ServerInfo> serverTypesContext;

    private final RedisScript<String> script;

    public ReactiveRedisServerRepository(ReactiveRedisTemplate<String, String> redisTemplate,
                                         BeanContext<ServerInfo> serverTypesContext) {
        this.redisTemplate = redisTemplate;
        this.serverTypesContext = serverTypesContext;
        String lua = "local result = {}; " +
                "for i =1, #(KEYS) do " +
                "result[i] = redis.call('get', KEYS[i]); " +
                "end " +
                "return result;";
        this.script = new DefaultRedisScript<>(lua, String.class);
    }

    static ServerConfig createTestServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setUrl("ws://192.168.88.145:8111/hall");
        serverConfig.setId("hall_1");
        serverConfig.setCurrentUserSize(1);
        serverConfig.setMaxUserSize(100);
        return serverConfig;
    }

    @Override
    public Mono<List<ServerConfig>> getServerConfig(String type) {
        ServerInfo bean = this.serverTypesContext.getBean(type);
        List<String> names = bean.getNames();
        return Mono.just(Lists.newArrayList(createTestServerConfig()));
        /*
        return this.redisTemplate.execute(this.script, names)
                .map(s -> createTestServerConfig())
                .collectList();
         */
    }
}
