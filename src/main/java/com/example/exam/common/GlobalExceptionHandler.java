package com.example.exam.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理器
 * 统一捕获各类异常，避免500错误暴露给用户
 *
 * @author example
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 捕获业务异常 → 重定向到错误页显示友好提示
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException e, Model model) {
        log.warn("业务异常：{}", e.getMessage());
        model.addAttribute("error", e.getMessage());
        return "error";
    }

    /**
     * 捕获参数校验异常（@Valid + @ModelAttribute）→ 返回表单页显示错误
     * 通过RedirectAttributes携带错误信息重定向回来源页
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidException(MethodArgumentNotValidException e,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        log.warn("参数校验异常：{}", e.getMessage());
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        redirectAttributes.addFlashAttribute("msg", msg);
        redirectAttributes.addFlashAttribute("msgType", "error");
        // 重定向回来源页
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer.replaceFirst("http://[^/]+", "") : "/");
    }

    /**
     * 捕获绑定异常（表单对象绑定失败）
     */
    @ExceptionHandler(BindException.class)
    public String handleBindException(BindException e,
                                      HttpServletRequest request,
                                      RedirectAttributes redirectAttributes) {
        log.warn("绑定异常：{}", e.getMessage());
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        redirectAttributes.addFlashAttribute("msg", msg);
        redirectAttributes.addFlashAttribute("msgType", "error");
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer.replaceFirst("http://[^/]+", "") : "/");
    }

    /**
     * 捕获所有未处理异常 → 记录错误日志，返回系统异常提示
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model) {
        log.error("系统异常：", e);
        model.addAttribute("error", "系统异常，请联系管理员");
        return "error";
    }

}
