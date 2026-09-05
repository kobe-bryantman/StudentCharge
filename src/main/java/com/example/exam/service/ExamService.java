package com.example.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.entity.*;
import com.example.exam.mapper.ExamAnswerMapper;
import com.example.exam.mapper.ExamRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考试Service
 * 处理考试流程：开始考试、交卷阅卷、查看成绩
 *
 * @author example
 */
@Service
public class ExamService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private QuestionService questionService;

    /**
     * 开始考试
     * 创建考试记录，状态为ongoing，计算第几次考试
     *
     * @param studentId 学生ID
     * @param courseId  课程ID
     * @return 考试记录
     */
    @Transactional
    public ExamRecord startExam(Long studentId, Long courseId) {
        // 查询该学生该课程已有的考试次数
        List<ExamRecord> existing = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getCourseId, courseId));
        int examCount = existing.size() + 1;

        // 创建新的考试记录
        ExamRecord record = new ExamRecord();
        record.setStudentId(studentId);
        record.setCourseId(courseId);
        record.setTotalScore(0);
        record.setExamTime(LocalDateTime.now());
        record.setStatus("ongoing");
        record.setExamCount(examCount);
        examRecordMapper.insert(record);

        return record;
    }

    /**
     * 获取考试题目列表
     */
    public List<Question> getExamQuestions(Long courseId) {
        return questionService.listByCourseId(courseId);
    }

    /**
     * 交卷并自动阅卷
     *
     * @param examRecordId 考试记录ID
     * @param studentId    学生ID
     * @param answers      学生答案：key=题目ID, value=答案
     * @return 阅卷后的考试记录
     */
    @Transactional
    public ExamRecord submitExam(Long examRecordId, Long studentId, java.util.Map<Long, String> answers) {
        // 查询考试记录
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        if (!record.getStudentId().equals(studentId)) {
            throw new RuntimeException("无权操作此考试记录");
        }
        if ("submitted".equals(record.getStatus())) {
            throw new RuntimeException("试卷已提交，不能重复提交");
        }

        // 获取该课程所有题目
        List<Question> questions = questionService.listByCourseId(record.getCourseId());
        int totalScore = 0;

        // 逐题阅卷
        for (Question question : questions) {
            String studentAnswer = answers.get(question.getId());
            if (studentAnswer == null) {
                studentAnswer = "";
            }

            // 判断题是否正确（忽略大小写，去空格）
            boolean isCorrect = false;
            if ("multiple".equals(question.getQuestionType())) {
                // 多选题：排序后比较
                isCorrect = normalizeAnswer(studentAnswer).equals(normalizeAnswer(question.getCorrectAnswer()));
            } else {
                // 单选题：直接比较
                isCorrect = studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
            }

            int score = isCorrect ? question.getScore() : 0;
            totalScore += score;

            // 保存答题明细
            ExamAnswer answer = new ExamAnswer();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(question.getId());
            answer.setStudentAnswer(studentAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setScore(score);
            examAnswerMapper.insert(answer);
        }

        // 更新考试记录：总分、状态改为已交卷
        record.setTotalScore(totalScore);
        record.setStatus("submitted");
        record.setExamTime(LocalDateTime.now());
        examRecordMapper.updateById(record);

        return record;
    }

    /**
     * 规范化答案（多选题排序，去空格，转大写）
     */
    private String normalizeAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        String upper = answer.trim().toUpperCase().replaceAll("\\s+", "");
        char[] chars = upper.toCharArray();
        java.util.Arrays.sort(chars);
        return new String(chars);
    }

    /**
     * 查询学生的考试记录列表
     */
    public List<ExamRecord> listByStudentId(Long studentId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .orderByDesc(ExamRecord::getExamTime));
    }

    /**
     * 根据ID查询考试记录
     */
    public ExamRecord getRecordById(Long id) {
        return examRecordMapper.selectById(id);
    }

    /**
     * 查询考试记录的答题明细
     */
    public List<ExamAnswer> listAnswersByRecordId(Long examRecordId) {
        return examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getExamRecordId, examRecordId));
    }

    /**
     * 查询课程的所有考试记录（教师端查看考试情况）
     */
    public List<ExamRecord> listByCourseId(Long courseId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getCourseId, courseId)
                        .orderByDesc(ExamRecord::getExamTime));
    }

}
