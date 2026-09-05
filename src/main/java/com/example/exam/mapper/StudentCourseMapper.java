package com.example.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.exam.entity.StudentCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生选课关联Mapper接口
 *
 * @author example
 */
@Mapper
public interface StudentCourseMapper extends BaseMapper<StudentCourse> {

}
