package cn.monkey.hall.state;

import cn.monkey.commons.data.pojo.vo.ResultCode;
import cn.monkey.proto.Chat;
import cn.monkey.proto.Command;
import cn.monkey.proto.CommandUtil;
import cn.monkey.proto.Game;
import cn.monkey.server.supported.user.User;
import com.google.common.base.Strings;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class HallCmdUtil {
    public static Command.PackageGroup error(Throwable error) {
        return CommandUtil.error(ResultCode.ERROR, error);
    }

    public static Command.PackageGroup chooseRoomResult(int wait) {
        Game.ChooseRoomServerResult.Builder builder = Game.ChooseRoomServerResult.newBuilder();
        builder.setWaitCount(wait);
        return CommandUtil.packageGroup(CommandUtil.pkg(ResultCode.OK, null, HallCmdType.CHOOSE_GAME_SERVER_RESULT, builder.build().toByteString()));
    }

    public static Command.PackageGroup loginResult(int resultCode) {
        return CommandUtil.packageGroup(CommandUtil.pkg(resultCode, null, HallCmdType.LOGIN_RESULT, null));
    }

    static Chat.Friend copyFriend(User user) {
        Chat.Friend.Builder builder = Chat.Friend.newBuilder();
        builder.setUid(user.getUid());
        String username = user.getUsername();
        if (!Strings.isNullOrEmpty(username)) {
            builder.setUsername(username);
        }
        return builder.build();
    }

    static Chat.Friends copyFriends(Collection<User> users) {
        List<Chat.Friend> friendList = new ArrayList<>(users.size());
        for (User user : users) {
            friendList.add(copyFriend(user));
        }
        Chat.Friends.Builder builder = Chat.Friends.newBuilder();
        builder.addAllFriends(friendList);
        return builder.build();
    }

    public static Command.PackageGroup getUserList(int resultCode, String msg, Collection<User> users) {
        return CommandUtil.packageGroup(CommandUtil.pkg(resultCode, msg, HallCmdType.SHOW_USER_LIST_RESULT, CollectionUtils.isEmpty(users) ? null : copyFriends(users).toByteString()));
    }

    public static Command.PackageGroup chooseRoomResult(String roomServerUrl) {
        Game.ChooseRoomServerResult.Builder builder = Game.ChooseRoomServerResult.newBuilder();
        builder.setRoomServerUrl(roomServerUrl);
        return CommandUtil.packageGroup(CommandUtil.pkg(ResultCode.OK, null, HallCmdType.CHOOSE_GAME_SERVER_RESULT, builder.build().toByteString()));
    }
}
