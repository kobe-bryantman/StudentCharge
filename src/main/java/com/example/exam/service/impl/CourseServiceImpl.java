package com.example.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.common.BusinessException;
import com.example.exam.entity.Course;
import com.example.exam.mapper.CourseMapper;
import com.example.exam.service.CourseService;
import com.example.exam.service.ExamService;
import com.example.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 课程Service实现类
 *
 * @author example
 */
@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 延迟注入QuestionService，避免潜在循环依赖
     */
    @Autowired
    @Lazy
    private QuestionService questionService;

    /**
     * 延迟注入ExamService，避免潜在循环依赖
     */
    @Autowired
    @Lazy
    private ExamService examService;

    @Override
    public List<Course> listByTeacherId(Long teacherId) {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, teacherId)
                        .orderByDesc(Course::getCreateTime));
    }

    @Override
    public List<Course> listAll() {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>().orderByDesc(Course::getCreateTime));
    }

    @Override
    public Course getById(Long id) {
        return courseMapper.selectById(id);
    }

    @Override
    public void save(Course course) {
        courseMapper.insert(course);
    }

    @Override
    public void update(Course course) {
        courseMapper.updateById(course);
    }

    @Override
    public void remove(Long id) {
        courseMapper.deleteById(id);
    }

    /**
     * 删除课程及其下所有考题（事务保证一致性）
     * 保护历史数据：有考试记录的课程禁止删除
     */
    @Override
    @Transactional
    public void removeWithQuestions(Long id) {
        // 保护历史数据：有考试记录的课程禁止删除
        if (examService.hasExamRecords(id)) {
            throw new BusinessException("该课程已有考试记录，无法删除");
        }
        // 先删除该课程下所有考题
        questionService.removeByCourseId(id);
        // 再删除课程
        courseMapper.deleteById(id);
    }

    @Override
    public boolean verifyOwnership(Long courseId, Long teacherId) {
        if (courseId == null || teacherId == null) {
            return false;
        }
        Course course = courseMapper.selectById(courseId);
        return course != null && teacherId.equals(course.getTeacherId());
    }

}
