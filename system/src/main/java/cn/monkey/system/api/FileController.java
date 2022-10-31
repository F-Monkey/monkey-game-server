package cn.monkey.system.api;

import cn.monkey.commons.bean.BeanContext;
import cn.monkey.commons.data.pojo.vo.Result;
import cn.monkey.commons.data.pojo.vo.Results;
import cn.monkey.system.service.IFileService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;

@Controller
@RequestMapping("/file")
public class FileController {

    private final BeanContext<IFileService> beanContext;

    public FileController(BeanContext<IFileService> beanContext) {
        this.beanContext = beanContext;
    }

    @PostMapping("/{moduleId}")
    Result<?> upload(@PathVariable("moduleId") String moduleId,
                     MultipartHttpServletRequest request) throws IOException {
        IFileService bean = this.beanContext.getBean(moduleId);
        if (bean == null) {
            return Results.fail("invalid moduleId");
        }
        return bean.upload(request);
    }
}
