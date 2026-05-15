package com.example.demo.mapper;

import com.example.demo.pojo.DTO.LoginDTO;
import com.example.demo.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface UserMapper {
    void register(LoginDTO loginDTO);

    User checkUserByUsername(String username);

    User checkUserByPhone(String phone);

    List<User> select(@Param("name")String name, @Param("gender")String gender, @Param("role")Integer role,@Param("page")Integer page);

    void changeStatus(@Param("id")Integer id,@Param("status")Integer status);

    Integer getSelectCount(@Param("name")String name, @Param("gender")String gender, @Param("role")Integer role,@Param("page")Integer page);

    void changeUpdateTime(@Param("now")LocalDateTime now,@Param("id")Integer id);

    void changePasswordByCode(@Param("phone") String phone,@Param("password") String password);

    User checkUserById(@Param("id")Integer id);
}
