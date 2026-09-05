package com.example.exam.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类（统一用户表，role区分教师/学生）
 *
 * @author example
 */
@Data
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码 */
    private String password;

    /** 真实姓名 */
    private String name;

    /** 性别 */
    private String gender;

    /** 手机号 */
    private String phone;

    /** 角色（teacher/student） */
    private String role;

    /** 工号（教师用，学生可为空） */
    private String teacherNo;

    /** 学号（学生用，教师可为空） */
    private String studentNo;

    /** 所在学院 */
    private String college;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
