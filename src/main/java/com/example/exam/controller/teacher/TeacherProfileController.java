package com.example.exam.controller.teacher;

import com.example.exam.controller.LoginController;
import com.example.exam.entity.Course;
import com.example.exam.entity.User;
import com.example.exam.service.CourseService;
import com.example.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 教师个人信息控制器
 * 显示教师信息 + 教授课程列表，仅可修改姓名/性别/手机号/学院
 *
 * @author example
 */
@Controller
@RequestMapping("/teacher")
public class TeacherProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    /**
     * 获取当前登录教师
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    /**
     * GET /teacher/profile 个人信息页面
     * 显示教师信息 + 教授课程列表
     */
    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        // 查询教授的所有课程
        List<Course> courses = courseService.listByTeacherId(teacher.getId());
        model.addAttribute("user", teacher);
        model.addAttribute("courses", courses);
        return "teacher/profile";
    }

    /**
     * POST /teacher/profile/update 修改个人信息
     * 工号不可修改，教授课程不可在此修改
     * 使用 PRG 模式（Post-Redirect-Get），通过 flash attribute 传递成功提示
     */
    @PostMapping("/profile/update")
    public String profileUpdate(@RequestParam String name,
                                @RequestParam String gender,
                                @RequestParam String phone,
                                @RequestParam String college,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User current = getCurrentUser(request);
        // 只更新允许修改的字段
        current.setName(name);
        current.setGender(gender);
        current.setPhone(phone);
        current.setCollege(college);
        userService.update(current);
        // 更新session中的用户信息
        request.getSession().setAttribute(LoginController.LOGIN_USER, current);
        // 通过 flash attribute 传递成功提示，重定向回 profile 页
        redirectAttributes.addFlashAttribute("msg", "保存成功");
        return "redirect:/teacher/profile";
    }

}
