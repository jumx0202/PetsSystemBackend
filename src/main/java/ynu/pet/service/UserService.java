package ynu.pet.service;

import ynu.pet.dto.Result;
import ynu.pet.dto.UserDTO;
import ynu.pet.entity.User;

public interface UserService {
    //注册：首次使用，保存用户到数据库
    Result<UserDTO> register(User user);
    //登录：已注册用户直接登录
    Result<UserDTO> login(String phone, String password);
    Result<UserDTO> getUserInfo(Long userId);
    Result<Void> updateUser(User user);
    Result<Void> updatePassword(Long userId, String oldPassword, String newPassword);
}
