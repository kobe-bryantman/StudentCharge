package com.example.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.exam.entity.Course;
import com.example.exam.mapper.CourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程Service
 * 处理课程CRUD业务
 *
 * @author example
 */
@Service
public class CourseService {

    @Autowired
    private CourseMapper courseMapper;

    /**
     * 分页查询所有课程（教师端）
     */
    public Page<Course> page(Integer current, Integer size) {
        return courseMapper.selectPage(new Page<>(current, size),
                new LambdaQueryWrapper<Course>().orderByDesc(Course::getCreateTime));
    }

    /**
     * 查询指定教师的课程列表
     */
    public List<Course> listByTeacherId(Long teacherId) {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getTeacherId, teacherId)
                        .orderByDesc(Course::getCreateTime));
    }

    /**
     * 查询所有课程（学生选课时用）
     */
    public List<Course> listAll() {
        return courseMapper.selectList(
                new LambdaQueryWrapper<Course>().orderByDesc(Course::getCreateTime));
    }

    /**
     * 根据ID查询课程
     */
    public Course getById(Long id) {
        return courseMapper.selectById(id);
    }

    /**
     * 新增课程
     */
    public void save(Course course) {
        courseMapper.insert(course);
    }

    /**
     * 更新课程
     */
    public void update(Course course) {
        courseMapper.updateById(course);
    }

    /**
     * 删除课程
     */
    public void remove(Long id) {
        courseMapper.deleteById(id);
    }

}
