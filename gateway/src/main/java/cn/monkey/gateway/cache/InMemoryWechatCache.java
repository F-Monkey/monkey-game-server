package cn.monkey.gateway.cache;

import cn.monkey.gateway.handler.http.WechatRequestHandler;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class InMemoryWechatCache implements WechatCache {

    private final LoadingCache<String, Mono<String>> tokenCache;

    public InMemoryWechatCache(WechatRequestHandler wechatRequestHandler) {
        this.tokenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.of(1, ChronoUnit.MINUTES))
                .build(new CacheLoader<String, Mono<String>>() {
                    @Override
                    @NonNull
                    public Mono<String> load(@NonNull String s) {
                        return wechatRequestHandler.getOpenId(s);
                    }
                });
    }

    @Override
    public Mono<String> getOpenId(String code) {
        return this.tokenCache.getUnchecked(code);
    }
}
