package com.example.demo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result <T>{
    private Integer code;
    private String msg;
    private T data;
    public static<T> Result<T> success(){
        return new Result<T>(1,"",null);
    }
    public static<T> Result<T> success(T data){
        return new Result<T>(1,"",data);
    }
    public static<T> Result<T> error(String msg,Integer code){
        return new Result<T>(code,msg,null);
    }
}
