package cn.monkey.gateway.cache;

import reactor.core.publisher.Mono;

public interface WechatCache {
    Mono<String> getOpenId(String code);
}
