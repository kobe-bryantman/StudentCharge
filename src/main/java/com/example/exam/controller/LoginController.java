package com.example.exam.controller;

import com.example.exam.common.BusinessException;
import com.example.exam.entity.User;
import com.example.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 登录控制器
 * 处理登录页面展示、登录提交、退出登录、根路径跳转
 *
 * @author example
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 根路径，根据登录状态和角色跳转
     */
    @GetMapping("/")
    public String root(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:" + getIndexUrl(user.getRole());
    }

    /**
     * 跳转登录页面
     * 访问 http://localhost:3721/login
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            HttpServletRequest request,
                            Model model) {
        // 如果已登录，直接跳首页
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user != null) {
            return "redirect:" + getIndexUrl(user.getRole());
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "login";
    }

    /**
     * 处理登录提交（真实数据库认证）
     */
    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletRequest request,
                          Model model) {
        try {
            // 调用Service进行认证
            User user = userService.login(username, password);
            // 登录成功，设置session
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            // 根据角色跳转首页
            return "redirect:" + getIndexUrl(user.getRole());
        } catch (BusinessException e) {
            // 登录失败，携带错误信息返回登录页
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/login";
    }

    /**
     * 根据角色获取首页URL
     */
    private String getIndexUrl(String role) {
        if ("teacher".equals(role)) {
            return "/teacher/index";
        } else {
            return "/student/index";
        }
    }

}
