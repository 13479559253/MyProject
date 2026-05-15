package com.example.demo.interceptor;

import com.example.demo.Constant.MyConst;
import com.example.demo.exception.MyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RoleInterception implements HandlerInterceptor {
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        Integer role = (Integer) request.getAttribute("role");
        String uri = request.getRequestURI();
        if(uri.startsWith("/admin/")){
            if(role == null || role != MyConst.ROLE_ADMIN){
                throw new MyException("权限不足",MyConst.CODE_FORBID);
            }
        }
        return true;
    }
}
