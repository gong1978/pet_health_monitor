package com.petcare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.petcare.common.JwtUtil;
import com.petcare.common.PasswordUtil;
import com.petcare.dto.LoginRequest;
import com.petcare.dto.LoginResponse;
import com.petcare.dto.ProfileResponse;
import com.petcare.dto.ProfileUpdateRequest;
import com.petcare.dto.RegisterRequest;
import com.petcare.dto.UserPageResponse;
import com.petcare.dto.UserQueryRequest;
import com.petcare.dto.UserUpdateRequest;
import com.petcare.dto.UserCreateRequest;
import com.petcare.entity.User;
import com.petcare.entity.UserPet;
import com.petcare.entity.VetConsultation;
import com.petcare.mapper.UserMapper;
import com.petcare.mapper.UserPetMapper;
import com.petcare.mapper.VetConsultationMapper;
import com.petcare.service.PetService; // [新增] 导入 PetService
import com.petcare.service.UserService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private UserPetMapper userPetMapper;

    @Autowired
    private VetConsultationMapper vetConsultationMapper;

    // [新增] 注入 PetService 用于级联删除宠物
    @Autowired
    private PetService petService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        // 验证参数
        if (loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }

        // 查询用户
        User user = getUserByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证密码
        if (!PasswordUtil.verifyPassword(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成 Token
        String accessToken = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 构建响应
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        // 验证参数
        if (registerRequest.getUsername() == null || registerRequest.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (registerRequest.getConfirmPassword() == null || registerRequest.getConfirmPassword().isEmpty()) {
            throw new RuntimeException("确认密码不能为空");
        }
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        if (registerRequest.getRole() == null || (registerRequest.getRole() < 1 || registerRequest.getRole() > 3)) {
            throw new RuntimeException("角色不合法");
        }

        // 检查用户名是否已存在
        User existUser = getUserByUsername(registerRequest.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(PasswordUtil.encryptPassword(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .phone(registerRequest.getPhone())
                .role(registerRequest.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        // 保存用户
        boolean success = this.save(user);
        if (!success) {
            throw new RuntimeException("注册失败，请稍后重试");
        }

        log.info("用户注册成功: {}", registerRequest.getUsername());
    }

    @Override
    public User getUserByUsername(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return this.getOne(queryWrapper);
    }

    @Override
    public User getUserById(Integer userId) {
        return this.getById(userId);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        // 验证 refreshToken
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("刷新 Token 无效");
        }

        // 解析 Token 获取用户信息
        Claims claims = jwtUtil.parseToken(refreshToken);
        if (claims == null) {
            throw new RuntimeException("刷新 Token 解析失败");
        }

        Integer userId = claims.get("userId", Integer.class);
        String username = claims.get("username", String.class);

        // 查询用户
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 生成新的 accessToken
        String newAccessToken = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUserId(), user.getUsername());

        // 构建响应
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .expiresIn(jwtExpiration)
                .build();
    }

    @Override
    public UserPageResponse pageQuery(UserQueryRequest queryRequest) {
        // 验证分页参数
        Integer pageNum = queryRequest.getPageNum();
        Integer pageSize = queryRequest.getPageSize();

        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }

        // 构建查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        if (queryRequest.getUsername() != null && !queryRequest.getUsername().isEmpty()) {
            queryWrapper.like("username", queryRequest.getUsername());
        }
        if (queryRequest.getFullName() != null && !queryRequest.getFullName().isEmpty()) {
            queryWrapper.like("full_name", queryRequest.getFullName());
        }
        if (queryRequest.getEmail() != null && !queryRequest.getEmail().isEmpty()) {
            queryWrapper.like("email", queryRequest.getEmail());
        }
        if (queryRequest.getPhone() != null && !queryRequest.getPhone().isEmpty()) {
            queryWrapper.like("phone", queryRequest.getPhone());
        }
        if (queryRequest.getRole() != null) {
            queryWrapper.eq("role", queryRequest.getRole());
        }

        // 排序
        String sortBy = queryRequest.getSortBy();
        String sortOrder = queryRequest.getSortOrder();

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "created_at";
        }
        if (sortOrder == null || sortOrder.isEmpty()) {
            sortOrder = "desc";
        }

        if ("asc".equalsIgnoreCase(sortOrder)) {
            queryWrapper.orderByAsc(sortBy);
        } else {
            queryWrapper.orderByDesc(sortBy);
        }

        // 执行分页查询
        long total = this.count(queryWrapper);
        long offset = (long) (pageNum - 1) * pageSize;

        List<User> users = this.list(queryWrapper.last("LIMIT " + offset + "," + pageSize));

        // 转换为DTO
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<UserPageResponse.UserDTO> records = users.stream()
                .map(user -> UserPageResponse.UserDTO.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole())
                        .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "")
                        .build())
                .collect(Collectors.toList());

        // 计算总页数
        int totalPages = (int) ((total + pageSize - 1) / pageSize);

        return UserPageResponse.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .totalPages(totalPages)
                .records(records)
                .build();
    }

    @Override
    public void updateUser(UserUpdateRequest updateRequest) {
        // 验证参数
        if (updateRequest.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 检查用户是否存在
        User user = getUserById(updateRequest.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证角色
        if (updateRequest.getRole() != null && (updateRequest.getRole() < 1 || updateRequest.getRole() > 3)) {
            throw new RuntimeException("角色不合法");
        }

        // 更新用户信息
        User updateUser = User.builder()
                .userId(updateRequest.getUserId())
                .fullName(updateRequest.getFullName())
                .email(updateRequest.getEmail())
                .phone(updateRequest.getPhone())
                .role(updateRequest.getRole())
                .build();

        boolean success = this.updateById(updateUser);
        if (!success) {
            throw new RuntimeException("更新用户失败");
        }

        log.info("用户信息更新成功: {}", updateRequest.getUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Integer userId) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 检查用户是否存在
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // [核心修改] 1. 查找并级联删除该用户拥有的所有宠物
        // 这一步确保宠物的详细信息（Alerts, SensorData, Reports等）被 PetService 清理
        QueryWrapper<UserPet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        List<UserPet> userPets = userPetMapper.selectList(queryWrapper);

        if (userPets != null && !userPets.isEmpty()) {
            for (UserPet up : userPets) {
                // 调用 PetService 的删除方法，它会负责删除 pets 表及其关联的子表数据
                petService.deletePet(up.getPetId());
            }
        }

        // 2. 确保用户-宠物关联关系已被清空 (petService.deletePet 可能已处理，此处兜底)
        userPetMapper.delete(queryWrapper);

        // 3. 删除用户发起的兽医咨询 (VetConsultation)
        QueryWrapper<VetConsultation> consultationQuery = new QueryWrapper<>();
        consultationQuery.eq("user_id", userId);
        vetConsultationMapper.delete(consultationQuery);

        // 4. 删除用户本身
        boolean success = this.removeById(userId);
        if (!success) {
            throw new RuntimeException("删除用户失败");
        }

        log.info("用户及其关联数据（宠物、咨询等）删除成功: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Integer> userIds) {
        // 验证参数
        if (userIds == null || userIds.isEmpty()) {
            throw new RuntimeException("用户ID列表不能为空");
        }

        // [核心修改] 循环调用 deleteUser 以确保触发每个用户的级联删除逻辑
        // 这样可以保证批量删除时，也能连带删除所有相关的宠物数据
        for (Integer userId : userIds) {
            deleteUser(userId);
        }

        log.info("批量删除用户成功: {}", userIds);
    }

    @Override
    public void createUser(UserCreateRequest createRequest) {
        // 参数校验
        if (createRequest.getUsername() == null || createRequest.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        if (createRequest.getPassword() == null || createRequest.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        if (createRequest.getRole() == null || createRequest.getRole() < 1 || createRequest.getRole() > 3) {
            throw new RuntimeException("角色不合法");
        }

        // 检查用户名唯一
        User existUser = getUserByUsername(createRequest.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 构造用户并加密密码
        User user = User.builder()
                .username(createRequest.getUsername())
                .password(PasswordUtil.encryptPassword(createRequest.getPassword()))
                .fullName(createRequest.getFullName())
                .email(createRequest.getEmail())
                .phone(createRequest.getPhone())
                .role(createRequest.getRole())
                .createdAt(LocalDateTime.now())
                .build();

        boolean success = this.save(user);
        if (!success) {
            throw new RuntimeException("创建用户失败");
        }

        log.info("创建用户成功: {}", user.getUsername());
    }

    @Override
    public ProfileResponse getProfile(Integer userId) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 查询用户信息
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 角色名称映射
        String roleName = getRoleName(user.getRole());

        // 构建响应
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ProfileResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .roleName(roleName)
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : "")
                .build();
    }

    @Override
    public void updateProfile(Integer userId, ProfileUpdateRequest profileUpdateRequest) {
        // 验证参数
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 检查用户是否存在
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证邮箱格式
        if (profileUpdateRequest.getEmail() != null && !profileUpdateRequest.getEmail().isEmpty()) {
            String emailRegex = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";
            if (!profileUpdateRequest.getEmail().matches(emailRegex)) {
                throw new RuntimeException("邮箱格式不正确");
            }
        }

        // 验证手机号格式
        if (profileUpdateRequest.getPhone() != null && !profileUpdateRequest.getPhone().isEmpty()) {
            String phoneRegex = "^1[3-9]\\d{9}$";
            if (!profileUpdateRequest.getPhone().matches(phoneRegex)) {
                throw new RuntimeException("手机号格式不正确");
            }
        }

        // 构建更新对象
        User updateUser = User.builder()
                .userId(userId)
                .build();

        // 更新基本信息
        if (profileUpdateRequest.getFullName() != null && !profileUpdateRequest.getFullName().isEmpty()) {
            updateUser.setFullName(profileUpdateRequest.getFullName());
        }
        if (profileUpdateRequest.getEmail() != null && !profileUpdateRequest.getEmail().isEmpty()) {
            updateUser.setEmail(profileUpdateRequest.getEmail());
        }
        if (profileUpdateRequest.getPhone() != null && !profileUpdateRequest.getPhone().isEmpty()) {
            updateUser.setPhone(profileUpdateRequest.getPhone());
        }

        // 如果要修改密码
        if (profileUpdateRequest.getNewPassword() != null && !profileUpdateRequest.getNewPassword().isEmpty()) {
            // 验证当前密码
            if (profileUpdateRequest.getCurrentPassword() == null || profileUpdateRequest.getCurrentPassword().isEmpty()) {
                throw new RuntimeException("修改密码时必须提供当前密码");
            }

            // 验证当前密码是否正确
            if (!PasswordUtil.verifyPassword(profileUpdateRequest.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("当前密码错误");
            }

            // 验证新密码
            if (profileUpdateRequest.getNewPassword().length() < 6) {
                throw new RuntimeException("新密码长度不能少于6个字符");
            }

            // 验证确认密码
            if (!profileUpdateRequest.getNewPassword().equals(profileUpdateRequest.getConfirmPassword())) {
                throw new RuntimeException("两次输入的新密码不一致");
            }

            // 加密新密码
            updateUser.setPassword(PasswordUtil.encryptPassword(profileUpdateRequest.getNewPassword()));
        }

        // 更新用户信息
        boolean success = this.updateById(updateUser);
        if (!success) {
            throw new RuntimeException("更新个人信息失败");
        }

        log.info("用户个人信息更新成功: {}", userId);
    }

    /**
     * 获取角色名称
     */
    private String getRoleName(Integer role) {
        if (role == null) {
            return "未知";
        }
        switch (role) {
            case 1:
                return "管理员";
            case 2:
                return "兽医";
            case 3:
                return "宠物主人";
            default:
                return "未知";
        }
    }
}