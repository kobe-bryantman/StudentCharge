package com.example.exam.controller.student;

import com.example.exam.controller.LoginController;
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
 * 学生选课/考试控制器
 *
 * @author example
 */
@Controller
@RequestMapping("/student")
public class StudentCourseController {

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
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    // ==================== 选课中心 ====================

    @GetMapping("/course")
    public String courseList(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        List<Course> allCourses = courseService.listAll();
        List<Long> selectedIds = studentCourseService.listCourseIdsByStudentId(student.getId());
        model.addAttribute("courses", allCourses);
        model.addAttribute("selectedIds", selectedIds);
        return "student/course-list";
    }

    @GetMapping("/course/select/{id}")
    public String selectCourse(@PathVariable Long id, HttpServletRequest request) {
        User student = getCurrentUser(request);
        studentCourseService.selectCourse(student.getId(), id);
        return "redirect:/student/course";
    }

    @GetMapping("/course/drop/{id}")
    public String dropCourse(@PathVariable Long id, HttpServletRequest request) {
        User student = getCurrentUser(request);
        studentCourseService.dropCourse(student.getId(), id);
        return "redirect:/student/course";
    }

    // ==================== 我的考试 ====================

    @GetMapping("/exam")
    public String examList(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
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
        List<ExamRecord> records = examService.listByStudentId(student.getId());
        model.addAttribute("courses", selectedCourses);
        model.addAttribute("courseMap", courseMap);
        model.addAttribute("records", records);
        return "student/exam-list";
    }

    @GetMapping("/exam/start/{courseId}")
    public String startExam(@PathVariable Long courseId, HttpServletRequest request) {
        User student = getCurrentUser(request);
        if (!studentCourseService.isSelected(student.getId(), courseId)) {
            return "redirect:/student/exam";
        }
        ExamRecord record = examService.startExam(student.getId(), courseId);
        return "redirect:/student/exam/do/" + record.getId();
    }

    @GetMapping("/exam/do/{recordId}")
    public String doExam(@PathVariable Long recordId, HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        ExamRecord record = examService.getRecordById(recordId);
        if (record == null || !record.getStudentId().equals(student.getId())) {
            return "redirect:/student/exam";
        }
        if ("submitted".equals(record.getStatus())) {
            return "redirect:/student/exam/result/" + recordId;
        }
        List<Question> questions = examService.getExamQuestions(record.getCourseId());
        Course course = courseService.getById(record.getCourseId());
        model.addAttribute("record", record);
        model.addAttribute("questions", questions);
        model.addAttribute("course", course);
        return "student/exam-do";
    }

    @PostMapping("/exam/submit")
    public String submitExam(@RequestParam Long recordId,
                             HttpServletRequest request) {
        User student = getCurrentUser(request);
        Map<String, String[]> allParams = request.getParameterMap();
        Map<Long, String> answers = new HashMap<>();
        for (Map.Entry<String, String[]> entry : allParams.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("q_")) {
                Long questionId = Long.parseLong(key.substring(2));
                String[] values = entry.getValue();
                Arrays.sort(values);
                String answer = String.join("", values);
                answers.put(questionId, answer);
            }
        }
        ExamRecord record = examService.submitExam(recordId, student.getId(), answers);
        return "redirect:/student/exam/result/" + record.getId();
    }

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

        long correctCount = answers.stream().filter(a -> a.getIsCorrect() == 1).count();
        model.addAttribute("record", record);
        model.addAttribute("answers", answers);
        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        model.addAttribute("correctCount", correctCount);
        model.addAttribute("totalCount", questions.size());
        return "student/exam-result";
    }

}
