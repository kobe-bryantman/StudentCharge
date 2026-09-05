package com.example.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.entity.StudentCourse;
import com.example.exam.mapper.StudentCourseMapper;
import com.example.exam.service.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生选课Service实现类
 *
 * @author example
 */
@Service
public class StudentCourseServiceImpl implements StudentCourseService {

    @Autowired
    private StudentCourseMapper studentCourseMapper;

    @Override
    public List<Long> listCourseIdsByStudentId(Long studentId) {
        List<StudentCourse> list = studentCourseMapper.selectList(
                new LambdaQueryWrapper<StudentCourse>().eq(StudentCourse::getStudentId, studentId));
        return list.stream().map(StudentCourse::getCourseId).collect(Collectors.toList());
    }

    @Override
    public List<StudentCourse> listByStudentId(Long studentId) {
        return studentCourseMapper.selectList(
                new LambdaQueryWrapper<StudentCourse>().eq(StudentCourse::getStudentId, studentId));
    }

    @Override
    public void selectCourse(Long studentId, Long courseId) {
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

    @Override
    public void dropCourse(Long studentId, Long courseId) {
        studentCourseMapper.delete(
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(StudentCourse::getStudentId, studentId)
                        .eq(StudentCourse::getCourseId, courseId));
    }

    @Override
    public boolean isSelected(Long studentId, Long courseId) {
        Long count = studentCourseMapper.selectCount(
                new LambdaQueryWrapper<StudentCourse>()
                        .eq(StudentCourse::getStudentId, studentId)
                        .eq(StudentCourse::getCourseId, courseId));
        return count != null && count > 0;
    }

}
