package com.example.exam.controller.student;

import com.example.exam.controller.LoginController;
import com.example.exam.entity.Course;
import com.example.exam.entity.User;
import com.example.exam.service.CourseService;
import com.example.exam.service.StudentCourseService;
import com.example.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生选课控制器
 * 提供选课中心和选课操作
 *
 * @author example
 */
@Controller
@RequestMapping("/student/course")
public class StudentCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentCourseService studentCourseService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录学生
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    /**
     * 选课中心 - 显示所有课程及已选状态
     */
    @GetMapping("/available")
    public String available(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        List<Course> allCourses = courseService.listAll();
        List<Long> selectedIds = studentCourseService.listCourseIdsByStudentId(student.getId());

        // 构建课程ID -> 教师姓名映射
        Map<Long, String> teacherNames = new HashMap<>();
        for (Course c : allCourses) {
            User teacher = userService.getById(c.getTeacherId());
            teacherNames.put(c.getId(), teacher != null ? teacher.getName() : "未知");
        }

        model.addAttribute("courses", allCourses);
        model.addAttribute("selectedIds", selectedIds);
        model.addAttribute("teacherNames", teacherNames);
        return "student/course/available";
    }

    /**
     * 选课操作
     */
    @PostMapping("/select")
    public String selectCourse(@RequestParam Long courseId,
                               HttpServletRequest request,
                               RedirectAttributes redirectAttributes) {
        User student = getCurrentUser(request);
        studentCourseService.selectCourse(student.getId(), courseId);
        redirectAttributes.addFlashAttribute("msg", "选课成功");
        return "redirect:/student/course/available";
    }

}
