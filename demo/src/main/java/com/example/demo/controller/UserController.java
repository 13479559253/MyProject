package com.example.demo.controller;


import com.example.demo.exception.MyException;
import com.example.demo.pojo.Result;
import com.example.demo.pojo.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping(value = "/info")
    public Result<User> getUserInfoById(@RequestParam("id") Integer id){
        if(id == null){
            throw new MyException("id不可为空");
        }
        User user = userService.getUserInfoById(id);
        return Result.success(user);
    }
}
