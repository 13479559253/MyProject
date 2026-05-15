package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String name;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String gender;
    private Integer role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
