package com.example.exam.controller.student;

import com.example.exam.controller.LoginController;
import com.example.exam.entity.Course;
import com.example.exam.entity.ExamRecord;
import com.example.exam.entity.User;
import com.example.exam.service.CourseService;
import com.example.exam.service.ExamService;
import com.example.exam.service.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * 学生首页控制器
 *
 * @author example
 */
@Controller
@RequestMapping("/student")
public class StudentIndexController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentCourseService studentCourseService;

    @Autowired
    private ExamService examService;

    /**
     * 获取当前登录学生
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    /**
     * GET /student/index 学生首页
     */
    @GetMapping("/index")
    public String index(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        // 已选课程
        List<Long> courseIds = studentCourseService.listCourseIdsByStudentId(student.getId());
        List<Course> selectedCourses = new ArrayList<>();
        for (Long id : courseIds) {
            Course c = courseService.getById(id);
            if (c != null) {
                selectedCourses.add(c);
            }
        }
        // 考试记录
        List<ExamRecord> records = examService.listByStudentId(student.getId());
        long submittedCount = records.stream().filter(r -> "submitted".equals(r.getStatus())).count();

        model.addAttribute("selectedCourses", selectedCourses);
        model.addAttribute("recordCount", records.size());
        model.addAttribute("submittedCount", submittedCount);
        return "student/index";
    }

}
