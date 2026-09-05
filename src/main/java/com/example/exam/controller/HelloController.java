package com.example.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Hello控制器
 * 用于验证项目启动是否成功
 *
 * @author example
 */
@Controller
@RequestMapping("/hello")
public class HelloController {

    /**
     * 测试接口
     * 访问 http://localhost:3721/hello 返回字符串
     */
    @GetMapping
    @ResponseBody
    public String hello() {
        return "在线考试系统启动成功！Hello World！";
    }

}
