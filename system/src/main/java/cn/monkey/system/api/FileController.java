package cn.monkey.system.api;

import cn.monkey.commons.bean.BeanContext;
import cn.monkey.commons.data.pojo.vo.Result;
import cn.monkey.commons.data.pojo.vo.Results;
import cn.monkey.system.service.IFileService;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/file")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private final BeanContext<IFileService> beanContext;

    public FileController(BeanContext<IFileService> beanContext) {
        this.beanContext = beanContext;
    }

    @PostMapping("/{moduleId}")
    @ResponseBody
    Result<?> upload(@PathVariable("moduleId") String moduleId,
                     MultipartHttpServletRequest request) throws IOException {
        IFileService bean = this.beanContext.getBean(moduleId);
        if (bean == null) {
            return Results.fail("invalid moduleId");
        }
        long start = System.currentTimeMillis();
        Result<?> upload = bean.upload(request);
        long end = System.currentTimeMillis();
        log.info("file upload cost: {} ms", end - start);
        return upload;
    }

    @GetMapping("/{moduleId}")
    void download(@PathVariable("moduleId") String moduleId,
                  HttpServletRequest request,
                  HttpServletResponse response) throws IOException {
        IFileService bean = this.beanContext.getBean(moduleId);
        if (bean == null) {
            this.writeClientError(response, Results.fail("invalid arguments [moduleId]"));
            return;
        }
        long start = System.currentTimeMillis();
        bean.download(request, response);
        long end = System.currentTimeMillis();
        log.info("download cost: {} ms", end - start);
    }

    private static Gson gson = new Gson();

    private void writeClientError(HttpServletResponse response, Result<Object> fail) throws IOException {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        try (OutputStream outputStream = response.getOutputStream()) {
            outputStream.write(gson.toJson(fail).getBytes(StandardCharsets.UTF_8));
        }
    }


}
