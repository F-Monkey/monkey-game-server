package cn.monkey.commons.data.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ServerInfo {
    private String code;
    private List<String> names;
}
