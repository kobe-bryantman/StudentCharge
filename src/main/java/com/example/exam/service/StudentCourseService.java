package com.example.exam.service;

import com.example.exam.entity.StudentCourse;

import java.util.List;

/**
 * 学生选课Service接口
 *
 * @author example
 */
public interface StudentCourseService {

    /**
     * 查询学生已选的课程ID列表
     */
    List<Long> listCourseIdsByStudentId(Long studentId);

    /**
     * 查询学生已选的选课记录列表
     */
    List<StudentCourse> listByStudentId(Long studentId);

    /**
     * 选课
     */
    void selectCourse(Long studentId, Long courseId);

    /**
     * 退选
     */
    void dropCourse(Long studentId, Long courseId);

    /**
     * 检查学生是否已选某课程
     */
    boolean isSelected(Long studentId, Long courseId);

}
