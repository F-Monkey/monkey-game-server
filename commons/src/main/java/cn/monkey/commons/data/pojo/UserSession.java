package cn.monkey.commons.data.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserSession implements Serializable {
    private String uid;
    private String username;
    private String hallServerId;
    private String hallServerUrl;
    private String gameServerId;
    private String gameServerUrl;
}
