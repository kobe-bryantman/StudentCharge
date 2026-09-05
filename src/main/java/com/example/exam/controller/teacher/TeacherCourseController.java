package com.example.exam.controller.teacher;

import com.example.exam.controller.LoginController;
import com.example.exam.entity.*;
import com.example.exam.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师课程/题库/考试管理控制器
 *
 * @author example
 */
@Controller
@RequestMapping("/teacher")
public class TeacherCourseController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录教师
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    // ==================== 课程管理 ====================

    @GetMapping("/course")
    public String courseList(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        List<Course> courses = courseService.listByTeacherId(teacher.getId());
        model.addAttribute("courses", courses);
        return "teacher/course-list";
    }

    @GetMapping("/course/add")
    public String courseAddPage() {
        return "teacher/course-form";
    }

    @PostMapping("/course/add")
    public String courseAdd(Course course, HttpServletRequest request) {
        User teacher = getCurrentUser(request);
        course.setTeacherId(teacher.getId());
        courseService.save(course);
        return "redirect:/teacher/course";
    }

    @GetMapping("/course/edit/{id}")
    public String courseEditPage(@PathVariable Long id, Model model) {
        Course course = courseService.getById(id);
        model.addAttribute("course", course);
        return "teacher/course-form";
    }

    @PostMapping("/course/edit")
    public String courseEdit(Course course) {
        courseService.update(course);
        return "redirect:/teacher/course";
    }

    @GetMapping("/course/delete/{id}")
    public String courseDelete(@PathVariable Long id) {
        courseService.remove(id);
        return "redirect:/teacher/course";
    }

    // ==================== 题库管理 ====================

    @GetMapping("/question")
    public String questionList(@RequestParam Long courseId, Model model) {
        Course course = courseService.getById(courseId);
        List<Question> questions = questionService.listByCourseId(courseId);
        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        return "teacher/question-list";
    }

    @GetMapping("/question/add")
    public String questionAddPage(@RequestParam Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "teacher/question-form";
    }

    @PostMapping("/question/add")
    public String questionAdd(Question question) {
        questionService.save(question);
        return "redirect:/teacher/question?courseId=" + question.getCourseId();
    }

    @GetMapping("/question/edit/{id}")
    public String questionEditPage(@PathVariable Long id, Model model) {
        Question question = questionService.getById(id);
        model.addAttribute("question", question);
        return "teacher/question-form";
    }

    @PostMapping("/question/edit")
    public String questionEdit(Question question) {
        questionService.update(question);
        return "redirect:/teacher/question?courseId=" + question.getCourseId();
    }

    @GetMapping("/question/delete/{id}")
    public String questionDelete(@PathVariable Long id, @RequestParam Long courseId) {
        questionService.remove(id);
        return "redirect:/teacher/question?courseId=" + courseId;
    }

    // ==================== 考试情况 ====================

    @GetMapping("/exam")
    public String examList(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        List<Course> courses = courseService.listByTeacherId(teacher.getId());
        List<ExamRecord> allRecords = new ArrayList<>();
        Map<Long, Course> courseMap = new HashMap<>();
        Map<Long, User> studentMap = new HashMap<>();
        for (Course c : courses) {
            courseMap.put(c.getId(), c);
            List<ExamRecord> records = examService.listByCourseId(c.getId());
            for (ExamRecord r : records) {
                if (!studentMap.containsKey(r.getStudentId())) {
                    studentMap.put(r.getStudentId(), userService.getById(r.getStudentId()));
                }
            }
            allRecords.addAll(records);
        }
        model.addAttribute("records", allRecords);
        model.addAttribute("courses", courses);
        model.addAttribute("courseMap", courseMap);
        model.addAttribute("studentMap", studentMap);
        return "teacher/exam-list";
    }

    @GetMapping("/exam/detail/{id}")
    public String examDetail(@PathVariable Long id, Model model) {
        ExamRecord record = examService.getRecordById(id);
        List<ExamAnswer> answers = examService.listAnswersByRecordId(id);
        Course course = courseService.getById(record.getCourseId());
        User student = userService.getById(record.getStudentId());
        model.addAttribute("record", record);
        model.addAttribute("answers", answers);
        model.addAttribute("course", course);
        model.addAttribute("student", student);
        List<Question> questions = questionService.listByCourseId(record.getCourseId());
        model.addAttribute("questions", questions);
        return "teacher/exam-detail";
    }

}
