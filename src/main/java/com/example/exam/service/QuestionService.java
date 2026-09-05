package com.example.exam.service;

import com.example.exam.entity.Question;

import java.util.List;

/**
 * 题目Service接口
 * 处理题库CRUD业务
 *
 * @author example
 */
public interface QuestionService {

    /**
     * 查询指定课程的题目列表
     */
    List<Question> listByCourseId(Long courseId);

    /**
     * 根据ID查询题目
     */
    Question getById(Long id);

    /**
     * 新增题目
     */
    void save(Question question);

    /**
     * 更新题目
     */
    void update(Question question);

    /**
     * 删除题目
     */
    void remove(Long id);

    /**
     * 按课程ID删除所有考题
     *
     * @param courseId 课程ID
     */
    void removeByCourseId(Long courseId);

    /**
     * 统计课程题目数量
     */
    long countByCourseId(Long courseId);

}
