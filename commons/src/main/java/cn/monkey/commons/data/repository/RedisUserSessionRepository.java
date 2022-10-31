package cn.monkey.commons.data.repository;

import cn.monkey.commons.data.pojo.UserSession;
import com.google.gson.Gson;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisUserSessionRepository implements UserSessionRepository {

    protected final RedisTemplate<String, String> redisTemplate;

    protected final Gson gson;

    public RedisUserSessionRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.gson = new Gson();
    }

    @Override

    public UserSession findByToken(String uid) {
        String s = this.redisTemplate.opsForValue().get(uid);
        if (s == null) {
            return null;
        }
        return this.gson.fromJson(s, UserSession.class);
    }

    @Override
    public void save(UserSession userSession) {
        this.redisTemplate.opsForValue().set(userSession.getUid(), this.gson.toJson(userSession));
    }
}
