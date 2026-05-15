package com.example.demo.util;

import com.example.demo.exception.MyException;

public class RegexUtil {
    public static void checkEmail(String email){
        if(email == null){
            throw new MyException("邮箱不可为空");
        }
        String emailRegx = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+\\.com$";
        if(!email.matches(emailRegx)) {
            throw new MyException("邮箱格式错误");
        }
    }
    public static void checkUsername(String username){
        if(username == null){
            throw new MyException("用户名不可为空");
        }
        String usernameRegx = "^[a-zA-Z0-9]{6,12}$";
        if(!username.matches(usernameRegx)) {
            throw new MyException("用户名格式错误");
        }
    }
    public static void checkPassword(String password){
        if(password == null){
            throw new MyException("密码不可为空");
        }
        String passwordRegx = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,12}$";
        if(!password.matches(passwordRegx)) {
            throw new MyException("密码格式错误");
        }
    }

    public static void checkPhone(String phone) {
        if(phone == null){
            throw new MyException("电话号码不可为空");
        }
        String phoneRegx = "^[1][0-9]{10}$";
        if(!phone.matches(phoneRegx)) {
            throw new MyException("电话号码格式错误");
        }
    }
}
