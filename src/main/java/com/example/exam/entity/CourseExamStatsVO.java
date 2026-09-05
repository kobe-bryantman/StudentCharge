package com.example.exam.entity;

import lombok.Data;

/**
 * 课程考试统计VO
 * 用于教师端课程列表展示已考人数
 *
 * @author example
 */
@Data
public class CourseExamStatsVO {

    /** 课程ID */
    private Long courseId;

    /** 课程名称 */
    private String courseName;

    /** 已考人数（去重学生数） */
    private Integer examCount;

}
