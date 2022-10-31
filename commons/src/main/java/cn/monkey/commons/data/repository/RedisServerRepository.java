package cn.monkey.commons.data.repository;

import cn.monkey.commons.data.pojo.ServerConfig;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;

public class RedisServerRepository implements ServerRepository {

    protected final RedisTemplate<String, String> redisTemplate;

    public RedisServerRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ServerConfig> getServerConfig(String name) {
        return Collections.emptyList();
    }

    @Override
    public void increaseCurrentUserCount(String id) {
        this.redisTemplate.opsForValue().increment(id);
    }

    @Override
    public void setCurrentUserCount(String serverId, long count) {
        this.redisTemplate.opsForValue().set(serverId, String.valueOf(count));
    }
}
