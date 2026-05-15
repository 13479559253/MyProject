package com.example.demo.service;

import com.example.demo.pojo.Order;
import com.example.demo.pojo.User;
import java.util.List;
public interface AdminService {

    List<User> select(String name, String gender, Integer role,Integer page);

    void ban(Integer id);

    void recover(Integer id);

    Integer getSelectCount(String name, String gender, Integer role, Integer page);

    List<Order> checkOrder(Integer page);
}
