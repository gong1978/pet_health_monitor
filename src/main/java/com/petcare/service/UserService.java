package com.petcare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.petcare.dto.LoginRequest;
import com.petcare.dto.LoginResponse;
import com.petcare.dto.ProfileResponse;
import com.petcare.dto.ProfileUpdateRequest;
import com.petcare.dto.RegisterRequest;
import com.petcare.dto.UserPageResponse;
import com.petcare.dto.UserQueryRequest;
import com.petcare.dto.UserUpdateRequest;
import com.petcare.entity.User;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {
    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     */
    void register(RegisterRequest registerRequest);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 根据用户ID查询用户
     */
    User getUserById(Integer userId);

    /**
     * 刷新 Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 分页查询用户
     */
    UserPageResponse pageQuery(UserQueryRequest queryRequest);

    /**
     * 更新用户信息
     */
    void updateUser(UserUpdateRequest updateRequest);

    /**
     * 删除用户
     */
    void deleteUser(Integer userId);

    /**
     * 批量删除用户
     */
    void batchDeleteUsers(java.util.List<Integer> userIds);

    /**
     * 创建用户
     */
    void createUser(com.petcare.dto.UserCreateRequest createRequest);

    /**
     * 获取个人信息
     */
    ProfileResponse getProfile(Integer userId);

    /**
     * 更新个人信息
     */
    void updateProfile(Integer userId, ProfileUpdateRequest profileUpdateRequest);
}
