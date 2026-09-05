package com.example.exam.service;

import com.example.exam.entity.*;

import java.util.List;
import java.util.Map;

/**
 * 考试Service接口
 * 处理考试流程：开始考试、交卷阅卷、查看成绩
 *
 * @author example
 */
public interface ExamService {

    /**
     * 开始考试（有ongoing记录则复用，否则新建）
     */
    ExamRecord startExam(Long studentId, Long courseId);

    /**
     * 查询学生某课程的最新考试记录（按exam_count降序取第一条）
     */
    ExamRecord getLatestRecord(Long studentId, Long courseId);

    /**
     * 获取考试题目列表
     */
    List<Question> getExamQuestions(Long courseId);

    /**
     * 交卷并自动批改（事务方法）
     * 验证记录归属+ongoing状态，批改每题，插入答题明细，更新成绩
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID（从session获取）
     * @param answers      学生答案 Map<题目ID, 答案字符串>
     * @return 更新后的考试记录
     */
    ExamRecord gradeExam(Long examRecordId, Long studentId, Map<Long, String> answers);

    /**
     * 查询学生的考试记录列表
     */
    List<ExamRecord> listByStudentId(Long studentId);

    /**
     * 根据ID查询考试记录
     */
    ExamRecord getRecordById(Long id);

    /**
     * 查询考试记录的答题明细
     */
    List<ExamAnswer> listAnswersByRecordId(Long examRecordId);

    /**
     * 查询课程的所有考试记录
     */
    List<ExamRecord> listByCourseId(Long courseId);

    // ==================== 教师端考试情况查询 ====================

    /**
     * 统计某课程已考人数（status=submitted，按student_id去重）
     */
    int countSubmittedByCourse(Long courseId);

    /**
     * 查询某课程所有已交卷学生的最新成绩（每个学生取exam_count最大的submitted记录）
     * 关联user查学生姓名、学号、学院，按成绩降序排列
     */
    List<StudentScoreVO> listLatestSubmittedByCourse(Long courseId);

    /**
     * 课程成绩统计：平均分、最高分、及格率、参考人数
     * 基于每个学生最新的submitted记录统计
     */
    CourseStatisticsVO getCourseStatistics(Long courseId);

    /**
     * 查询某学生某课程的所有已交卷记录（按exam_count升序）
     */
    List<ExamRecord> listSubmittedRecordsByStudentAndCourse(Long studentId, Long courseId);

}
