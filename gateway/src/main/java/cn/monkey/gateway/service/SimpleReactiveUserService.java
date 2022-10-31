package cn.monkey.gateway.service;


import cn.monkey.commons.data.ServerType;
import cn.monkey.commons.data.pojo.ServerConfig;
import cn.monkey.commons.data.pojo.UserSession;
import cn.monkey.commons.data.pojo.vo.ResultCode;
import cn.monkey.commons.data.repository.ReactiveServerRepository;
import cn.monkey.commons.data.repository.ReactiveUserSessionRepository;
import cn.monkey.gateway.cache.WechatCache;
import cn.monkey.gateway.repository.UserRepository;
import cn.monkey.gateway.util.CommandUtil;
import cn.monkey.proto.Command;
import cn.monkey.proto.User;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.function.Predicate;

@Service
public class SimpleReactiveUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(SimpleReactiveUserService.class);

    private final UserRepository userRepository;

    private final ReactiveUserSessionRepository userSessionRepository;

    private final ReactiveServerRepository serverRepository;

    private final WechatCache wechatCache;

    public SimpleReactiveUserService(UserRepository userRepository,
                                     WechatCache wechatCache,
                                     ReactiveUserSessionRepository userSessionRepository,
                                     ReactiveServerRepository serverRepository) {
        this.wechatCache = wechatCache;
        this.userRepository = userRepository;
        this.userSessionRepository = userSessionRepository;
        this.serverRepository = serverRepository;
    }

    @Override
    public Mono<Command.PackageGroup> login(Mono<Command.Package> pkg) {
        return pkg.flatMap(p -> {
            try {
                return Mono.just(User.Login.parseFrom(p.getContent()));
            } catch (InvalidProtocolBufferException e) {
                return Mono.error(e);
            }
        }).flatMap(login -> {
            String uid = login.getUid();
            if (!Strings.isNullOrEmpty(uid)) {
                return this.loginByUid(uid);
            } else {
                return this.loginByUsernameAndPassword(login.getUsername(), login.getPassword());
            }
        });
    }

    @Override
    public Mono<Command.PackageGroup> register(Mono<Command.Package> request) {
        return request.flatMap(p -> {
            try {
                User.Register register = User.Register.parseFrom(p.getContent());
                return this.register(register.getUsername(), register.getPassword());
            } catch (InvalidProtocolBufferException e) {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Mono<Command.PackageGroup> wxSignIn(Mono<Command.Package> requestEntity) {
        return requestEntity.flatMap(pkg -> {
            try {
                return Mono.just(User.Login.parseFrom(pkg.getContent()));
            } catch (InvalidProtocolBufferException e) {
                return Mono.error(e);
            }
        })
                .flatMap(login -> {
                    String appCode = login.getAppCode();
                    if (Strings.isNullOrEmpty(appCode)) {
                        return Mono.error(new IllegalArgumentException("empty appCode"));
                    }
                    return this.findOrCreateWxChatUser(login);
                })
                .doOnError(e -> log.error("#wxSignIn error:\n", e))
                .onErrorResume(e -> Mono.just(cn.monkey.proto.CommandUtil.error(ResultCode.ERROR, e)));
    }

    private Mono<Command.PackageGroup> findOrCreateWxChatUser(User.Login login) {
        String appCode = login.getAppCode();
        return this.wechatCache.getOpenId(appCode)
                .flatMap(openId -> {
                    cn.monkey.gateway.data.User user = new cn.monkey.gateway.data.User();
                    user.setOpenId(openId);
                    return this.userRepository.save(user);
                })
                .flatMap(this::findBestHallServer)
                .doOnNext(userSession -> this.userSessionRepository.save(userSession.getUid(), userSession))
                .map(userSession -> {
                    User.Session copy = copy(userSession);
                    return cn.monkey.proto.CommandUtil.packageGroup(cn.monkey.proto.CommandUtil.pkg(ResultCode.OK, null, null, copy.toByteString()));
                });
    }

    static User.Session copy(UserSession userSession) {
        User.Session.Builder builder = User.Session.newBuilder();
        builder.setHallServer(userSession.getHallServerUrl());
        User.UserInfo.Builder userInfoBuilder = User.UserInfo.newBuilder();
        userInfoBuilder.setUid(userSession.getUid());
        builder.setUser(userInfoBuilder.build());
        builder.setToken(userSession.getUid());
        return builder.build();
    }

    static User.UserInfo copy(cn.monkey.gateway.data.User user) {
        String id = user.getId();
        String username = user.getUsername();
        User.UserInfo.Builder builder = User.UserInfo.newBuilder();
        if (!Strings.isNullOrEmpty(username)) {
            builder.setUsername(username);
        }
        builder.setUid(id);
        return builder.build();
    }

    private Mono<Command.PackageGroup> register(String username, String password) {
        return this.userRepository.findByUsername(username)
                .flatMap(user -> {
                    if (user != null) {
                        return Mono.error(new IllegalArgumentException("username: " + username + " is already exists"));
                    }
                    user = new cn.monkey.gateway.data.User();
                    user.setUsername(username);
                    user.setPassword(password);
                    return this.userRepository.save(user);
                })
                .map(user -> {
                    User.UserInfo copy = copy(user);
                    return cn.monkey.proto.CommandUtil.packageGroup(cn.monkey.proto.CommandUtil.pkg(ResultCode.OK, null, null, copy.toByteString()));
                })
                .onErrorResume(e -> Mono.just(cn.monkey.proto.CommandUtil.error(ResultCode.ERROR, e)));
    }

    private Mono<? extends Command.PackageGroup> loginByUsernameAndPassword(String username, String password) {
        return this.userRepository.findByUsername(username)
                .flatMap(user -> {
                    String password1 = user.getPassword();
                    if (!this.checkPassword(password, password1)) {
                        return Mono.empty();
                    }
                    return Mono.just(user);
                })
                .flatMap(user -> findBestHallServer(user).map(us -> Tuples.of(user, us)))
                .map(t -> CommandUtil.login(t.getT1(), t.getT2()))
                .switchIfEmpty(Mono.just(CommandUtil.loginFail("bad username or password")))
                .onErrorResume(e -> Mono.just(cn.monkey.proto.CommandUtil.error(ResultCode.ERROR, e)));
    }

    private Mono<? extends Command.PackageGroup> loginByUid(String uid) {
        return this.userRepository.findById(uid)
                .flatMap(user -> this.userSessionRepository.findById(uid)
                        .map(userSession -> Tuples.of(user, userSession)))
                .map(t -> CommandUtil.login(t.getT1(), t.getT2()))
                .switchIfEmpty(Mono.just(CommandUtil.loginFail("bad uid: " + uid)))
                .onErrorResume(e -> Mono.just(cn.monkey.proto.CommandUtil.error(ResultCode.ERROR, e)));
    }

    private Mono<UserSession> findBestHallServer(cn.monkey.gateway.data.User user) {
        Mono<List<ServerConfig>> serverConfigs = this.serverRepository.getServerConfig(ServerType.HALL);
        Predicate<ServerConfig> p = serverConfig -> serverConfig.getCurrentUserSize() < serverConfig.getMaxUserSize();

        return serverConfigs.flatMap(list -> {
            for (ServerConfig sc : list) {
                if (p.test(sc)) {
                    UserSession userSession = new UserSession();
                    userSession.setUid(user.getId());
                    userSession.setHallServerId(sc.getId());
                    userSession.setHallServerUrl(sc.getUrl());
                    return Mono.just(userSession);
                }
            }
            return Mono.error(new IllegalArgumentException("empty server"));
        });
    }

    private boolean checkPassword(String password, String password1) {
        return true;
    }
}
