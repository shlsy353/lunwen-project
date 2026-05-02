package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.User;
import com.project.service.IUserService;
import com.project.util.JwtUtil;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户管理控制器
 * 处理登录、注册以及用户资料的相关操作
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User user) {
        return userService.login(user);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserId(token);
            return userService.getUserInfo(userId);
        } catch (Exception e) {
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        // 检查用户名重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userService.count(queryWrapper) > 0) {
            return Result.error("该用户名/账号已存在");
        }
        // 使用 MD5 加密原始密码
        user.setPassword(cn.hutool.crypto.SecureUtil.md5(user.getPassword()));
        return userService.save(user) ? Result.success("注册成功") : Result.error("注册失败");
    }

    /**
     * 分页查询用户列表
     * 支持根据用户名、姓名、角色、学号以及领队状态进行筛选
     */
    @GetMapping("/page")
    public Result<Page<User>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Integer leaderStatus) {
        Page<User> page = new Page<>(pageNum, pageSize);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        // 如果提供了用户名或姓名，进行模糊查询
        if (StringUtils.hasText(username) || StringUtils.hasText(name)) {
            queryWrapper.nested(w -> {
                if (StringUtils.hasText(username)) {
                    w.like("username", username);
                }
                if (StringUtils.hasText(name)) {
                    w.or().like("name", name);
                }
            });
        }

        // 角色筛选（支持逗号分隔的多个角色）
        if (StringUtils.hasText(role)) {
            if (role.contains(",")) {
                queryWrapper.in("role", (Object[]) role.split(","));
            } else {
                queryWrapper.eq("role", role);
            }
        }

        // 学号精确匹配
        if (StringUtils.hasText(studentId)) {
            queryWrapper.eq("student_id", studentId);
        }

        // 领队状态筛选（0-未申请, 1-申请中, 2-已通过）
        if (leaderStatus != null) {
            queryWrapper.eq("leader_status", leaderStatus);
        }

        queryWrapper.orderByDesc("create_time");
        return Result.success(userService.page(page, queryWrapper));
    }

    /**
     * 新增用户资料
     */
    @PostMapping
    public Result<?> save(@RequestBody User user) {
        // 检查用户名重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (userService.count(queryWrapper) > 0) {
            return Result.error("该用户名/工号已存在");
        }
        // 如果未设置密码，默认使用 "123456" 并加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(cn.hutool.crypto.SecureUtil.md5(user.getPassword()));
        } else {
            user.setPassword(cn.hutool.crypto.SecureUtil.md5("123456"));
        }
        return userService.save(user) ? Result.success() : Result.error("保存失败");
    }

    /**
     * 更新用户资料
     */
    @PutMapping
    public Result<?> update(@RequestBody User user) {
        // 检查用户名重复（排除自身）
        if (StringUtils.hasText(user.getUsername())) {
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username", user.getUsername());
            queryWrapper.ne("id", user.getId());
            if (userService.count(queryWrapper) > 0) {
                return Result.error("该用户名/工号已与其他用户冲突");
            }
        }
        // 如果更新了密码，则将其加密
        if (StringUtils.hasText(user.getPassword())) {
            // 简单判断：如果长度小于 32 位，则认为是明文，需要 MD5 处理
            if (user.getPassword().length() < 32) {
                user.setPassword(cn.hutool.crypto.SecureUtil.md5(user.getPassword()));
            }
        }
        // 如果角色不是领队教师，重置领队状态，防止数据不一致
        // 避免出现 role=TEACHER 但 leaderStatus=2 的脏数据
        if (!"LEADER".equals(user.getRole())) {
            user.setLeaderStatus(0);
        }
        return userService.updateById(user) ? Result.success() : Result.error("更新失败");
    }

    /**
     * 根据 ID 获取单个用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null); // 为了安全，清除返回对象中的密码
        }
        return Result.success(user);
    }

    /**
     * 根据 ID 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return userService.removeById(id) ? Result.success() : Result.error("删除失败");
    }

    /**
     * 领队老师资格申请
     */
    @PutMapping("/apply-leader")
    public Result<?> applyLeader(@RequestBody User user) {
        User dbUser = userService.getById(user.getId());
        if (dbUser == null)
            return Result.error("用户不存在");
        if (!"TEACHER".equals(dbUser.getRole()))
            return Result.error("仅教师可申请领队");

        dbUser.setLeaderStatus(1); // 设置状态为：申请中
        return userService.updateById(dbUser) ? Result.success("申请已提交") : Result.error("提交失败");
    }
}
