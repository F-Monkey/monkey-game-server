package cn.monkey.hall.state;

public interface HallCmdType {
    int LOGIN = 1000;
    int LOGIN_RESULT = 1001;

    int CHOOSE_GAME_SERVER = 1002;
    int CHOOSE_GAME_SERVER_RESULT = 1003;

    int ENTER_CHAT_ROOM = 1004;
    int ENTER_CHAT_ROOM_RESULT = 1005;

    int SHOW_USER_LIST = 1006;
    int SHOW_USER_LIST_RESULT = 1007;
}
