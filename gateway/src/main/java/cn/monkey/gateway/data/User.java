package cn.monkey.gateway.data;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;


@Data
public class User implements Serializable {
    @Id
    private String id;
    private String openId;
    private String username;
    private String password;
}
