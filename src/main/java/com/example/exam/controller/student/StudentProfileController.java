package com.example.exam.controller.student;

import com.example.exam.controller.LoginController;
import com.example.exam.entity.Course;
import com.example.exam.entity.StudentCourse;
import com.example.exam.entity.User;
import com.example.exam.service.CourseService;
import com.example.exam.service.StudentCourseService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 学生个人信息控制器
 * 显示学生信息 + 所选课程列表（含授课教师名），仅可修改姓名/性别/手机号/学院
 *
 * @author example
 */
@Controller
@RequestMapping("/student")
public class StudentProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentCourseService studentCourseService;

    /**
     * 获取当前登录学生
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    /**
     * GET /student/profile 个人信息页面
     * 显示学生信息 + 所选课程列表（课程名、授课教师）
     */
    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        // 查询所选课程列表
        List<StudentCourse> scList = studentCourseService.listByStudentId(student.getId());
        List<Course> courses = new ArrayList<>();
        Map<Long, String> teacherNameMap = new HashMap<>();
        for (StudentCourse sc : scList) {
            Course c = courseService.getById(sc.getCourseId());
            if (c != null) {
                courses.add(c);
                // 查询授课教师姓名
                if (!teacherNameMap.containsKey(c.getTeacherId())) {
                    User teacher = userService.getById(c.getTeacherId());
                    teacherNameMap.put(c.getTeacherId(), teacher != null ? teacher.getName() : "未知");
                }
            }
        }
        model.addAttribute("user", student);
        model.addAttribute("courses", courses);
        model.addAttribute("teacherNameMap", teacherNameMap);
        return "student/profile";
    }

    /**
     * POST /student/profile/update 修改个人信息
     * 学号不可修改，所选课程不可在此修改
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
        current.setName(name);
        current.setGender(gender);
        current.setPhone(phone);
        current.setCollege(college);
        userService.update(current);
        // 更新session
        request.getSession().setAttribute(LoginController.LOGIN_USER, current);
        // 通过 flash attribute 传递成功提示，重定向回 profile 页
        redirectAttributes.addFlashAttribute("msg", "保存成功");
        return "redirect:/student/profile";
    }

}
