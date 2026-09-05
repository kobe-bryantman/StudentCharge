package com.example.exam.service;

import com.example.exam.entity.ExamAnswer;
import com.example.exam.entity.ExamRecord;
import com.example.exam.entity.Question;

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
     * 开始考试
     */
    ExamRecord startExam(Long studentId, Long courseId);

    /**
     * 获取考试题目列表
     */
    List<Question> getExamQuestions(Long courseId);

    /**
     * 交卷并自动阅卷
     */
    ExamRecord submitExam(Long examRecordId, Long studentId, Map<Long, String> answers);

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

}
