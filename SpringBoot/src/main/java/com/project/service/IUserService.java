package com.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.entity.User;
import com.project.util.Result;

import java.util.Map;

public interface IUserService extends IService<User> {
    Result<Map<String, Object>> login(User user);

    Result<User> getUserInfo(Long userId);
}
