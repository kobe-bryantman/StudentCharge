package com.example.exam.controller.teacher;

import com.example.exam.common.BusinessException;
import com.example.exam.controller.LoginController;
import com.example.exam.entity.Course;
import com.example.exam.entity.User;
import com.example.exam.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 教师课程管理控制器
 * 提供课程的增删改查，所有操作验证课程归属当前教师
 *
 * @author example
 */
@Controller
@RequestMapping("/teacher/course")
public class CourseController {

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
     * GET /teacher/course/list 课程列表
     */
    @GetMapping("/list")
    public String list(HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        List<Course> courses = courseService.listByTeacherId(teacher.getId());
        model.addAttribute("courses", courses);
        return "teacher/course/list";
    }

    /**
     * GET /teacher/course/add 新增课程页面
     */
    @GetMapping("/add")
    public String addPage() {
        return "teacher/course/add";
    }

    /**
     * POST /teacher/course/add 新增课程提交
     */
    @PostMapping("/add")
    public String add(@RequestParam String courseName,
                      @RequestParam(required = false) String description,
                      HttpServletRequest request,
                      RedirectAttributes redirectAttributes) {
        // 课程名称非空校验
        if (courseName == null || courseName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "课程名称不能为空");
            redirectAttributes.addFlashAttribute("msgType", "error");
            return "redirect:/teacher/course/add";
        }
        User teacher = getCurrentUser(request);
        Course course = new Course();
        course.setCourseName(courseName.trim());
        course.setDescription(description);
        course.setTeacherId(teacher.getId());
        courseService.save(course);
        redirectAttributes.addFlashAttribute("msg", "新增成功");
        return "redirect:/teacher/course/list";
    }

    /**
     * GET /teacher/course/edit?id=xxx 编辑课程页面
     */
    @GetMapping("/edit")
    public String editPage(@RequestParam Long id, HttpServletRequest request, Model model) {
        User teacher = getCurrentUser(request);
        // 权限验证：课程必须属于当前教师
        if (!courseService.verifyOwnership(id, teacher.getId())) {
            throw new BusinessException("无权操作该课程");
        }
        Course course = courseService.getById(id);
        model.addAttribute("course", course);
        return "teacher/course/edit";
    }

    /**
     * POST /teacher/course/edit 编辑课程提交
     */
    @PostMapping("/edit")
    public String edit(@RequestParam Long id,
                       @RequestParam String courseName,
                       @RequestParam(required = false) String description,
                       HttpServletRequest request,
                       RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(request);
        // 权限验证
        if (!courseService.verifyOwnership(id, teacher.getId())) {
            throw new BusinessException("无权操作该课程");
        }
        // 课程名称非空校验
        if (courseName == null || courseName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "课程名称不能为空");
            redirectAttributes.addFlashAttribute("msgType", "error");
            return "redirect:/teacher/course/edit?id=" + id;
        }
        Course course = courseService.getById(id);
        course.setCourseName(courseName.trim());
        course.setDescription(description);
        courseService.update(course);
        redirectAttributes.addFlashAttribute("msg", "修改成功");
        return "redirect:/teacher/course/list";
    }

    /**
     * POST /teacher/course/delete 删除课程（级联删除考题）
     */
    @PostMapping("/delete")
    public String delete(@RequestParam Long id,
                         HttpServletRequest request,
                         RedirectAttributes redirectAttributes) {
        User teacher = getCurrentUser(request);
        // 权限验证
        if (!courseService.verifyOwnership(id, teacher.getId())) {
            throw new BusinessException("无权操作该课程");
        }
        // 事务删除课程及其下所有考题
        courseService.removeWithQuestions(id);
        redirectAttributes.addFlashAttribute("msg", "删除成功");
        return "redirect:/teacher/course/list";
    }

}
