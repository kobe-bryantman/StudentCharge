package com.example.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exam.common.BusinessException;
import com.example.exam.entity.User;
import com.example.exam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户Service
 * 处理用户登录认证等业务
 *
 * @author example
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录认证
     *
     * @param username 账号
     * @param password 密码
     * @return 登录成功的用户对象
     */
    public User login(String username, String password) {
        // 根据账号查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        // 校验用户是否存在
        if (user == null) {
            throw new BusinessException("账号不存在");
        }
        // 校验密码
        if (!password.equals(user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        return user;
    }

    /**
     * 根据ID查询用户
     */
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 更新用户信息
     */
    public void update(User user) {
        userMapper.updateById(user);
    }

}
