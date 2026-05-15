package com.example.demo.exception;

public class MyException extends RuntimeException {
    private Integer code = 200;
    public MyException(String message,Integer code) {
        super(message);
        this.code = code;
    }
    public MyException(String message) {
        super(message);
    }
    public Integer getCode(){
        return code;
    }

}
