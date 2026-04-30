package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Registration;
import com.project.service.IRegistrationService;
import com.project.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 竞赛报名管理控制器
 * 处理学生报名竞赛、取消报名、报名列表查询及关联校验
 */
@Slf4j
@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    @Autowired
    private IRegistrationService registrationService;

    @Autowired
    private com.project.service.IStudentLeaderService studentLeaderService;

    /**
     * 分页查询报名记录列表
     * 
     * @param pageNum       当前页码
     * @param pageSize      每页显示数量
     * @param competitionId 筛选特定的竞赛
     * @param studentId     筛选特定的学生
     * @param leaderId      筛选特定的领队老师
     * @return 包含报名记录的分页结果
     */
    @GetMapping("/page")
    public Result<Page<Registration>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long leaderId) {
        Page<Registration> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Registration> queryWrapper = new QueryWrapper<>();

        // 动态添加筛选条件
        if (competitionId != null) {
            queryWrapper.eq("competition_id", competitionId);
        }
        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        if (leaderId != null) {
            queryWrapper.eq("leader_id", leaderId);
        }

        // 按报名时间倒序排列，最新报名的显示在最前
        queryWrapper.orderByDesc("create_time");
        return Result.success(registrationService.page(page, queryWrapper));
    }

    /**
     * 提交竞赛报名申请
     * 在保存前会验证：1. 参数完整性 2. 重复报名 3. 领队关联状态
     * 
     * @param registration 报名实体数据
     * @return 操作结果及错误提示
     */
    @PostMapping
    public Result<?> save(@RequestBody Registration registration) {
        if (registration.getCompetitionId() == null || registration.getStudentId() == null) {
            return Result.error("参数错误：竞赛或学生 ID 不能为空");
        }

        // 1. 检查是否存在重复报名记录（同一学生、同一竞赛）
        long count = registrationService.count(new QueryWrapper<Registration>()
                .eq("competition_id", registration.getCompetitionId())
                .eq("student_id", registration.getStudentId()));
        if (count > 0) {
            return Result.error("您已报名该竞赛，无需重复操作");
        }

        // 2. 领队关联性校验：学生必须先与领队老师建立正式关联 (status=1)
        if (registration.getLeaderId() == null) {
            return Result.error("请选择您的领队老师");
        }

        long slCount = studentLeaderService.count(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.project.entity.StudentLeader>()
                        .eq("student_id", registration.getStudentId())
                        .eq("leader_id", registration.getLeaderId())
                        .eq("status", 1));
        if (slCount == 0) {
            return Result.error("您尚未与该领队老师建立正式关联，请先申请或等待老师审核");
        }

        // 3. 设置默认状态为 0 (待审核)
        if (registration.getStatus() == null) {
            registration.setStatus(0);
        }

        try {
            return registrationService.save(registration) ? Result.success("报名成功") : Result.error("报名失败");
        } catch (Exception e) {
            log.error("报名保存异常", e);
            return Result.error("服务器异常，请稍后重试");
        }
    }

    /**
     * 更新报名记录（用于管理员或领队修改报名状态）
     * 
     * @param registration 更新后的报名实体数据
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@RequestBody Registration registration) {
        return registrationService.updateById(registration) ? Result.success() : Result.error("记录更新失败");
    }

    /**
     * 根据 ID 撤回/取消报名申请
     * 
     * @param id 报名记录 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return registrationService.removeById(id) ? Result.success("已撤销报名") : Result.error("取消失败");
    }
}
