package com.example.demo.service.impl;

import com.example.demo.Constant.MyConst;
import com.example.demo.exception.MyException;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.pojo.DTO.OrderDTO;
import com.example.demo.pojo.Order;
import com.example.demo.pojo.SocketMsg;
import com.example.demo.service.OrderService;
import com.example.demo.util.PositionUtil;
import com.example.demo.websocket.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    public Integer createOrder(Integer userId, OrderDTO orderDTO) {
        PositionUtil.checkLatLng(orderDTO.getStartLatitude(), orderDTO.getStartLongitude(),
                orderDTO.getEndLatitude(), orderDTO.getEndLongitude());
        Order order = new Order();
        order.setUserId(userId);
        order.setStartLatitude(orderDTO.getStartLatitude());
        order.setStartLongitude(orderDTO.getStartLongitude());
        order.setEndLatitude(orderDTO.getEndLatitude());
        order.setEndLongitude(orderDTO.getEndLongitude());
        order.setStartAddress(orderDTO.getStartAddress());
        order.setEndAddress(orderDTO.getEndAddress());
        order.setVersion(0);
        order.setStatus(MyConst.ORDER_WAIT);
        orderMapper.createOrder(order);
        SocketMsg<OrderDTO> socketMsg = new SocketMsg<>();
        orderDTO.setOrderId(order.getId());
        socketMsg.setData(orderDTO);
        socketMsg.setType(MyConst.WEBSOCKET_ORDER_NEW);
        WebSocketHandler.sendMsgToAll(socketMsg);
        return order.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer receiveOrder(Integer orderId, Integer driverId) {
        Order order = orderMapper.checkOrderById(orderId);
        if(order == null){
            throw new MyException("订单不存在");
        }
        if(order.getStatus() != MyConst.ORDER_WAIT){
            throw new MyException("订单被抢走了");
        }
        int row = orderMapper.receiveOrder(orderId,driverId,order.getVersion());
        if(row == 0){
            throw new MyException("订单被抢走了");
        }
        SocketMsg<Integer> socketMsg = new SocketMsg<>();
        socketMsg.setData(orderId);
        socketMsg.setType(MyConst.WEBSOCKET_ORDER_RECEIVE);
        WebSocketHandler.sendMsgToAll(socketMsg);
        WebSocketHandler.sendAcceptMessage(order.getUserId());
        return order.getUserId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Integer id) {
        Order order = orderMapper.checkOrderById(id);
        if(order == null){
            throw new MyException("订单信息错误");
        }
        if(order.getStatus() == MyConst.ORDER_COMPLETE || order.getStatus() == MyConst.ORDER_CANCEL){
            throw new MyException("订单状态错误,不可取消");
        }
        else if(order.getStatus() == MyConst.ORDER_WAIT){
            SocketMsg<Integer> socketMsg = new SocketMsg<>();
            socketMsg.setData(id);
            socketMsg.setType(MyConst.WEBSOCKET_ORDER_RECEIVE);
            WebSocketHandler.sendMsgToAll(socketMsg);
        }
        else if(order.getStatus() == MyConst.ORDER_CONTINUE){
            WebSocketHandler.sendCancelMessage(order.getDriverId());
        }
        orderMapper.changeStatusById(id,MyConst.ORDER_CANCEL);
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Integer id) {
        Order order = orderMapper.checkOrderById(id);
        if(order == null){
            throw new MyException("订单信息错误");
        }
        if(order.getStatus() != MyConst.ORDER_CONTINUE){
            throw new MyException("订单状态错误,完成失败");
        }
        WebSocketHandler.sendCompleteMessage(order.getUserId());
        orderMapper.changeStatusById(id,MyConst.ORDER_COMPLETE);
    }

}
