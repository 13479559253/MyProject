package com.example.demo.mapper;

import com.example.demo.pojo.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    Integer createOrder(Order order);

    int receiveOrder(@Param("id") Integer id,@Param("driverId") Integer driverId,@Param("version") Integer version);

    Order checkOrderById(Integer orderId);

    List<Order> checkOrder(Integer page);

    void changeStatusById(@Param("id") Integer orderId, @Param("status") Integer status);
}
