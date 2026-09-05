package com.example.exam.entity;

import lombok.Data;

/**
 * 课程成绩统计VO
 * 用于教师端某课程成绩列表顶部统计卡片
 *
 * @author example
 */
@Data
public class CourseStatisticsVO {

    /** 平均分 */
    private Double avgScore;

    /** 最高分 */
    private Integer maxScore;

    /** 及格率（百分比，如85.5表示85.5%） */
    private Double passRate;

    /** 参考人数 */
    private Integer studentCount;

}
