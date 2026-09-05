package com.example.exam.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 全局异常处理器
 * 适配Thymeleaf服务端渲染模式，异常时跳转到错误页面
 *
 * @author example
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获业务异常，携带错误信息跳转回登录页
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        log.warn("业务异常：{}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "login";
    }

    /**
     * 捕获其他未知异常，跳转到错误页面
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("系统异常：", e);
        model.addAttribute("error", "系统繁忙，请稍后重试");
        return "error";
    }

}
