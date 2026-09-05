package com.example.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.entity.StudentCourse;
import com.example.exam.mapper.StudentCourseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生选课Service
 *
 * @author example
 */
@Service
public class StudentCourseService {

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    /**
     * 查询学生已选的课程ID列表
     */
    public List<Long> listCourseIdsByStudentId(Long studentId) {
        List<StudentCourse> list = studentCourseMapper.selectList(
                new LambdaQueryWrapper<StudentCourse>().eq(StudentCourse::getStudentId, studentId));
        return list.stream().map(StudentCourse::getCourseId).collect(java.util.stream.Collectors.toList());
    }

    /**
     * 查询学生已选的选课记录列表
     */
    public List<StudentCourse> listByStudentId(Long studentId) {
        return studentCourseMapper.selectList(
                new LambdaQueryWrapper<StudentCourse>().eq(StudentCourse::getStudentId, studentId));
    }

    /**
     * 选课
     */
    public void selectCourse(Long studentId, Long courseId) {
        // 检查是否已选
        StudentCourse exist = studentCourseMapper.selectOne(
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(StudentCourse::getStudentId, studentId)
                        .eq(StudentCourse::getCourseId, courseId));
        if (exist == null) {
            StudentCourse sc = new StudentCourse();
            sc.setStudentId(studentId);
            sc.setCourseId(courseId);
            studentCourseMapper.insert(sc);
        }
    }

    /**
     * 退选
     */
    public void dropCourse(Long studentId, Long courseId) {
        studentCourseMapper.delete(
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(StudentCourse::getStudentId, studentId)
                        .eq(StudentCourse::getCourseId, courseId));
    }

    /**
     * 检查学生是否已选某课程
     */
    public boolean isSelected(Long studentId, Long courseId) {
        Long count = studentCourseMapper.selectCount(
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(StudentCourse::getStudentId, studentId)
                        .eq(StudentCourse::getCourseId, courseId));
        return count != null && count > 0;
    }

}
