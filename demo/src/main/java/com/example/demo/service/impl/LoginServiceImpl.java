package com.example.demo.service.impl;

import com.example.demo.exception.MyException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.pojo.DTO.CodeLoginDTO;
import com.example.demo.pojo.DTO.LoginDTO;
import com.example.demo.pojo.DTO.ModifyPasswordDTO;
import com.example.demo.pojo.User;
import com.example.demo.service.LoginService;
import com.example.demo.util.JwtUtil;
import com.example.demo.util.RedisUtil;
import com.example.demo.util.RegexUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private RedisUtil redisUtil;

    public String LoginByUsername(LoginDTO loginDTO) {
        String lockKey = "Login:Lock:" + loginDTO.getUsername();
        if(redisUtil.get(lockKey) != null){
            throw new MyException("用户信息错误次数过多，请稍后再试");
        }
        RegexUtil.checkUsername(loginDTO.getUsername());
        RegexUtil.checkPassword(loginDTO.getPassword());
        User u = userMapper.checkUserByUsername(loginDTO.getUsername());
        if (u == null) {
            throw new MyException("用户名或密码错误");
        }
        if(u.getStatus() == 0){
            throw new MyException("该用户已被管理员封禁");
        }
        String errorCountKey = "Login:ErrorCount:" + loginDTO.getUsername();
        if(!BCrypt.checkpw(loginDTO.getPassword(), u.getPassword())) {
            int errorCount = redisUtil.get(errorCountKey) == null ? 0 : Integer.parseInt(redisUtil.get(errorCountKey));
            redisUtil.set(errorCountKey, String.valueOf(errorCount + 1) , 60);
            if(errorCount + 1 >= 5){
                redisUtil.set(lockKey,"1",60);
            }
            throw new MyException("用户名或密码错误");
        }
        redisUtil.remove(errorCountKey);
        Map<String,Object> map = new HashMap<>();
        map.put("name",u.getName());
        map.put("id",u.getId());
        map.put("role",u.getRole());
        map.put("phone",u.getPhone());
        return jwtUtil.createToken(map);
    }

    public String LoginByCode(CodeLoginDTO codeLoginDTO) {
        RegexUtil.checkPhone(codeLoginDTO.getPhone());
        String codeKey = "Login:Code:" + codeLoginDTO.getPhone();
        if(codeLoginDTO.getCode().isEmpty()){
            throw new MyException("验证码不可为空");
        }
        User u = userMapper.checkUserByPhone(codeLoginDTO.getPhone());
        if (u == null) {
            throw new MyException("验证码错误");
        }
        if(!Objects.equals(redisUtil.get(codeKey), codeLoginDTO.getCode())){
            throw new MyException("验证码错误");
        }
        if(u.getStatus() == 0){
            throw new MyException("该用户已被管理员封禁");
        }
        redisUtil.remove(codeKey);
        Map<String,Object> map = new HashMap<>();
        map.put("name",u.getName());
        map.put("id",u.getId());
        map.put("role",u.getRole());
        map.put("phone",u.getPhone());
        return jwtUtil.createToken(map);
    }

    public void SendCodeByPhone(String phone,String type) {
        RegexUtil.checkPhone(phone);
        String codeKey = "Login:Code:" + phone;
        String codeLockKey = "Login:Code:Lock:" + phone;
        if(type.equals("modify")){
            codeKey = "Password:Code:" + phone;
            codeLockKey = "Password:Code:Lock:" + phone;
        }
        if(redisUtil.get(codeLockKey) != null){
            throw new MyException("验证码已发送，请稍后");
        }
        String code = getCode();
        redisUtil.set(codeKey,code,120);
        redisUtil.set(codeLockKey,"1",60);
        System.out.println("向电话为" + phone + "发送验证码:" + code);
    }

    public void register(LoginDTO loginDTO) {
        RegexUtil.checkUsername(loginDTO.getUsername());
        RegexUtil.checkPassword(loginDTO.getPassword());
        User u = userMapper.checkUserByUsername(loginDTO.getUsername());
        if (u != null) {
            throw new MyException("注册失败，请更换信息重试");
        }
        loginDTO.setPassword(BCrypt.hashpw(loginDTO.getPassword(), BCrypt.gensalt()));
        userMapper.register(loginDTO);
    }

    public void changePasswordByCode(ModifyPasswordDTO modifyPasswordDTO) {
        RegexUtil.checkPhone(modifyPasswordDTO.getPhone());
        RegexUtil.checkPassword(modifyPasswordDTO.getPassword());
        String codeKey = "Password:Code:" + modifyPasswordDTO.getPhone();
        if(modifyPasswordDTO.getCode().isEmpty()){
            throw new MyException("验证码不可为空");
        }
        User u = userMapper.checkUserByPhone(modifyPasswordDTO.getPhone());
        if (u == null) {
            throw new MyException("验证码错误");
        }
        if(!Objects.equals(redisUtil.get(codeKey), modifyPasswordDTO.getCode())){
            throw new MyException("验证码错误");
        }
        if(u.getStatus() == 0){
            throw new MyException("该用户已被管理员封禁");
        }
        redisUtil.remove(codeKey);
        System.out.println("密码修改为" + modifyPasswordDTO.getPassword());
        modifyPasswordDTO.setPassword(BCrypt.hashpw(modifyPasswordDTO.getPassword(), BCrypt.gensalt()));
        userMapper.changePasswordByCode(modifyPasswordDTO.getPhone(),modifyPasswordDTO.getPassword());
    }

    public String getCode(){
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int code = random.nextInt(0,900000) + 100000;
        return String.valueOf(code);
    }
}
