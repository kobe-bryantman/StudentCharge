package com.example.exam.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生成绩VO
 * 用于教师端某课程全班成绩列表展示
 *
 * @author example
 */
@Data
public class StudentScoreVO {

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    private String name;

    /** 学号 */
    private String studentNo;

    /** 学院 */
    private String college;

    /** 总得分 */
    private Integer totalScore;

    /** 考试时间 */
    private LocalDateTime examTime;

    /** 第几次考试 */
    private Integer examCount;

    /** 考试记录ID */
    private Long examRecordId;

}
