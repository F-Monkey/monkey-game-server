package cn.monkey.commons.data.repository;

import cn.monkey.commons.data.pojo.UserSession;
import reactor.core.publisher.Mono;

public interface ReactiveUserSessionRepository {
    Mono<UserSession> findById(String id);

    Mono<Boolean> save(String uid, UserSession session);
}
