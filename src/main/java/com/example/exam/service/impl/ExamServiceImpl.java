package com.example.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.common.BusinessException;
import com.example.exam.entity.*;
import com.example.exam.mapper.ExamAnswerMapper;
import com.example.exam.mapper.ExamRecordMapper;
import com.example.exam.service.ExamService;
import com.example.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
        // 优先复用进行中的考试记录
        ExamRecord ongoing = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getCourseId, courseId)
                        .eq(ExamRecord::getStatus, "ongoing")
                        .last("LIMIT 1"));
        if (ongoing != null) {
            return ongoing;
        }

        // 计算新的exam_count：该学生该课程已有最大count + 1
        List<ExamRecord> existing = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getCourseId, courseId)
                        .orderByDesc(ExamRecord::getExamCount));
        int examCount = existing.isEmpty() ? 1 : existing.get(0).getExamCount() + 1;

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
    public ExamRecord getLatestRecord(Long studentId, Long courseId) {
        return examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getCourseId, courseId)
                        .orderByDesc(ExamRecord::getExamCount)
                        .last("LIMIT 1"));
    }

    @Override
    public List<Question> getExamQuestions(Long courseId) {
        return questionService.listByCourseId(courseId);
    }

    @Override
    @Transactional
    public ExamRecord gradeExam(Long examRecordId, Long studentId, Map<Long, String> answers) {
        // a. 查exam_record，验证存在+归属当前学生+status=ongoing
        ExamRecord record = examRecordMapper.selectById(examRecordId);
        if (record == null) {
            throw new BusinessException("考试记录不存在");
        }
        if (!record.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作此考试记录");
        }
        if ("submitted".equals(record.getStatus())) {
            throw new BusinessException("已交卷，请勿重复提交");
        }

        // b. 查该课程所有考题，转为Map便于查找
        List<Question> questions = questionService.listByCourseId(record.getCourseId());
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        // c. 遍历每道题批改
        List<ExamAnswer> examAnswerList = new ArrayList<>();
        for (Question question : questions) {
            String rawAnswer = answers.get(question.getId());
            // 未作答存"未作答"
            String studentAnswer = (rawAnswer == null || rawAnswer.trim().isEmpty())
                    ? "未作答" : rawAnswer.trim().toUpperCase();

            // 与正确答案比对（忽略大小写+排序）
            boolean isCorrect = normalizeAnswer(rawAnswer).equals(normalizeAnswer(question.getCorrectAnswer()));

            int score = isCorrect ? question.getScore() : 0;

            ExamAnswer answer = new ExamAnswer();
            answer.setExamRecordId(examRecordId);
            answer.setQuestionId(question.getId());
            answer.setStudentAnswer(studentAnswer);
            answer.setIsCorrect(isCorrect ? 1 : 0);
            answer.setScore(score);
            examAnswerMapper.insert(answer);
            examAnswerList.add(answer);
        }

        // d. Stream流计算总分
        int totalScore = examAnswerList.stream().mapToInt(ExamAnswer::getScore).sum();

        // e. 更新exam_record
        record.setTotalScore(totalScore);
        record.setStatus("submitted");
        record.setExamTime(LocalDateTime.now());
        examRecordMapper.updateById(record);

        return record;
    }

    /**
     * 规范化答案（去空格、转大写、排序，确保单选/多选格式一致）
     * 例：["B","A"] → "AB"，数据库存"AB"也能匹配
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

    // ==================== 教师端考试情况查询 ====================

    @Override
    public int countSubmittedByCourse(Long courseId) {
        return examRecordMapper.countSubmittedByCourse(courseId);
    }

    @Override
    public List<StudentScoreVO> listLatestSubmittedByCourse(Long courseId) {
        return examRecordMapper.listLatestSubmittedByCourse(courseId);
    }

    @Override
    public CourseStatisticsVO getCourseStatistics(Long courseId) {
        return examRecordMapper.getCourseStatistics(courseId);
    }

    @Override
    public List<ExamRecord> listSubmittedRecordsByStudentAndCourse(Long studentId, Long courseId) {
        return examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getCourseId, courseId)
                        .eq(ExamRecord::getStatus, "submitted")
                        .orderByAsc(ExamRecord::getExamCount));
    }

    @Override
    @Transactional
    public ExamRecord retakeExam(Long studentId, Long courseId) {
        // 计算新的exam_count：查该学生该课程已有最大count + 1
        List<ExamRecord> existing = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, studentId)
                        .eq(ExamRecord::getCourseId, courseId)
                        .orderByDesc(ExamRecord::getExamCount));
        int examCount = existing.isEmpty() ? 1 : existing.get(0).getExamCount() + 1;

        // 新建ongoing记录，不删除历史
        ExamRecord record = new ExamRecord();
        record.setStudentId(studentId);
        record.setCourseId(courseId);
        record.setTotalScore(0);
        record.setStatus("ongoing");
        record.setExamCount(examCount);
        examRecordMapper.insert(record);

        return record;
    }

    @Override
    public boolean hasExamRecords(Long courseId) {
        if (courseId == null) {
            return false;
        }
        Long count = examRecordMapper.selectCount(
                new LambdaQueryWrapper<ExamRecord>().eq(ExamRecord::getCourseId, courseId));
        return count != null && count > 0;
    }

}
