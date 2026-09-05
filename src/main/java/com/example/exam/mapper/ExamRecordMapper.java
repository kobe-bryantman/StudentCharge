package com.example.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.exam.entity.CourseStatisticsVO;
import com.example.exam.entity.ExamRecord;
import com.example.exam.entity.StudentScoreVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 考试记录Mapper接口
 *
 * @author example
 */
@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    /**
     * 统计某课程已考人数（status=submitted，按student_id去重）
     */
    @Select("SELECT COUNT(DISTINCT student_id) FROM exam_record WHERE course_id = #{courseId} AND status = 'submitted'")
    int countSubmittedByCourse(Long courseId);

    /**
     * 查询某课程所有已交卷学生的最新成绩
     * 每个学生取exam_count最大的submitted记录，关联user查学生姓名、学号、学院
     * 按成绩降序排列
     */
    @Select("SELECT er.id AS examRecordId, er.student_id AS studentId, " +
            "u.name AS name, u.student_no AS studentNo, u.college AS college, " +
            "er.total_score AS totalScore, er.exam_time AS examTime, er.exam_count AS examCount " +
            "FROM exam_record er " +
            "INNER JOIN user u ON er.student_id = u.id " +
            "WHERE er.course_id = #{courseId} AND er.status = 'submitted' " +
            "AND er.exam_count = (" +
            "  SELECT MAX(er2.exam_count) FROM exam_record er2 " +
            "  WHERE er2.student_id = er.student_id AND er2.course_id = er.course_id AND er2.status = 'submitted'" +
            ") " +
            "ORDER BY er.total_score DESC")
    List<StudentScoreVO> listLatestSubmittedByCourse(Long courseId);

    /**
     * 课程成绩统计：平均分、最高分、及格率、参考人数
     * 基于每个学生最新的submitted记录统计
     */
    @Select("SELECT AVG(t.total_score) AS avgScore, MAX(t.total_score) AS maxScore, " +
            "ROUND(SUM(CASE WHEN t.total_score >= 60 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS passRate, " +
            "COUNT(*) AS studentCount " +
            "FROM (" +
            "  SELECT er.total_score FROM exam_record er " +
            "  WHERE er.course_id = #{courseId} AND er.status = 'submitted' " +
            "  AND er.exam_count = (" +
            "    SELECT MAX(er2.exam_count) FROM exam_record er2 " +
            "    WHERE er2.student_id = er.student_id AND er2.course_id = er.course_id AND er2.status = 'submitted'" +
            "  )" +
            ") t")
    CourseStatisticsVO getCourseStatistics(Long courseId);

}
