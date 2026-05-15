package com.example.demo.controller;

import com.example.demo.exception.MyException;
import com.example.demo.pojo.DTO.CodeLoginDTO;
import com.example.demo.pojo.DTO.LoginDTO;
import com.example.demo.pojo.DTO.ModifyPasswordDTO;
import com.example.demo.pojo.Result;
import com.example.demo.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/login")
public class LoginController {
    @Autowired
    private LoginService loginService;

    @PostMapping(value = "/username")
    public Result<?> LoginByUsername(@RequestBody LoginDTO loginDTO){
        if(loginDTO == null){
            throw new MyException("传入信息不可为空");
        }
        String token = loginService.LoginByUsername(loginDTO);
        return Result.success(token);
    }

    @PostMapping(value = "/phone")
    public Result<?> LoginByPhone(@RequestBody CodeLoginDTO codeLoginDTO){
        if(codeLoginDTO == null){
            throw new MyException("传入信息不可为空");
        }
        String token = loginService.LoginByCode(codeLoginDTO);
        return Result.success(token);
    }

    @GetMapping(value = "/phone/code")
    public Result<?> SendCodeByPhone(@RequestParam("phone") String phone,@RequestParam("type") String type){
        if(phone == null){
            throw new MyException("传入信息不可为空");
        }
        loginService.SendCodeByPhone(phone,type);
        return Result.success();
    }

    @PostMapping(value = "/register")
    public Result<?> register(@RequestBody LoginDTO loginDTO){
        if(loginDTO == null){
            throw new MyException("传入信息不可为空");
        }
        loginService.register(loginDTO);
        return Result.success();
    }

    @PostMapping(value = "/phone/password")
    public Result<?> changePasswordByCode(@RequestBody ModifyPasswordDTO modifyPasswordDTO){
        if(modifyPasswordDTO == null){
            throw new MyException("传入信息不可为空");
        }
        loginService.changePasswordByCode(modifyPasswordDTO);
        return Result.success();
    }
}
