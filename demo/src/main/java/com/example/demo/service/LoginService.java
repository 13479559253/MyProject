package com.example.demo.service;

import com.example.demo.pojo.DTO.CodeLoginDTO;
import com.example.demo.pojo.DTO.LoginDTO;
import com.example.demo.pojo.DTO.ModifyPasswordDTO;

public interface LoginService {

    String LoginByUsername(LoginDTO loginDTO);

    String LoginByCode(CodeLoginDTO codeLoginDTO);

    void SendCodeByPhone(String phone,String type);

    void register(LoginDTO loginDTO);

    void changePasswordByCode(ModifyPasswordDTO modifyPasswordDTO);
}
