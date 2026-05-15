package com.example.demo.interceptor;

import com.example.demo.Constant.MyConst;
import com.example.demo.exception.MyException;
import com.example.demo.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("token");
        if (token == null) {
            throw new MyException("请先进行登录", MyConst.CODE_UNLOGIN);
        }
        Claims claims = jwtUtil.parseToken(token);
        request.setAttribute("id", claims.get("id"));
        request.setAttribute("role", claims.get("role"));
        return true;
    }
}
