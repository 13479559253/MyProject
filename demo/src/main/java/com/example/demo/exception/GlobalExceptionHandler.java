package com.example.demo.exception;

import com.example.demo.pojo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MyException.class)
    public Result<?> handleMyException(MyException e) {
        return Result.error(e.getMessage(),e.getCode());
    }
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e){
        return Result.error(e.getMessage(),500);
    }
}
