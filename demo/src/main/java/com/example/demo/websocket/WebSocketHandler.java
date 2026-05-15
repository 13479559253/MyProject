package com.example.demo.websocket;

import com.example.demo.Constant.MyConst;
import com.example.demo.pojo.SocketMsg;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.PositionUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class WebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private JwtUtil jwtUtil;
    private static final Map<Integer, WebSocketSession> users = new ConcurrentHashMap<>();
    private static final Map<Integer, WebSocketSession> drivers = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if(query == null)return;
        String token = query.split("=")[1];
        Claims claims = jwtUtil.parseToken(token);
        Integer id = (Integer) claims.get("id");
        Integer role = (Integer) claims.get("role");
        session.getAttributes().put("id", id);
        session.getAttributes().put("role", role);
        if(role == MyConst.ROLE_USER){
            users.put(id, session);
        } else if(role == MyConst.ROLE_DRIVER){
            drivers.put(id, session);
        }
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
        try{
            String payload = message.getPayload();
            SocketMsg<?> socketMsg = objectMapper.readValue(payload, SocketMsg.class);
            String type = socketMsg.getType();
            switch (type){
                case MyConst.WEBSOCKET_POSITION:
                    sendPosition(session, socketMsg);
                    break;
                case MyConst.WEBSOCKET_ORDER_NEW:
                    sendMsgToAll(socketMsg);
                    break;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //实现司机接单之后通知用户司机已接单(需要添加司机传递信息)
    public static void sendAcceptMessage(Integer id){
        WebSocketSession session = users.get(id);
        SocketMsg<?> socketMsg = new SocketMsg<>();
        socketMsg.setType(MyConst.WEBSOCKET_ORDER_RECEIVE);
        if(session != null){
            String json = objectMapper.writeValueAsString(socketMsg);
            TextMessage textMessage = new TextMessage(json);
            try {
                session.sendMessage(textMessage);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void sendCancelMessage(Integer id){
        WebSocketSession session = drivers.get(id);
        SocketMsg<?> socketMsg = new SocketMsg<>();
        socketMsg.setType(MyConst.WEBSOCKET_ORDER_CANCEL);
        if(session != null){
            String json = objectMapper.writeValueAsString(socketMsg);
            TextMessage textMessage = new TextMessage(json);
            try{
                session.sendMessage(textMessage);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static void sendCompleteMessage(Integer id){
        WebSocketSession session = users.get(id);
        SocketMsg<?> socketMsg = new SocketMsg<>();
        socketMsg.setType(MyConst.WEBSOCKET_ORDER_COMPLETE);
        if(session != null){
            String json = objectMapper.writeValueAsString(socketMsg);
            TextMessage textMessage = new TextMessage(json);
            try{
                session.sendMessage(textMessage);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    //实现司机对订单用户的实时传递位置信息
    private void sendPosition(WebSocketSession session, SocketMsg<?> msg) throws Exception {
        PositionData positionData = objectMapper.convertValue(msg.getData(), PositionData.class);
        if(positionData == null) return;
        if(positionData.getId() != null){
            WebSocketSession userSession = users.get(positionData.getId());
            if(userSession == null || !userSession.isOpen()){
                return;
            }
            String json = objectMapper.writeValueAsString(msg);
            TextMessage message = new TextMessage(json);
            synchronized (userSession) {
                userSession.sendMessage(message);
            }
        }
    }

    //实现对所有司机的广播
    public static void sendMsgToAll(SocketMsg<?> msg){
        drivers.forEach((id,driverSession) -> {
            String json = objectMapper.writeValueAsString(msg);
            TextMessage message = new TextMessage(json);
            try {
                synchronized (driverSession) {
                    driverSession.sendMessage(message);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        Integer id = (Integer) session.getAttributes().get("id");
        Integer role = (Integer) session.getAttributes().get("role");
        if(id == null)return;
        if(role == MyConst.ROLE_USER){
            users.remove(id);
        } else if(role == MyConst.ROLE_DRIVER){
            drivers.remove(id);
        }
    }
}
