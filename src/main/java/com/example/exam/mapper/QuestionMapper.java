package com.example.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考题Mapper接口
 *
 * @author example
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

}
