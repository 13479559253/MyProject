package com.example.demo.controller;

import com.example.demo.Constant.MyConst;
import com.example.demo.exception.MyException;
import com.example.demo.pojo.DTO.OrderDTO;
import com.example.demo.pojo.Order;
import com.example.demo.pojo.Result;
import com.example.demo.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping(value = "/create")
    public Result<?> createOrder(@RequestBody OrderDTO orderDTO, HttpServletRequest request){
        Integer id = (Integer) request.getAttribute("id");
        Integer role = (Integer) request.getAttribute("role");
        if(role != MyConst.ROLE_USER){
            throw new MyException("身份认证出错",MyConst.CODE_FORBID);
        }
        Integer orderId = orderService.createOrder(id,orderDTO);
        return Result.success(orderId);
    }

    @GetMapping(value = "/receive")
    public Result<?> receiveOrder(@RequestParam(value = "orderId") Integer orderId, HttpServletRequest request){
        Integer id = (Integer) request.getAttribute("id");
        Integer role = (Integer) request.getAttribute("role");
        if(role != MyConst.ROLE_DRIVER){
            throw new MyException("身份认证错误",MyConst.CODE_FORBID);
        }
        Integer userId = orderService.receiveOrder(orderId,id);
        return Result.success(userId);
    }

    @GetMapping(value = "/cancel")
    public Result<?> cancelOrder(@RequestParam(value = "orderId") Integer id){
        orderService.cancelOrder(id);
        return Result.success();
    }

    @GetMapping(value = "/complete")
    public Result<?> completeOrder(@RequestParam(value = "orderId") Integer id){
        orderService.completeOrder(id);
        return Result.success();
    }
}
