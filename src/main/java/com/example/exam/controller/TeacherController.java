package com.example.exam.controller;

import com.example.exam.entity.*;
import com.example.exam.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 教师端控制器
 * 处理教师端所有页面和业务：首页、课程管理、题库管理、考试情况、个人信息
 *
 * @author example
 */
@Controller
@RequestMapping("/teacher")
public class TeacherController {

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
        return (User) session.getAttribute("user");
    }

    /**
     * 教师首页
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

    // ==================== 课程管理 ====================

    /**
     * 课程列表
     */
    @GetMapping("/course")
    public String courseList(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        List<Course> courses = courseService.listByTeacherId(teacher.getId());
        model.addAttribute("courses", courses);
        return "teacher/course-list";
    }

    /**
     * 新增课程页面
     */
    @GetMapping("/course/add")
    public String courseAddPage() {
        return "teacher/course-form";
    }

    /**
     * 新增课程提交
     */
    @PostMapping("/course/add")
    public String courseAdd(Course course, HttpServletRequest request) {
        User teacher = getCurrentUser(request);
        course.setTeacherId(teacher.getId());
        courseService.save(course);
        return "redirect:/teacher/course";
    }

    /**
     * 编辑课程页面
     */
    @GetMapping("/course/edit/{id}")
    public String courseEditPage(@PathVariable Long id, Model model) {
        Course course = courseService.getById(id);
        model.addAttribute("course", course);
        return "teacher/course-form";
    }

    /**
     * 编辑课程提交
     */
    @PostMapping("/course/edit")
    public String courseEdit(Course course) {
        courseService.update(course);
        return "redirect:/teacher/course";
    }

    /**
     * 删除课程
     */
    @GetMapping("/course/delete/{id}")
    public String courseDelete(@PathVariable Long id) {
        courseService.remove(id);
        return "redirect:/teacher/course";
    }

    // ==================== 题库管理 ====================

    /**
     * 题目列表
     */
    @GetMapping("/question")
    public String questionList(@RequestParam Long courseId, Model model) {
        Course course = courseService.getById(courseId);
        List<Question> questions = questionService.listByCourseId(courseId);
        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        return "teacher/question-list";
    }

    /**
     * 新增题目页面
     */
    @GetMapping("/question/add")
    public String questionAddPage(@RequestParam Long courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "teacher/question-form";
    }

    /**
     * 新增题目提交
     */
    @PostMapping("/question/add")
    public String questionAdd(Question question) {
        questionService.save(question);
        return "redirect:/teacher/question?courseId=" + question.getCourseId();
    }

    /**
     * 编辑题目页面
     */
    @GetMapping("/question/edit/{id}")
    public String questionEditPage(@PathVariable Long id, Model model) {
        Question question = questionService.getById(id);
        model.addAttribute("question", question);
        return "teacher/question-form";
    }

    /**
     * 编辑题目提交
     */
    @PostMapping("/question/edit")
    public String questionEdit(Question question) {
        questionService.update(question);
        return "redirect:/teacher/question?courseId=" + question.getCourseId();
    }

    /**
     * 删除题目
     */
    @GetMapping("/question/delete/{id}")
    public String questionDelete(@PathVariable Long id, @RequestParam Long courseId) {
        questionService.remove(id);
        return "redirect:/teacher/question?courseId=" + courseId;
    }

    // ==================== 考试情况 ====================

    /**
     * 考试记录列表
     */
    @GetMapping("/exam")
    public String examList(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        List<Course> courses = courseService.listByTeacherId(teacher.getId());
        // 汇总所有课程的考试记录，并构建课程和学生映射供模板显示名称
        java.util.List<ExamRecord> allRecords = new java.util.ArrayList<>();
        java.util.Map<Long, Course> courseMap = new java.util.HashMap<>();
        java.util.Map<Long, User> studentMap = new java.util.HashMap<>();
        for (Course c : courses) {
            courseMap.put(c.getId(), c);
            List<ExamRecord> records = examService.listByCourseId(c.getId());
            for (ExamRecord r : records) {
                // 缓存学生信息，避免重复查询
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

    /**
     * 考试详情（查看答题明细）
     */
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
        // 查询每道题的详情
        List<Question> questions = questionService.listByCourseId(record.getCourseId());
        model.addAttribute("questions", questions);
        return "teacher/exam-detail";
    }

    // ==================== 个人信息 ====================

    /**
     * 个人信息页面
     */
    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        model.addAttribute("user", teacher);
        return "teacher/profile";
    }

    /**
     * 修改个人信息提交
     */
    @PostMapping("/profile")
    public String profileUpdate(User user, HttpServletRequest request) {
        User current = getCurrentUser(request);
        // 只更新允许修改的字段
        current.setName(user.getName());
        current.setGender(user.getGender());
        current.setPhone(user.getPhone());
        current.setCollege(user.getCollege());
        userService.update(current);
        // 更新session
        request.getSession().setAttribute("user", current);
        return "redirect:/teacher/profile";
    }

}
