package com.example.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.entity.Question;
import com.example.exam.mapper.QuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 题目Service
 * 处理题库CRUD业务
 *
 * @author example
 */
@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    /**
     * 查询指定课程的题目列表
     */
    public List<Question> listByCourseId(Long courseId) {
        return questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getCourseId, courseId)
                        .orderByAsc(Question::getId));
    }

    /**
     * 根据ID查询题目
     */
    public Question getById(Long id) {
        return questionMapper.selectById(id);
    }

    /**
     * 新增题目
     */
    public void save(Question question) {
        questionMapper.insert(question);
    }

    /**
     * 更新题目
     */
    public void update(Question question) {
        questionMapper.updateById(question);
    }

    /**
     * 删除题目
     */
    public void remove(Long id) {
        questionMapper.deleteById(id);
    }

    /**
     * 统计课程题目数量
     */
    public long countByCourseId(Long courseId) {
        return questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getCourseId, courseId));
    }

}
