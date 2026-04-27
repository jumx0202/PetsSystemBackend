package ynu.pet.controller;

import ynu.pet.dto.Result;
import ynu.pet.dto.UserDTO;
import ynu.pet.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ynu.pet.service.UserService;

@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户注册", description = "新用户注册，手机号唯一")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "注册成功",
                    content = @Content(schema = @Schema(implementation = UserDTO.class))),
            @ApiResponse(responseCode = "500", description = "手机号已注册")
    })
    @PostMapping("/register")
    public Result<UserDTO> register(
            @Parameter(description = "用户信息", required = true)
            @RequestBody User user) {
        return userService.register(user);
    }

    @Operation(summary = "用户登录", description = "使用手机号和密码登录")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "500", description = "用户不存在或密码错误")
    })
    @PostMapping("/login")
    public Result<UserDTO> login(
            @Parameter(description = "手机号") @RequestParam(name = "phone") String phone,
            @Parameter(description = "密码") @RequestParam(name = "password") String password) {
        return userService.login(phone, password);
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo(
            @Parameter(hidden = true)
            @RequestAttribute(name = "userId") Long userId) {
        return userService.getUserInfo(userId);
    }

    @Operation(summary = "更新用户信息", description = "修改用户名、头像等")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/update")
    public Result<Void> updateUser(
            @RequestBody User user,
            @Parameter(hidden = true) @RequestAttribute(name = "userId") Long userId) {
        user.setId(userId);
        return userService.updateUser(user);
    }

    @Operation(summary = "修改密码", description = "需要验证原密码")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Parameter(description = "原密码") @RequestParam(name = "oldPassword") String oldPassword,
            @Parameter(description = "新密码") @RequestParam(name = "newPassword") String newPassword,
            @Parameter(hidden = true) @RequestAttribute(name = "userId") Long userId) {
        return userService.updatePassword(userId, oldPassword, newPassword);
    }
}