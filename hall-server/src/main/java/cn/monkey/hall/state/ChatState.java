package cn.monkey.hall.state;

import cn.monkey.commons.utils.Timer;
import cn.monkey.state.core.OncePerInitState;
import cn.monkey.state.core.StateGroup;

public class ChatState extends OncePerInitState {

    public static final String CODE = "chat";

    public ChatState(Timer timer, StateGroup stateGroup) {
        super(CODE, timer, stateGroup);
    }

    @Override
    protected void onInit() {

    }

    @Override
    public void fireEvent(Object event) throws Exception {

    }

    @Override
    public String finish() throws Exception {
        return null;
    }
}
