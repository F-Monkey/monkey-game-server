package cn.monkey.gateway.controller;

import cn.monkey.gateway.service.UserService;
import cn.monkey.proto.Command;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/signIn")
    Mono<Command.PackageGroup> signIn(@RequestBody Mono<Command.Package> requestEntity) {
        return this.userService.login(requestEntity);
    }

    @PostMapping(value = "/wx/signIn", consumes = {"application/x-protobuf"}, produces = {"application/x-protobuf"})
    Mono<Command.PackageGroup> wxSignIn(@RequestBody Mono<Command.Package> requestEntity) {
        return this.userService.wxSignIn(requestEntity);
    }

    @RequestMapping("/register")
    @PostMapping
    Mono<Command.PackageGroup> register(Mono<Command.Package> request) {
        return this.userService.register(request);
    }
}
