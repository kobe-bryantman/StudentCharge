package com.example.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.entity.*;
import com.example.exam.mapper.ExamAnswerMapper;
import com.example.exam.mapper.ExamRecordMapper;
import com.example.exam.service.ExamService;
import com.example.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 考试Service实现类
 *
 * @author example
 */
@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private QuestionService questionService;

    @Override
    @Transactional
    public ExamRecord startExam(Long studentId, Long courseId) {
        List<ExamRecord> existing = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getCourseId, courseId));
        int examCount = existing.size() + 1;

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

    @Override
    public List<Question> getExamQuestions(Long courseId) {
        return questionService.listByCourseId(courseId);
    }

    @Override
    @Transactional
    public ExamRecord submitExam(Long examRecordId, Long studentId, Map<Long, String> answers) {
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

        List<Question> questions = questionService.listByCourseId(record.getCourseId());
        int totalScore = 0;

        for (Question question : questions) {
            String studentAnswer = answers.get(question.getId());
            if (studentAnswer == null) {
                studentAnswer = "";
            }

            boolean isCorrect = false;
            if ("multiple".equals(question.getQuestionType())) {
                isCorrect = normalizeAnswer(studentAnswer).equals(normalizeAnswer(question.getCorrectAnswer()));
            } else {
                isCorrect = studentAnswer.trim().equalsIgnoreCase(question.getCorrectAnswer().trim());
            }

            int score = isCorrect ? question.getScore() : 0;
            totalScore += score;

            ExamAnswer answer = new ExamAnswer();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(question.getId());
            answer.setStudentAnswer(studentAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setScore(score);
            examAnswerMapper.insert(answer);
        }

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
        Arrays.sort(chars);
        return new String(chars);
    }

    @Override
    public List<ExamRecord> listByStudentId(Long studentId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .orderByDesc(ExamRecord::getExamTime));
    }

    @Override
    public ExamRecord getRecordById(Long id) {
        return examRecordMapper.selectById(id);
    }

    @Override
    public List<ExamAnswer> listAnswersByRecordId(Long examRecordId) {
        return examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getExamRecordId, examRecordId));
    }

    @Override
    public List<ExamRecord> listByCourseId(Long courseId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getCourseId, courseId)
                        .orderByDesc(ExamRecord::getExamTime));
    }

}
