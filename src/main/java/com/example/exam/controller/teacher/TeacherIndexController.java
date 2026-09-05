package com.example.exam.controller.teacher;

import com.example.exam.controller.LoginController;
import com.example.exam.entity.Course;
import com.example.exam.entity.User;
import com.example.exam.service.CourseService;
import com.example.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 教师首页控制器
 *
 * @author example
 */
@Controller
@RequestMapping("/teacher")
public class TeacherIndexController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuestionService questionService;

    /**
     * 获取当前登录教师
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    /**
     * GET /teacher/index 教师首页
     */
    @GetMapping("/index")
    public String index(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        List<Course> courses = courseService.listByTeacherId(teacher.getId());
        model.addAttribute("courses", courses);
        model.addAttribute("courseCount", courses.size());
        long questionCount = 0;
        for (Course c : courses) {
            questionCount += questionService.countByCourseId(c.getId());
        }
        model.addAttribute("questionCount", questionCount);
        return "teacher/index";
    }

}
