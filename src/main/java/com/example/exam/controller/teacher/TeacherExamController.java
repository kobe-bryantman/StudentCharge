package com.example.exam.controller.teacher;

import com.example.exam.common.BusinessException;
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
import java.util.stream.Collectors;

/**
 * 教师端考试情况控制器
 * 提供按课程查看考试情况、全班成绩列表、考生考试详情
 *
 * @author example
 */
@Controller
@RequestMapping("/teacher/exam")
public class TeacherExamController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private ExamService examService;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @Autowired
    private StudentCourseService studentCourseService;

    /**
     * 获取当前登录教师
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    /**
     * 考试情况入口 - 按课程查看
     * 查询当前教师所有课程，每门课统计已考人数
     */
    @GetMapping("/course-list")
    public String courseList(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        List<Course> courses = courseService.listByTeacherId(teacher.getId());

        // 统计每门课已考人数
        Map<Long, Integer> examCountMap = new HashMap<>();
        for (Course c : courses) {
            examCountMap.put(c.getId(), examService.countSubmittedByCourse(c.getId()));
        }

        model.addAttribute("courses", courses);
        model.addAttribute("examCountMap", examCountMap);
        return "teacher/exam/course-list";
    }

    /**
     * 某课程的全班成绩列表
     */
    @GetMapping("/student-list")
    public String studentList(@RequestParam Long courseId, HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);

        // 验证课程属于当前教师
        if (!courseService.verifyOwnership(courseId, teacher.getId())) {
            throw new BusinessException("无权查看该课程的考试情况");
        }

        Course course = courseService.getById(courseId);

        // 查询全班最新成绩
        List<StudentScoreVO> students = examService.listLatestSubmittedByCourse(courseId);

        // 查询统计数据（SQL聚合）
        CourseStatisticsVO statistics = examService.getCourseStatistics(courseId);
        if (statistics == null) {
            statistics = new CourseStatisticsVO();
            statistics.setStudentCount(0);
            statistics.setPassRate(0.0);
        }

        model.addAttribute("course", course);
        model.addAttribute("students", students);
        model.addAttribute("statistics", statistics);
        return "teacher/exam/student-list";
    }

    /**
     * 某考生该课程的考试详情（多次考试全部展开）
     */
    @GetMapping("/student-detail")
    public String studentDetail(@RequestParam Long studentId,
                                @RequestParam Long courseId,
                                HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);

        // 验证课程属于当前教师
        if (!courseService.verifyOwnership(courseId, teacher.getId())) {
            throw new BusinessException("无权查看该课程的考试情况");
        }

        // 验证学生选了该课程
        if (!studentCourseService.isSelected(studentId, courseId)) {
            throw new BusinessException("该学生未选择此课程");
        }

        Course course = courseService.getById(courseId);
        User student = userService.getById(studentId);

        // 查询该学生该课程所有已交卷记录（按exam_count升序）
        List<ExamRecord> records = examService.listSubmittedRecordsByStudentAndCourse(studentId, courseId);

        // 查询课程所有题目
        List<Question> questions = questionService.listByCourseId(courseId);
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // 为每条记录构建答题明细
        List<Map<String, Object>> recordDetails = new ArrayList<>();
        for (ExamRecord record : records) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("record", record);

            List<ExamAnswer> answers = examService.listAnswersByRecordId(record.getId());
            detail.put("answers", answers);

            // 构建题目ID -> 答题明细映射
            Map<Long, ExamAnswer> answerMap = answers.stream()
                    .collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a));
            detail.put("answerMap", answerMap);

            // 计算答对题数
            long correctCount = answers.stream().filter(a -> a.getIsCorrect() == 1).count();
            detail.put("correctCount", correctCount);

            recordDetails.add(detail);
        }

        model.addAttribute("course", course);
        model.addAttribute("student", student);
        model.addAttribute("questions", questions);
        model.addAttribute("recordDetails", recordDetails);
        return "teacher/exam/student-detail";
    }

}
