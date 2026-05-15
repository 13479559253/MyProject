package com.example.demo.controller;

import com.example.demo.exception.MyException;
import com.example.demo.pojo.Order;
import com.example.demo.pojo.Result;
import com.example.demo.pojo.User;
import com.example.demo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping(value = "/check/user")
    public Result<?> select(@RequestParam(value="name",required=false)String name,
                            @RequestParam(value="gender",required=false)String gender,
                            @RequestParam(value="role",required=false)Integer role,
                            @RequestParam(value="page",required=false,defaultValue="0")Integer page) {
        List<User> userList = adminService.select(name,gender,role,page);
        Integer count = adminService.getSelectCount(name,gender,role,page);
        Map<String ,Object> map = new HashMap<>();
        map.put("userList",userList);
        map.put("total",count);
        return Result.success(map);
    }

    @GetMapping(value = "/ban")
    public Result<?> ban(@RequestParam(value="id")Integer id){
        if (id == null || id <= 0) {
            throw new MyException("用户ID不合法");
        }
        adminService.ban(id);
        return Result.success();
    }

    @GetMapping(value = "/recover")
    public Result<?> recover(@RequestParam(value="id")Integer id){
        if (id == null || id <= 0) {
            throw new MyException("用户ID不合法");
        }
        adminService.recover(id);
        return Result.success();
    }

    @GetMapping(value = "/check/order")
    public Result<?> checkOrder(@RequestParam(value = "page", defaultValue = "0") Integer page){
        List<Order> orderList = adminService.checkOrder(page);
        return Result.success(orderList);
    }
}
