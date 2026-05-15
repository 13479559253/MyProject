package com.example.demo.service;

import com.example.demo.pojo.DTO.OrderDTO;
import com.example.demo.pojo.Order;

import java.util.List;

public interface OrderService {

    Integer createOrder(Integer userId, OrderDTO orderDTO);

    Integer receiveOrder(Integer orderId, Integer driverId);

    void cancelOrder(Integer id);

    void completeOrder(Integer id);
}
