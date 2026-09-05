package com.example.exam.config;

import com.example.exam.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 * 保护/teacher/*和/student/*路径，未登录用户重定向到登录页
 *
 * @author example
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // 获取session
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // 未登录，重定向到登录页
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        // 角色权限校验
        if (uri.startsWith("/teacher/") && !"teacher".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login?error=无权限访问教师端");
            return false;
        }
        if (uri.startsWith("/student/") && !"student".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login?error=无权限访问学生端");
            return false;
        }

        return true;
    }

}
