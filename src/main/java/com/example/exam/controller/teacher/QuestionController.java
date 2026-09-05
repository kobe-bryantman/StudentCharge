package com.example.exam.controller.teacher;

import com.example.exam.common.BusinessException;
import com.example.exam.controller.LoginController;
import com.example.exam.entity.Course;
import com.example.exam.entity.Question;
import com.example.exam.entity.User;
import com.example.exam.service.CourseService;
import com.example.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

/**
 * 教师考题管理控制器
 * 提供考题的增删改查，所有操作验证课程归属当前教师
 * 多选题正确答案通过checkbox提交，后端用String[]接收后排序拼接为"AB"格式
 *
 * @author example
 */
@Controller
@RequestMapping("/teacher/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseService courseService;

    /**
     * 获取当前登录教师
     */
    private User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession();
        return (User) session.getAttribute(LoginController.LOGIN_USER);
    }

    /**
     * GET /teacher/question/list?courseId=xxx 考题列表
     */
    @GetMapping("/list")
    public String list(@RequestParam Long courseId, HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        // 权限验证：课程必须属于当前教师
        if (!courseService.verifyOwnership(courseId, teacher.getId())) {
            throw new BusinessException("无权操作该课程的考题");
        }
        Course course = courseService.getById(courseId);
        List<Question> questions = questionService.listByCourseId(courseId);
        model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        return "teacher/question/list";
    }

    /**
     * GET /teacher/question/add?courseId=xxx 新增考题页面
     */
    @GetMapping("/add")
    public String addPage(@RequestParam Long courseId, HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        // 权限验证
        if (!courseService.verifyOwnership(courseId, teacher.getId())) {
            throw new BusinessException("无权操作该课程的考题");
        }
        Course course = courseService.getById(courseId);
        model.addAttribute("course", course);
        return "teacher/question/add";
    }

    /**
     * POST /teacher/question/add 新增考题提交
     * correctAnswer用String[]接收（单选select和多选checkbox统一处理）
     */
    @PostMapping("/add")
    public String add(@RequestParam Long courseId,
                      @RequestParam String questionText,
                      @RequestParam String questionType,
                      @RequestParam String optionA,
                      @RequestParam String optionB,
                      @RequestParam String optionC,
                      @RequestParam String optionD,
                      @RequestParam(required = false, value = "correctAnswer") String[] correctAnswers,
                      @RequestParam Integer score,
                      HttpServletRequest request,
                      RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(request);
        // 权限验证
        if (!courseService.verifyOwnership(courseId, teacher.getId())) {
            throw new BusinessException("无权操作该课程的考题");
        }
        // 表单校验
        String error = validateQuestion(questionText, optionA, optionB, optionC, optionD, correctAnswers, score);
        if (error != null) {
            redirectAttributes.addFlashAttribute("msg", error);
            redirectAttributes.addFlashAttribute("msgType", "error");
            return "redirect:/teacher/question/add?courseId=" + courseId;
        }
        Question question = new Question();
        question.setCourseId(courseId);
        question.setQuestionText(questionText.trim());
        question.setQuestionType(questionType);
        question.setOptionA(optionA.trim());
        question.setOptionB(optionB.trim());
        question.setOptionC(optionC.trim());
        question.setOptionD(optionD.trim());
        question.setCorrectAnswer(normalizeAnswers(correctAnswers));
        question.setScore(score);
        questionService.save(question);
        redirectAttributes.addFlashAttribute("msg", "新增成功");
        return "redirect:/teacher/question/list?courseId=" + courseId;
    }

    /**
     * GET /teacher/question/edit?id=xxx 编辑考题页面
     */
    @GetMapping("/edit")
    public String editPage(@RequestParam Long id, HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException("考题不存在");
        }
        // 权限验证：通过courseId验证课程归属
        if (!courseService.verifyOwnership(question.getCourseId(), teacher.getId())) {
            throw new BusinessException("无权操作该考题");
        }
        Course course = courseService.getById(question.getCourseId());
        model.addAttribute("question", question);
        model.addAttribute("course", course);
        return "teacher/question/edit";
    }

    /**
     * POST /teacher/question/edit 编辑考题提交
     */
    @PostMapping("/edit")
    public String edit(@RequestParam Long id,
                       @RequestParam Long courseId,
                       @RequestParam String questionText,
                       @RequestParam String questionType,
                       @RequestParam String optionA,
                       @RequestParam String optionB,
                       @RequestParam String optionC,
                       @RequestParam String optionD,
                       @RequestParam(required = false, value = "correctAnswer") String[] correctAnswers,
                       @RequestParam Integer score,
                       HttpServletRequest request,
                       RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(request);
        // 权限验证
        if (!courseService.verifyOwnership(courseId, teacher.getId())) {
            throw new BusinessException("无权操作该考题");
        }
        // 表单校验
        String error = validateQuestion(questionText, optionA, optionB, optionC, optionD, correctAnswers, score);
        if (error != null) {
            redirectAttributes.addFlashAttribute("msg", error);
            redirectAttributes.addFlashAttribute("msgType", "error");
            return "redirect:/teacher/question/edit?id=" + id;
        }
        Question question = questionService.getById(id);
        question.setQuestionText(questionText.trim());
        question.setQuestionType(questionType);
        question.setOptionA(optionA.trim());
        question.setOptionB(optionB.trim());
        question.setOptionC(optionC.trim());
        question.setOptionD(optionD.trim());
        question.setCorrectAnswer(normalizeAnswers(correctAnswers));
        question.setScore(score);
        questionService.update(question);
        redirectAttributes.addFlashAttribute("msg", "修改成功");
        return "redirect:/teacher/question/list?courseId=" + courseId;
    }

    /**
     * POST /teacher/question/delete 删除考题
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Long id,
                         @RequestParam Long courseId,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(request);
        // 权限验证
        if (!courseService.verifyOwnership(courseId, teacher.getId())) {
            throw new BusinessException("无权操作该考题");
        }
        questionService.remove(id);
        redirectAttributes.addFlashAttribute("msg", "删除成功");
        return "redirect:/teacher/question/list?courseId=" + courseId;
    }

    /**
     * 考题表单校验
     */
    private String validateQuestion(String questionText, String optionA, String optionB,
                                    String optionC, String optionD, String[] correctAnswers, Integer score) {
        if (questionText == null || questionText.trim().isEmpty()) {
            return "题干不能为空";
        }
        if (optionA == null || optionA.trim().isEmpty()
                || optionB == null || optionB.trim().isEmpty()
                || optionC == null || optionC.trim().isEmpty()
                || optionD == null || optionD.trim().isEmpty()) {
            return "所有选项不能为空";
        }
        if (correctAnswers == null || correctAnswers.length == 0) {
            return "请选择正确答案";
        }
        if (score == null || score < 1) {
            return "分值必须大于等于1";
        }
        return null;
    }

    /**
     * 将答案数组排序后拼接为无逗号字符串（如AB、ACD）
     */
    private String normalizeAnswers(String[] answers) {
        if (answers == null || answers.length == 0) {
            return "";
        }
        // 去空格、转大写、排序
        return Arrays.stream(answers)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .sorted()
                .reduce("", String::concat)
                .toUpperCase();
    }

}
