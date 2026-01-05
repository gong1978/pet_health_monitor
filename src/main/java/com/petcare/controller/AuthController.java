package com.petcare.controller;

import com.petcare.common.Result;
import com.petcare.dto.LoginRequest;
import com.petcare.dto.LoginResponse;
import com.petcare.dto.ProfileResponse;
import com.petcare.dto.ProfileUpdateRequest;
import com.petcare.dto.RegisterRequest;
import com.petcare.dto.UserPageResponse;
import com.petcare.dto.UserQueryRequest;
import com.petcare.dto.UserUpdateRequest;
import com.petcare.dto.UserCreateRequest;
import com.petcare.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("用户登录: {}", loginRequest.getUsername());
        try {
            LoginResponse loginResponse = userService.login(loginRequest);
            return Result.success(loginResponse);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest registerRequest) {
        log.info("用户注册: {}", registerRequest.getUsername());
        try {
            userService.register(registerRequest);
            return Result.success("注册成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 刷新 Token
     */
    @PostMapping("/refresh-token")
    public Result<LoginResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        log.info("刷新 Token");
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.fail(400, "Token 格式错误");
            }
            String refreshToken = authHeader.substring(7);
            LoginResponse loginResponse = userService.refreshToken(refreshToken);
            return Result.success(loginResponse);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/user-info")
    public Result<Object> getUserInfo(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        Integer role = (Integer) request.getAttribute("role");

        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        return Result.success(new Object() {
            public final Integer id = userId;
            public final String name = username;
            public final Integer userRole = role;
        });
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }
        log.info("用户登出: {}", userId);
        return Result.success("登出成功");
    }

    /**
     * 分页查询用户
     */
    @PostMapping("/users/page")
    public Result<UserPageResponse> pageQuery(@RequestBody UserQueryRequest queryRequest) {
        log.info("分页查询用户: pageNum={}, pageSize={}", queryRequest.getPageNum(), queryRequest.getPageSize());
        try {
            UserPageResponse response = userService.pageQuery(queryRequest);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public Result<Object> getUserDetail(@PathVariable Integer userId) {
        log.info("获取用户详情: {}", userId);
        try {
            if (userId == null) {
                return Result.fail(400, "用户ID不能为空");
            }
            var user = userService.getUserById(userId);
            if (user == null) {
                return Result.fail(400, "用户不存在");
            }
            return Result.success(new Object() {
                public final Integer id = user.getUserId();
                public final String username = user.getUsername();
                public final String fullName = user.getFullName();
                public final String email = user.getEmail();
                public final String phone = user.getPhone();
                public final Integer role = user.getRole();
            });
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/users")
    public Result<String> updateUser(@RequestBody UserUpdateRequest updateRequest) {
        log.info("更新用户信息: {}", updateRequest.getUserId());
        try {
            userService.updateUser(updateRequest);
            return Result.success("用户信息更新成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{userId}")
    public Result<String> deleteUser(@PathVariable Integer userId) {
        log.info("删除用户: {}", userId);
        try {
            userService.deleteUser(userId);
            return Result.success("用户删除成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 批量删除用户
     */
    @PostMapping("/users/batch-delete")
    public Result<String> batchDeleteUsers(@RequestBody List<Integer> userIds) {
        log.info("批量删除用户: {}", userIds);
        try {
            userService.batchDeleteUsers(userIds);
            return Result.success("用户批量删除成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 创建用户
     */
    @PostMapping("/users")
    public Result<String> createUser(@RequestBody UserCreateRequest createRequest) {
        log.info("创建用户: {}", createRequest.getUsername());
        try {
            userService.createUser(createRequest);
            return Result.success("创建用户成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public Result<ProfileResponse> getProfile(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("获取个人信息: {}", userId);
        try {
            ProfileResponse profile = userService.getProfile(userId);
            return Result.success(profile);
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }

    /**
     * 更新个人信息
     */
    @PutMapping("/profile")
    public Result<String> updateProfile(HttpServletRequest request, @RequestBody ProfileUpdateRequest profileUpdateRequest) {
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            return Result.unauthorized("未授权");
        }

        log.info("更新个人信息: {}", userId);
        try {
            userService.updateProfile(userId, profileUpdateRequest);
            return Result.success("个人信息更新成功");
        } catch (RuntimeException e) {
            return Result.fail(400, e.getMessage());
        }
    }
}
