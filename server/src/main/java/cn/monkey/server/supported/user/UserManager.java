package cn.monkey.server.supported.user;

import cn.monkey.server.Session;

import java.util.Collection;

public interface UserManager {
    User findOrCreate(Session session, String token);

    User find(String uid);

    Collection<User> findAll();
}
