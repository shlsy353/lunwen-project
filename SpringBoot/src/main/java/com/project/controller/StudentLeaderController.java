package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.project.entity.StudentLeader;
import com.project.entity.User;
import com.project.service.IStudentLeaderService;
import com.project.service.IUserService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 师生关联/领队管理控制器
 * 维护学生与负责其竞赛工作的领队老师之间的绑定关系（包括：申请关联、老师通过及学生导出等功能）
 */
@RestController
@RequestMapping("/api/student-leader")
public class StudentLeaderController {

    @Autowired
    private IStudentLeaderService studentLeaderService;

    @Autowired
    private IUserService userService;

    /**
     * 获取指定老师当前名下的所有关联学生列表
     * 核心逻辑是基于关联表中 status=1 (已确认) 的记录，再通过 ID 反拉学生基本信息。
     * 
     * @param leaderId 当前教师/领队的唯一 ID
     * @return 该老师目前所管理的正式学生资料集合
     */
    @GetMapping("/my-students")
    public Result<List<User>> getMyStudents(@RequestParam Long leaderId) {
        List<StudentLeader> associations = studentLeaderService.list(new QueryWrapper<StudentLeader>()
                .eq("leader_id", leaderId).eq("status", 1));

        // 若无已绑定的学生，快捷返回空列表
        if (associations.isEmpty())
            return Result.success(new ArrayList<>());

        List<Long> studentIds = associations.stream().map(StudentLeader::getStudentId).collect(Collectors.toList());
        return Result.success(userService.listByIds(studentIds));
    }

    /**
     * 获取指定学生当前所跟随的所有领队老师
     * 用于学生端查询确认自己究竟属于哪些老师麾下。
     * 
     * @param studentId 当前登录学生的 ID
     * @return 该学生目前所归属的正式领队资料（已剔除敏感的密码项）
     */
    @GetMapping("/my-leaders")
    public Result<List<User>> getMyLeaders(@RequestParam Long studentId) {
        List<StudentLeader> associations = studentLeaderService.list(new QueryWrapper<StudentLeader>()
                .eq("student_id", studentId).eq("status", 1));

        if (associations.isEmpty())
            return Result.success(new ArrayList<>());

        List<Long> leaderIds = associations.stream().map(StudentLeader::getLeaderId).collect(Collectors.toList());
        List<User> leaders = userService.listByIds(leaderIds);

        // 此处为保障安全，返回前清空老师的原始密码字段
        leaders.forEach(u -> u.setPassword(null));
        return Result.success(leaders);
    }

    /**
     * 学生主动申请：与特定的领队老师建立辅导关联
     * 业务规则：检测 ID 的有效性，并防止针对同一个老师发起重复的或尚未处理的申请。
     * 
     * @param association 关联项请求体，包含 leaderId 和 studentId
     * @return 业务逻辑处理结果反馈
     */
    @PostMapping("/apply")
    public Result<?> apply(@RequestBody StudentLeader association) {
        if (association.getStudentId() == null || association.getLeaderId() == null) {
            return Result.error("参数错误：目标老师及当前学生信息不全");
        }

        // 关键防控：拦截重复提交关联请求（包括审核中项）
        long count = studentLeaderService.count(new QueryWrapper<StudentLeader>()
                .eq("student_id", association.getStudentId())
                .eq("leader_id", association.getLeaderId()));
        if (count > 0)
            return Result.error("关联已存在或正在老师审核列队中，请耐心等候");

        // 初始化关联状态为 0 (待处理/审核中)
        association.setStatus(0);
        return studentLeaderService.save(association) ? Result.success("关联申请已送达老师端") : Result.error("提交申请失败");
    }

    /**
     * 老师端手动拉入：通过学生工号（ID）直接添加指定的学生关联
     * 不同于申请，老师侧添加此处默认为直接通过关联 (status=1)。
     * 
     * @param association 关联实体
     * @return 操作返回
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody StudentLeader association) {
        if (association.getStudentId() == null || association.getLeaderId() == null) {
            return Result.error("参数不完整");
        }

        // 核心设定：老师手动录入的学生直接置为：1-正式关联
        association.setStatus(1);
        return studentLeaderService.save(association) ? Result.success("已成功添加至管理学生列表") : Result.error("添加操作失败");
    }

    /**
     * 老师端审核：针对学生发来的“跟随申请”进行通过或拒绝
     * 
     * @param association 包含 ID 及其新状态 (1 为通过，2 为拒绝)
     * @return 处理动作的结果
     */
    @PutMapping("/status")
    public Result<?> updateStatus(@RequestBody StudentLeader association) {
        if (association.getId() == null || association.getStatus() == null) {
            return Result.error("参数错误：ID 信息缺失");
        }
        return studentLeaderService.updateById(association) ? Result.success("操作已同步") : Result.error("处理失败");
    }

    /**
     * 获取老师端待确认收徒的申请列表
     * 
     * @param leaderId 领队老师 ID
     * @return 待通过的学生申请详单 (status=0)
     */
    @GetMapping("/pending")
    public Result<List<StudentLeader>> getPending(@RequestParam Long leaderId) {
        return Result.success(studentLeaderService.list(new QueryWrapper<StudentLeader>()
                .eq("leader_id", leaderId).eq("status", 0)));
    }

    /**
     * 强制解除关联关系
     * 这里使用软注销策略：将状态 status 打标记为 3，代表已解除/注销，而非真实的物理删除，保存审计轨迹
     * 
     * @param leaderId  被解除关联的老师
     * @param studentId 被移除管理的学生
     * @return 解绑提示
     */
    @DeleteMapping("/remove")
    public Result<?> remove(@RequestParam Long leaderId, @RequestParam Long studentId) {
        UpdateWrapper<StudentLeader> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("leader_id", leaderId).eq("student_id", studentId).set("status", 3);
        return studentLeaderService.update(updateWrapper) ? Result.success("已移除师生关系") : Result.error("解除失败");
    }
}
