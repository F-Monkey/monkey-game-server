package cn.monkey.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
@RestController
public class GatewayApplication {

    private static final Logger log = LoggerFactory.getLogger(GatewayApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(GatewayApplication.class, args);
        RequestMappingHandlerMapping bean = run.getBean(RequestMappingHandlerMapping.class);
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = bean.getHandlerMethods();
        List<PathPattern> urlList = new ArrayList<>();
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            Set<PathPattern> patterns = info.getPatternsCondition().getPatterns();
            urlList.addAll(patterns);
        }
        log.info("urlList: {}", urlList);
    }

    @GetMapping("/test")
    public String test() {
        return "test";
    }
}
