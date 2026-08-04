package com.macedxs.mx.identity.controller;

import com.macedxs.mx.identity.entity.UserEntity;
import com.macedxs.mx.identity.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserEntity> findAll() {
        return service.findAll();
    }
}