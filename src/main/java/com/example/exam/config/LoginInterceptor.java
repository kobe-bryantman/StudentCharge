package com.example.exam.config;

import com.example.exam.controller.LoginController;
import com.example.exam.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 * 1. 未登录用户重定向到 /login
 * 2. 访问 /teacher/** 要求 role=teacher
 * 3. 访问 /student/** 要求 role=student
 *
 * @author example
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 放行登录相关路径（双重保险，WebMvcConfig也已排除）
        String contextPath = request.getContextPath();
        if (uri.startsWith(contextPath + "/login")
                || uri.startsWith(contextPath + "/doLogin")
                || uri.startsWith(contextPath + "/logout")
                || uri.startsWith(contextPath + "/hello")
                || uri.startsWith(contextPath + "/css/")
                || uri.startsWith(contextPath + "/js/")
                || uri.startsWith(contextPath + "/images/")) {
            return true;
        }

        // 获取session中的登录用户
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(LoginController.LOGIN_USER);

        // 未登录，重定向到登录页
        if (user == null) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        // 角色权限校验
        if (uri.startsWith(contextPath + "/teacher/") && !"teacher".equals(user.getRole())) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }
        if (uri.startsWith(contextPath + "/student/") && !"student".equals(user.getRole())) {
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        return true;
    }

}
