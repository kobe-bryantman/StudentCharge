package com.example.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.entity.Question;
import com.example.exam.mapper.QuestionMapper;
import com.example.exam.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目Service实现类
 *
 * @author example
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Override
    public List<Question> listByCourseId(Long courseId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getCourseId, courseId)
                        .orderByAsc(Question::getId));
    }

    @Override
    public Question getById(Long id) {
        return questionMapper.selectById(id);
    }

    @Override
    public void save(Question question) {
        questionMapper.insert(question);
    }

    @Override
    public void update(Question question) {
        questionMapper.updateById(question);
    }

    @Override
    public void remove(Long id) {
        questionMapper.deleteById(id);
    }

    /**
     * 按课程ID删除所有考题
     */
    @Override
    public void removeByCourseId(Long courseId) {
        questionMapper.delete(
                new LambdaQueryWrapper<Question>().eq(Question::getCourseId, courseId));
    }

    @Override
    public long countByCourseId(Long courseId) {
        return questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getCourseId, courseId));
    }

}
