package cn.monkey.hall.state;

import cn.monkey.server.supported.user.User;
import cn.monkey.server.supported.user.UserManager;
import cn.monkey.state.core.StateContext;

import java.util.Collection;

public class ChatServerContext implements StateContext {

    private final UserManager userManager;

    public ChatServerContext(UserManager userManager) {
        this.userManager = userManager;
    }

    public Collection<User> findAll() {
        return this.userManager.findAll();
    }
}
