package cn.monkey.hall.state;

import cn.monkey.commons.utils.Timer;
import cn.monkey.commons.data.repository.ServerRepository;
import cn.monkey.proto.Command;
import cn.monkey.server.supported.user.User;
import cn.monkey.server.supported.user.UserManager;
import cn.monkey.state.core.SimpleStateGroup;
import cn.monkey.state.core.SimpleStateGroupFactory;
import cn.monkey.state.core.StateGroup;
import reactor.util.function.Tuples;

public class HallServerStateGroupFactory extends SimpleStateGroupFactory {

    private final ServerRepository roomServerRepository;

    private final UserManager userManager;

    public HallServerStateGroupFactory(Timer timer,
                                       ServerRepository roomServerRepository,
                                       UserManager userManager) {
        super(timer);
        this.roomServerRepository = roomServerRepository;
        this.userManager = userManager;
    }

    @Override
    public StateGroup create(String id, Object... args) {
        Command.Package pkg = (Command.Package) args[0];
        int cmdType = pkg.getCmdType();
        if (cmdType == HallCmdType.CHOOSE_GAME_SERVER) {
            return this.createWaitingGroup(id);
        }
        if (cmdType == HallCmdType.ENTER_CHAT_ROOM) {
            return this.createChatGroup(id, (User) args[1]);
        }
        throw new IllegalArgumentException("invalid options");
    }

    private static Command.Package buildEnterEvent() {
        Command.Package.Builder builder = Command.Package.newBuilder();
        builder.setCmdType(HallCmdType.ENTER_CHAT_ROOM);
        return builder.build();
    }

    /**
     * @param id
     * @param user invited user
     * @return
     */
    private StateGroup createChatGroup(String id, User user) {
        ChatServerContext chatServerContext = new ChatServerContext(this.userManager);
        SimpleStateGroup stateGroup = new SimpleStateGroup(id, chatServerContext, this.timer, true);
        ChatState chatState = new ChatState(super.timer, stateGroup);
        stateGroup.addState(chatState);
        stateGroup.setStartState(ChatState.CODE);
        stateGroup.addEvent(Tuples.of(user, buildEnterEvent()));
        return stateGroup;
    }

    protected StateGroup createWaitingGroup(String id) {
        HallServerContext hallServerContext = new HallServerContext(this.roomServerRepository);
        SimpleStateGroup stateGroup = new SimpleStateGroup(id, hallServerContext, this.timer, true);
        ServerState serverState = new ServerState(super.timer, stateGroup);
        stateGroup.addState(serverState);
        stateGroup.setStartState(ServerState.CODE);
        return stateGroup;
    }
}
