package com.example.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.exam.entity.ExamAnswer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答题明细Mapper接口
 *
 * @author example
 */
@Mapper
public interface ExamAnswerMapper extends BaseMapper<ExamAnswer> {

}
