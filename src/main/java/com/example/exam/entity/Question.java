package com.example.exam.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考题实体类
 *
 * @author example
 */
@Data
@TableName("question")
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属课程ID */
    private Long courseId;

    /** 题干 */
    @NotBlank(message = "题干不能为空")
    private String questionText;

    /** 选项A */
    private String optionA;

    /** 选项B */
    private String optionB;

    /** 选项C */
    private String optionC;

    /** 选项D */
    private String optionD;

    /** 正确答案（A/B/C/D，多选如AB） */
    private String correctAnswer;

    /** 题型（single单选/multiple多选） */
    private String questionType;

    /** 分值 */
    @NotNull(message = "分值不能为空")
    @Min(value = 1, message = "分值至少1分")
    private Integer score;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
