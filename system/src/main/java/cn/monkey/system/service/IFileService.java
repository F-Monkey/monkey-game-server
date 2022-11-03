package cn.monkey.system.service;

import cn.monkey.commons.data.pojo.vo.Result;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface IFileService {

    default Result<?> upload(MultipartHttpServletRequest request) throws IOException {
        throw new UnsupportedOperationException();
    }

    default void download(HttpServletRequest request, HttpServletResponse response) throws IOException {
        throw new UnsupportedOperationException();
    }
}
