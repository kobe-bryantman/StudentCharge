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
 * 学生考试控制器
 * 提供考试列表、开始考试、交卷、查看成绩
 *
 * @author example
 */
@Controller
@RequestMapping("/student/exam")
public class ExamController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private StudentCourseService studentCourseService;

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

    /**
     * 我的考试 - 已选课程列表及考试状态
     */
    @GetMapping("/list")
    public String examList(HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);

        // 查询已选课程ID
        List<Long> courseIds = studentCourseService.listCourseIdsByStudentId(student.getId());
        List<Course> selectedCourses = new ArrayList<>();
        Map<Long, String> teacherNames = new HashMap<>();
        Map<Long, ExamRecord> latestRecords = new HashMap<>();

        for (Long courseId : courseIds) {
            Course c = courseService.getById(courseId);
            if (c != null) {
                selectedCourses.add(c);
                User teacher = userService.getById(c.getTeacherId());
                teacherNames.put(courseId, teacher != null ? teacher.getName() : "未知");

                // 查询该课程最新考试记录
                ExamRecord latest = examService.getLatestRecord(student.getId(), courseId);
                latestRecords.put(courseId, latest);
            }
        }

        model.addAttribute("courses", selectedCourses);
        model.addAttribute("teacherNames", teacherNames);
        model.addAttribute("latestRecords", latestRecords);
        return "student/exam/list";
    }

    /**
     * 开始/继续考试
     */
    @GetMapping("/start")
    public String startExam(@RequestParam Long courseId, HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);

        // 验证已选该课程
        if (!studentCourseService.isSelected(student.getId(), courseId)) {
            return "redirect:/student/exam/list";
        }

        // 获取或创建ongoing考试记录
        ExamRecord record = examService.startExam(student.getId(), courseId);

        // 如果已提交，重定向到成绩页
        if ("submitted".equals(record.getStatus())) {
            return "redirect:/student/exam/result/" + record.getId();
        }

        Course course = courseService.getById(courseId);
        List<Question> questions = examService.getExamQuestions(courseId);

        // 计算总分
        int totalScore = questions.stream().mapToInt(Question::getScore).sum();

        model.addAttribute("record", record);
        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        model.addAttribute("totalScore", totalScore);
        return "student/exam/start";
    }

    /**
     * 交卷并自动阅卷
     */
    @PostMapping("/submit")
    public String submitExam(@RequestParam Long examRecordId,
                             HttpServletRequest request) {
        User student = getCurrentUser(request);
        Map<String, String[]> allParams = request.getParameterMap();
        Map<Long, String> answers = new HashMap<>();

        for (Map.Entry<String, String[]> entry : allParams.entrySet()) {
            String key = entry.getKey();
            // 答案参数前缀 answer_题目ID
            if (key.startsWith("answer_")) {
                Long questionId = Long.parseLong(key.substring(7));
                String[] values = entry.getValue();
                Arrays.sort(values);
                String answer = String.join("", values);
                answers.put(questionId, answer);
            }
        }

        ExamRecord record = examService.submitExam(examRecordId, student.getId(), answers);
        return "redirect:/student/exam/result/" + record.getId();
    }

    /**
     * 查看考试成绩
     */
    @GetMapping("/result/{recordId}")
    public String examResult(@PathVariable Long recordId, HttpServletRequest request, Model model) {
        User student = getCurrentUser(request);
        ExamRecord record = examService.getRecordById(recordId);

        // 验证记录归属
        if (record == null || !record.getStudentId().equals(student.getId())) {
            return "redirect:/student/exam/list";
        }

        List<ExamAnswer> answers = examService.listAnswersByRecordId(recordId);
        Course course = courseService.getById(record.getCourseId());
        List<Question> questions = course != null ? examService.getExamQuestions(course.getId()) : new ArrayList<>();

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
