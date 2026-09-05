package com.example.exam.service;

import com.example.exam.entity.User;

/**
 * 用户Service接口
 * 处理用户登录认证等业务
 *
 * @author example
 */
public interface UserService {

    /**
     * 用户登录认证
     *
     * @param username 账号
     * @param password 密码
     * @return 登录成功的用户对象
     */
    User login(String username, String password);

    /**
     * 根据ID查询用户
     */
    User getById(Long id);

    /**
     * 更新用户信息
     */
    void update(User user);

}
