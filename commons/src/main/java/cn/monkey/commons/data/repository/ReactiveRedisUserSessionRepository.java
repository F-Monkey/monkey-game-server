package cn.monkey.commons.data.repository;

import cn.monkey.commons.data.pojo.UserSession;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;

public class ReactiveRedisUserSessionRepository implements ReactiveUserSessionRepository {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    private final Gson gson;

    public ReactiveRedisUserSessionRepository(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.gson = new Gson();
    }

    @Override
    public Mono<UserSession> findById(String id) {
        return this.redisTemplate.opsForValue().get(id)
                .flatMap(s -> {
                    if (Strings.isNullOrEmpty(s)) {
                        return Mono.empty();
                    }
                    return Mono.just(this.gson.fromJson(s, UserSession.class));
                });
    }

    @Override
    public Mono<Boolean> save(String uid, UserSession session) {
        return this.redisTemplate.opsForValue().set(uid, this.gson.toJson(session));
    }
}
