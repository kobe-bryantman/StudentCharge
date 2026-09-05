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
 * session key = "loginUser"
 *
 * @author example
 */
@Controller
public class LoginController {

    /** Session中存储当前登录用户的key */
    public static final String LOGIN_USER = "loginUser";

    @Autowired
    private UserService userService;

    /**
     * 根路径，根据登录状态和角色跳转
     */
    @GetMapping("/")
    public String root(HttpServletRequest request) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(LOGIN_USER);
        if (user == null) {
            return "redirect:/login";
        }
        return "redirect:" + getIndexUrl(user.getRole());
    }

    /**
     * GET /login 返回登录页，如果已登录则直接跳对应首页
     */
    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(LOGIN_USER);
        if (user != null) {
            return "redirect:" + getIndexUrl(user.getRole());
        }
        return "login";
    }

    /**
     * POST /doLogin 处理登录提交
     * 登录成功：session存loginUser，按role跳转
     * 登录失败：返回登录页，model加msg="账号或密码错误"
     */
    @PostMapping("/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpServletRequest request,
                          Model model) {
        try {
            User user = userService.login(username, password);
            HttpSession session = request.getSession();
            session.setAttribute(LOGIN_USER, user);
            return "redirect:" + getIndexUrl(user.getRole());
        } catch (BusinessException e) {
            // 登录失败，统一提示
            model.addAttribute("msg", "账号或密码错误");
            return "login";
        }
    }

    /**
     * GET /logout 清除session，重定向登录页
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
