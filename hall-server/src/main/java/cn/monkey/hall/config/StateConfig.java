package cn.monkey.hall.config;

import cn.monkey.commons.data.repository.RedisServerRepository;
import cn.monkey.commons.data.repository.ServerRepository;
import cn.monkey.commons.utils.Timer;
import cn.monkey.hall.state.HallServerStateGroupFactory;
import cn.monkey.server.supported.user.UserManager;
import cn.monkey.state.core.SimpleStateGroupPool;
import cn.monkey.state.core.StateGroupFactory;
import cn.monkey.state.core.StateGroupPool;
import cn.monkey.state.scheduler.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class StateConfig {

    @Bean
    Timer timer() {
        return new Timer() {
        };
    }

    @Bean
    ServerRepository serverRepository(RedisTemplate<String, String> redisTemplate) {
        return new RedisServerRepository(redisTemplate);
    }

    @Bean
    StateGroupFactory stateGroupFactory(Timer timer,
                                        ServerRepository serverRepository,
                                        UserManager userManager) {
        return new HallServerStateGroupFactory(timer, serverRepository, userManager);
    }

    @Bean
    StateGroupPool stateGroupPool(StateGroupFactory stateGroupFactory) {
        return new SimpleStateGroupPool(stateGroupFactory);
    }

    @Bean
    StateGroupSchedulerFactoryConfig stateGroupFactoryConfig() {
        return StateGroupSchedulerFactoryConfig.newBuilder().build();
    }

    @Bean
    StateGroupSchedulerFactory stateGroupSchedulerFactory(StateGroupSchedulerFactoryConfig stateGroupFactoryConfig) {
        return new SimpleStateGroupSchedulerFactory(stateGroupFactoryConfig);
    }

    @Bean
    EventPublishSchedulerFactory eventPublishSchedulerFactory() {
        return new SimpleEventPublishSchedulerFactory();
    }

    @Bean
    SchedulerManagerConfig schedulerManagerConfig() {
        return SchedulerManagerConfig.newBuilder().build();
    }

    @Bean
    SchedulerManager schedulerManager(StateGroupPool stateGroupPool,
                                      StateGroupSchedulerFactory stateGroupSchedulerFactory,
                                      EventPublishSchedulerFactory eventPublishSchedulerFactory,
                                      SchedulerManagerConfig managerConfig) {
        return new SimpleSchedulerManager(stateGroupPool, stateGroupSchedulerFactory, eventPublishSchedulerFactory, managerConfig);
    }
}
