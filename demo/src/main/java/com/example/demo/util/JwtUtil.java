package com.example.demo.util;

import com.example.demo.Constant.MyConst;
import com.example.demo.exception.MyException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.token.key}")
    private String KEY;
    private static final Integer CONTINUE_TIME = 3600 * 1000;
    private Key getKey(){
        return Keys.hmacShaKeyFor(KEY.getBytes());
    }
    public String createToken(Map<String,Object> claims) {
        return Jwts.builder().setClaims(claims).signWith(getKey()).
                setExpiration(new Date(System.currentTimeMillis() + CONTINUE_TIME)).
                compact();
    }

    public Claims parseToken(String token){
        try {
            return Jwts.parserBuilder().
                    setSigningKey(getKey()).
                    build().parseClaimsJws(token).
                    getBody();
        } catch (ExpiredJwtException e) {
            throw new MyException("令牌过期，请重新登录", MyConst.CODE_UNLOGIN);
        } catch (Exception e) {
            throw new MyException("令牌无效",MyConst.CODE_UNLOGIN);
        }
    }
}
