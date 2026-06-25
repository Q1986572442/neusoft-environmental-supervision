package org.nep.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.exception.BusinessException;
import org.nep.common.result.Result;
import org.nep.common.utils.JwtUtils;
import org.nep.system.dto.LoginResult;
import org.nep.system.entity.User;
import org.nep.system.service.UserService;
import org.nep.system.service.impl.UserServiceImpl;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户注册、登录、信息管理、密码修改等功能
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserServiceImpl userServiceImpl;

    public UserController(UserService userService, UserServiceImpl userServiceImpl) {
        this.userService = userService;
        this.userServiceImpl = userServiceImpl;
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        return Result.ok("注册成功", userService.register(user));
    }

    @Operation(summary = "用户登录（返回JWT Token）")
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestParam String phone, @RequestParam String password) {
        return Result.ok("登录成功", userServiceImpl.loginWithToken(phone, password));
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) { return Result.ok(userService.getById(id)); }

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    public Result<java.util.List<User>> list() { return Result.ok(userService.list()); }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String keyword) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User> w =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (role != null && !role.isEmpty()) w.eq(User::getRole, role);
        if (keyword != null && !keyword.isEmpty())
            w.and(wr -> wr.like(User::getRealName, keyword).or().like(User::getPhone, keyword));
        w.orderByDesc(User::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> r =
                userService.page(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), w);
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("records", r.getRecords());
        result.put("total", r.getTotal());
        result.put("current", r.getCurrent());
        result.put("size", r.getSize());
        return Result.ok(result);
    }

    @Operation(summary = "更新用户（含头像）")
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody Map<String, Object> map) {
        User user = new User();
        user.setId(id);
        if (map.containsKey("realName")) user.setRealName((String) map.get("realName"));
        if (map.containsKey("age")) user.setAge(map.get("age") != null ? ((Number) map.get("age")).intValue() : null);
        if (map.containsKey("gender")) user.setGender(map.get("gender") != null ? ((Number) map.get("gender")).intValue() : null);
        if (map.containsKey("avatar")) user.setAvatar((String) map.get("avatar"));
        if (map.containsKey("email")) user.setEmail((String) map.get("email"));
        if (map.containsKey("status")) user.setStatus(map.get("status") != null ? ((Number) map.get("status")).intValue() : null);
        if (map.containsKey("role")) user.setRole((String) map.get("role"));
        if (map.containsKey("provinceId")) user.setProvinceId(map.get("provinceId") != null ? ((Number) map.get("provinceId")).longValue() : null);
        if (map.containsKey("cityId")) user.setCityId(map.get("cityId") != null ? ((Number) map.get("cityId")).longValue() : null);
        userService.updateById(user);
        return Result.success("更新成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = Long.parseLong(body.get("userId"));
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        User user = userService.getById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        String oldEnc = DigestUtils.md5DigestAsHex(oldPassword.getBytes(StandardCharsets.UTF_8));
        if (!oldEnc.equals(user.getPassword())) throw new BusinessException("原密码错误");

        user.setPassword(DigestUtils.md5DigestAsHex(newPassword.getBytes(StandardCharsets.UTF_8)));
        userService.updateById(user);
        return Result.success("密码修改成功，请重新登录");
    }

    @Operation(summary = "重置密码（管理员）")
    @PutMapping("/reset-password/{id}")
    public Result<Void> resetPassword(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
        userService.updateById(user);
        return Result.success("密码已重置为123456");
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/status/{id}")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) throw new BusinessException("用户不存在");
        user.setStatus(user.getStatus() == 0 ? 1 : 0);
        userService.updateById(user);
        return Result.success(user.getStatus() == 1 ? "用户已启用" : "用户已禁用");
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "根据Token获取当前用户信息")
    @GetMapping("/current")
    public Result<User> current(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) throw new BusinessException("未登录");
        String token = authHeader.substring(7);
        Long userId = JwtUtils.getUserId(token);
        User user = userService.getById(userId);
        if (user != null) user.setPassword(null);
        return Result.ok(user);
    }
}
