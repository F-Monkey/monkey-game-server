package cn.monkey.server.supported.user;

import cn.monkey.commons.utils.Timer;
import cn.monkey.server.Session;

public class SimpleUser implements User {

    private final String uid;

    private final String username;

    private Session session;

    private Timer timer;

    private volatile long lastOperationTime;

    private long maxActiveInterval;

    public SimpleUser(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    @Override
    public String getUid() {
        return this.uid;
    }

    @Override
    public Session getSession() {
        return this.session;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public void setSession(Session session) {
        this.session = session;
    }

    @Override
    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void refreshLastOperateTime() {
        this.lastOperationTime = this.timer.getCurrentTimeMs();
    }

    public void setMaxActiveInterval(long maxActiveInterval) {
        this.maxActiveInterval = maxActiveInterval;
    }

    @Override
    public boolean isActive() {
        return User.super.isActive() && (this.maxActiveInterval <= 0 || this.timer.getCurrentTimeMs() - this.lastOperationTime > this.maxActiveInterval);
    }
}
