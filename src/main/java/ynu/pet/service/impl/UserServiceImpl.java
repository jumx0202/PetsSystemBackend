package ynu.pet.service.impl;

import ynu.pet.dto.Result;
import ynu.pet.dto.UserDTO;
import ynu.pet.entity.User;
import ynu.pet.mapper.UserMapper;
import ynu.pet.service.UserService;
import ynu.pet.utils.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result<UserDTO> register(User user) {
        // 1. 检查手机号是否已注册
        User existingUser = userMapper.findByPhone(user.getPhone());
        if (existingUser != null) {
            return Result.error("手机号已注册，请直接登录");
        }

        // 2. 【移除密码加密】密码明文存储
        // user.setPassword(PasswordUtil.encode(user.getPassword()));  // 注释掉加密

        // 3. 设置默认昵称（如果没有）
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            user.setUsername("用户" + user.getPhone().substring(7));
        }

        // 4. 保存到数据库（密码是明文）
        userMapper.insert(user);

        // 5. 返回用户信息（带token）
        UserDTO dto = convertToDTO(user);
        dto.setToken(JwtUtil.generateToken(user.getId()));
        return Result.success(dto);
    }

    @Override
    public Result<UserDTO> login(String phone, String password) {
        // 1. 查询用户
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            return Result.error("用户不存在，请先注册");
        }

        // 2. 【明文密码比较】直接比较，不使用BCrypt
        if (!password.equals(user.getPassword())) {
            return Result.error("密码错误");
        }

        // 3. 返回用户信息（带token）
        UserDTO dto = convertToDTO(user);
        dto.setToken(JwtUtil.generateToken(user.getId()));
        return Result.success(dto);
    }

    @Override
    public Result<UserDTO> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(convertToDTO(user));
    }

    @Override
    public Result<Void> updateUser(User user) {
        userMapper.update(user);
        return Result.success();
    }

    @Override
    public Result<Void> updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 【明文密码验证】
        if (!oldPassword.equals(user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 【新密码明文存储】
        user.setPassword(newPassword);  // 直接存明文，不加密
        userMapper.update(user);
        return Result.success();
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        dto.setPassword(null);  // DTO不返回密码
        return dto;
    }
}