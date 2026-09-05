package com.example.exam.service;

import com.example.exam.entity.Course;

import java.util.List;

/**
 * 课程Service接口
 * 处理课程CRUD业务
 *
 * @author example
 */
public interface CourseService {

    /**
     * 查询指定教师的课程列表
     */
    List<Course> listByTeacherId(Long teacherId);

    /**
     * 查询所有课程（学生选课时用）
     */
    List<Course> listAll();

    /**
     * 根据ID查询课程
     */
    Course getById(Long id);

    /**
     * 新增课程
     */
    void save(Course course);

    /**
     * 更新课程
     */
    void update(Course course);

    /**
     * 删除课程
     */
    void remove(Long id);

}
