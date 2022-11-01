package cn.monkey.hall.server;

import cn.monkey.hall.state.HallCmdType;
import cn.monkey.hall.state.HallCmdUtil;
import cn.monkey.proto.Chat;
import cn.monkey.proto.Command;
import cn.monkey.server.Session;
import cn.monkey.server.netty.protobuf.ProtobufDispatcher;
import cn.monkey.server.supported.user.User;
import cn.monkey.server.supported.user.UserManager;
import cn.monkey.state.scheduler.SchedulerManager;
import com.google.common.base.Strings;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuples;

@Component
public class HallDispatcher implements ProtobufDispatcher {

    private static final Logger log = LoggerFactory.getLogger(HallDispatcher.class);

    private final String USER_KEY = "user";

    private final UserManager userManager;

    private final SchedulerManager schedulerManager;

    private final Scheduler chooseGameScheduler;

    private final Scheduler loginScheduler;

    public HallDispatcher(UserManager userManager,
                          SchedulerManager schedulerManager) {
        this.userManager = userManager;

        this.schedulerManager = schedulerManager;
        this.loginScheduler = Schedulers.newParallel("login", 3);
        this.chooseGameScheduler = Schedulers.newParallel("choose_game", 2);
    }

    @Override
    public void dispatch(Session session, Command.Package pkg) {
        int cmdType = pkg.getCmdType();
        switch (cmdType) {
            case HallCmdType.LOGIN:
                this.login(session, pkg);
                return;
            case HallCmdType.CHOOSE_GAME_SERVER:
                this.chooseGameServer(session, pkg);
                return;
            case HallCmdType.ENTER_CHAT_ROOM:
                this.findInvitedUser2Enter(session, pkg);
        }
    }

    private void findInvitedUser2Enter(Session session, Command.Package pkg) {
        Mono.just(pkg)
                .flatMap(p -> {
                    String groupId = p.getGroupId();
                    if (Strings.isNullOrEmpty(groupId)) {
                        return Mono.error(new IllegalArgumentException("groupId is required"));
                    }
                    ByteString content = p.getContent();
                    Chat.ChatEnter chatEnter;
                    try {
                        chatEnter = Chat.ChatEnter.parseFrom(content);
                    } catch (InvalidProtocolBufferException e) {
                        return Mono.error(e);
                    }
                    Chat.Invite invite = chatEnter.getInvite();
                    String invitedUid = invite.getInvitedUid();
                    User user = this.userManager.find(invitedUid);
                    if (user == null) {
                        return Mono.error(new IllegalArgumentException("uid: " + invitedUid + " is not exists"));
                    }
                    return Mono.just(Tuples.of(groupId, user));
                })
                .map(t -> {
                    User user = session.getAttribute(USER_KEY);
                    if (user == null) {
                        throw new UnsupportedOperationException("user has not login yet");
                    }
                    return Tuples.of(t.getT1(), user, t.getT2());
                })
                .doOnNext(t -> this.schedulerManager.addEvent(t.getT1(), t.getT2(), t.getT3()))
                .doOnError(e -> {
                    log.error("enter chat room error");
                    session.write(HallCmdUtil.error(e));
                })
                .subscribeOn(this.chooseGameScheduler)
                .subscribe();
    }

    private void chooseGameServer(Session session, Command.Package pkg) {
        Mono.just(pkg)
                .map(p -> {
                    String groupId = p.getGroupId();
                    if (Strings.isNullOrEmpty(groupId)) {
                        throw new IllegalArgumentException("groupId is required");
                    }
                    return groupId;
                })
                .map(s -> {
                    User user = session.getAttribute(USER_KEY);
                    if (user == null) {
                        throw new UnsupportedOperationException("user has not login yet");
                    }
                    return Tuples.of(s, user);
                })
                .doOnNext(t -> this.schedulerManager.addEvent(t.getT1(), Tuples.of(t.getT2(), pkg)))
                .doOnError(e -> session.write(HallCmdUtil.error(e)))
                .subscribeOn(this.chooseGameScheduler)
                .subscribe();
    }

    private void login(Session session, Command.Package pkg) {
        Mono.just(pkg)
                .flatMap(p -> {
                    try {
                        cn.monkey.proto.User.Session s = cn.monkey.proto.User.Session.parseFrom(p.getContent());
                        return Mono.just(this.userManager.findOrCreate(session, s.getToken()));
                    } catch (InvalidProtocolBufferException e) {
                        return Mono.error(e);
                    }
                })
                .doOnNext(user -> session.setAttribute(USER_KEY, user))
                .doOnError(e -> session.write(HallCmdUtil.error(e)))
                .subscribeOn(this.loginScheduler)
                .subscribe();
    }
}
