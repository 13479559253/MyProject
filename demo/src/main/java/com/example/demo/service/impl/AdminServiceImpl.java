package com.example.demo.service.impl;

import com.example.demo.Constant.MyConst;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.Order;
import com.example.demo.pojo.User;
import com.example.demo.service.AdminService;
import com.example.demo.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrderMapper orderMapper;

    public List<User> select(String name, String gender, Integer role,Integer page) {
        page = page * 14;
        return userMapper.select(name,gender,role,page);
    }
    public Integer getSelectCount(String name, String gender, Integer role, Integer page) {
        return userMapper.getSelectCount(name,gender,role,page);
    }
    public void ban(Integer id) {
        userMapper.changeStatus(id, MyConst.STATUS_BAN);
        String key = "Ban:User:" + id;
        redisUtil.setnx(key,"1",7200);
        userMapper.changeUpdateTime(LocalDateTime.now(),id);
    }

    public void recover(Integer id) {
        userMapper.changeStatus(id,MyConst.STATUS_NORMAL);
        String key = "Ban:User:" + id;
        if(redisUtil.exist(key)){
            redisUtil.remove(key);
        }
        userMapper.changeUpdateTime(LocalDateTime.now(),id);
    }

    public List<Order> checkOrder(Integer page) {
        page = page * 14;
        return orderMapper.checkOrder(page);
    }

}
