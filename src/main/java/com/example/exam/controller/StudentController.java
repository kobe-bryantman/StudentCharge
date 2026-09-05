package com.example.exam.controller;

import com.example.exam.entity.*;
import com.example.exam.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * 学生端控制器
 * 处理学生端所有页面和业务：首页、选课中心、我的考试、在线答题、成绩查看、个人信息
 *
 * @author example
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentCourseService studentCourseService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private ExamService examService;

    @Autowired
    private UserService userService;

    /**
     * 获取当前登录学生
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute("user");
    }

    /**
     * 学生首页
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

    // ==================== 选课中心 ====================

    /**
     * 选课中心 - 课程列表
     */
    @GetMapping("/course")
    public String courseList(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        List<Course> allCourses = courseService.listAll();
        List<Long> selectedIds = studentCourseService.listCourseIdsByStudentId(student.getId());
        model.addAttribute("courses", allCourses);
        model.addAttribute("selectedIds", selectedIds);
        return "student/course-list";
    }

    /**
     * 选课
     */
    @GetMapping("/course/select/{id}")
    public String selectCourse(@PathVariable Long id, HttpServletRequest request) {
        User student = getCurrentUser(request);
        studentCourseService.selectCourse(student.getId(), id);
        return "redirect:/student/course";
    }

    /**
     * 退选
     */
    @GetMapping("/course/drop/{id}")
    public String dropCourse(@PathVariable Long id, HttpServletRequest request) {
        User student = getCurrentUser(request);
        studentCourseService.dropCourse(student.getId(), id);
        return "redirect:/student/course";
    }

    // ==================== 我的考试 ====================

    /**
     * 我的考试 - 考试记录列表
     */
    @GetMapping("/exam")
    public String examList(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        // 已选课程
        List<Long> courseIds = studentCourseService.listCourseIdsByStudentId(student.getId());
        List<Course> selectedCourses = new ArrayList<>();
        Map<Long, Course> courseMap = new HashMap<>();
        for (Long id : courseIds) {
            Course c = courseService.getById(id);
            if (c != null) {
                selectedCourses.add(c);
                courseMap.put(id, c);
            }
        }
        // 考试记录
        List<ExamRecord> records = examService.listByStudentId(student.getId());
        model.addAttribute("courses", selectedCourses);
        model.addAttribute("courseMap", courseMap);
        model.addAttribute("records", records);
        return "student/exam-list";
    }

    /**
     * 开始考试 - 创建考试记录并跳转到答题页
     */
    @GetMapping("/exam/start/{courseId}")
    public String startExam(@PathVariable Long courseId, HttpServletRequest request) {
        User student = getCurrentUser(request);
        // 检查是否已选该课程
        if (!studentCourseService.isSelected(student.getId(), courseId)) {
            return "redirect:/student/exam";
        }
        // 创建考试记录
        ExamRecord record = examService.startExam(student.getId(), courseId);
        return "redirect:/student/exam/do/" + record.getId();
    }

    /**
     * 在线答题页面
     */
    @GetMapping("/exam/do/{recordId}")
    public String doExam(@PathVariable Long recordId, HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        ExamRecord record = examService.getRecordById(recordId);
        // 校验权限
        if (record == null || !record.getStudentId().equals(student.getId())) {
            return "redirect:/student/exam";
        }
        // 已交卷则跳转成绩页
        if ("submitted".equals(record.getStatus())) {
            return "redirect:/student/exam/result/" + recordId;
        }
        // 获取题目
        List<Question> questions = examService.getExamQuestions(record.getCourseId());
        Course course = courseService.getById(record.getCourseId());
        model.addAttribute("record", record);
        model.addAttribute("questions", questions);
        model.addAttribute("course", course);
        return "student/exam-do";
    }

    /**
     * 交卷阅卷
     * 接收表单提交的所有答案，name格式为 q_{questionId}
     */
    @PostMapping("/exam/submit")
    public String submitExam(@RequestParam Long recordId,
                             HttpServletRequest request) {
        User student = getCurrentUser(request);
        // 使用 getParameterMap 获取所有参数，兼容单选(单值)和多选(多值)
        Map<String, String[]> allParams = request.getParameterMap();
        // 解析答案：key=questionId, value=答案字符串
        Map<Long, String> answers = new HashMap<>();
        for (Map.Entry<String, String[]> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("q_")) {
                Long questionId = Long.parseLong(key.substring(2));
                String[] values = entry.getValue();
                // 多选题多个值拼接（排序）
                Arrays.sort(values);
                String answer = String.join("", values);
                answers.put(questionId, answer);
            }
        }
        // 调用阅卷
        ExamRecord record = examService.submitExam(recordId, student.getId(), answers);
        return "redirect:/student/exam/result/" + record.getId();
    }

    /**
     * 成绩查看
     */
    @GetMapping("/exam/result/{recordId}")
    public String examResult(@PathVariable Long recordId, HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        ExamRecord record = examService.getRecordById(recordId);
        if (record == null || !record.getStudentId().equals(student.getId())) {
            return "redirect:/student/exam";
        }
        List<ExamAnswer> answers = examService.listAnswersByRecordId(recordId);
        Course course = courseService.getById(record.getCourseId());
        List<Question> questions = questionService.listByCourseId(record.getCourseId());

        // 计算正确率
        long correctCount = answers.stream().filter(a -> a.getIsCorrect() == 1).count();
        model.addAttribute("record", record);
        model.addAttribute("answers", answers);
        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalCount", questions.size());
        return "student/exam-result";
    }

    // ==================== 个人信息 ====================

    /**
     * 个人信息页面
     */
    @GetMapping("/profile")
    public String profile(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        model.addAttribute("user", student);
        return "student/profile";
    }

    /**
     * 修改个人信息提交
     */
    @PostMapping("/profile")
    public String profileUpdate(User user, HttpServletRequest request) {
        User current = getCurrentUser(request);
        current.setName(user.getName());
        current.setGender(user.getGender());
        current.setPhone(user.getPhone());
        current.setCollege(user.getCollege());
        userService.update(current);
        request.getSession().setAttribute("user", current);
        return "redirect:/student/profile";
    }

}
