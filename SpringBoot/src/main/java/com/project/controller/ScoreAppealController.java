package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.ScoreAppeal;
import com.project.service.IScoreAppealService;
import com.project.util.JwtUtil;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.project.service.IStudentLeaderService;
import java.util.List;
import java.util.stream.Collectors;
import com.project.entity.StudentLeader;

/**
 * 成绩申诉管理控制器
 * 处理学生针对竞赛成绩的申诉提交、撤回以及领队审核
 */
@RestController
@RequestMapping("/api/score-appeal")
public class ScoreAppealController {

    @Autowired
    private IScoreAppealService scoreAppealService;

    @Autowired
    private IStudentLeaderService studentLeaderService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 分页查询申诉记录
     * 
     * @param pageNum       当前页码
     * @param pageSize      每页条数
     * @param status        申诉状态 (0-待处理, 1-已处理, 2-已撤回)
     * @param studentId     学生 ID
     * @param competitionId 竞赛 ID
     * @return 申诉记录分页信息
     */
    @GetMapping("/page")
    public Result<Page<ScoreAppeal>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Long leaderId,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        String role = "";
        Long userId = null;
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            role = jwtUtil.getRole(token);
            userId = jwtUtil.getUserId(token);
        } else {
            return Result.error("未登录或 Token 失效");
        }

        Page<ScoreAppeal> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ScoreAppeal> queryWrapper = new QueryWrapper<>();

        if (status != null) {
            queryWrapper.eq("status", status);
        }

        // 核心权限逻辑
        if ("ADMIN".equals(role)) {
            // 管理员具备全局视野，但也支持按学生、领队或竞赛筛选
            if (studentId != null)
                queryWrapper.eq("student_id", studentId);
            if (competitionId != null)
                queryWrapper.eq("competition_id", competitionId);
            if (leaderId != null) {
                queryWrapper.inSql("student_id", "SELECT student_id FROM student_leader WHERE leader_id = " + leaderId);
            }
        } else if ("TEACHER".equals(role) || "LEADER".equals(role)) {
            // 教师/领队老师只能查看自己负责且已审核通过的学生提交的申诉
            // 先获取当前名下所有已关联的学生ID
            List<Long> myStudentIds = studentLeaderService
                    .list(new QueryWrapper<StudentLeader>().eq("leader_id", userId))
                    .stream().map(StudentLeader::getStudentId).collect(Collectors.toList());

            if (myStudentIds.isEmpty()) {
                // 如果名下没学生，绝对查不到任何申诉
                queryWrapper.apply("1=0");
            } else {
                queryWrapper.in("student_id", myStudentIds);
            }

            // 允许进一步按竞赛筛选自己的业务
            if (competitionId != null)
                queryWrapper.eq("competition_id", competitionId);
        } else if ("STUDENT".equals(role)) {
            // 学生只能看自己的申诉
            queryWrapper.eq("student_id", userId);
        } else {
            // 未知角色或权限不足，返回空数据
            queryWrapper.apply("1=0");
        }

        queryWrapper.orderByDesc("create_time");
        return Result.success(scoreAppealService.page(page, queryWrapper));
    }

    /**
     * 提交一条新的成绩申诉申请
     * 
     * @param scoreAppeal 申诉资料
     * @return 处理结果
     */
    @PostMapping
    public Result<?> save(@RequestBody ScoreAppeal scoreAppeal) {
        return scoreAppealService.save(scoreAppeal) ? Result.success("申诉提交成功") : Result.error("提交申诉失败");
    }

    /**
     * 修改或处理申诉记录（領队使用）
     * 
     * @param scoreAppeal 更新资料
     * @return 处理结果
     */
    @PutMapping
    public Result<?> update(@RequestBody ScoreAppeal scoreAppeal, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String role = jwtUtil.getRole(token);
        Long userId = jwtUtil.getUserId(token);

        if (!"ADMIN".equals(role)) {
            // 非管理员处理申诉，必须校验该申诉学生是否属于该领队
            ScoreAppeal dbAppeal = scoreAppealService.getById(scoreAppeal.getId());
            if (dbAppeal == null)
                return Result.error("申诉记录不存在");

            QueryWrapper<com.project.entity.StudentLeader> qw = new QueryWrapper<>();
            qw.eq("student_id", dbAppeal.getStudentId());
            qw.eq("leader_id", userId);
            // 检查 student_leader 表中是否存在该关联
            if (studentLeaderService.count(qw) == 0) {
                return Result.error("权限不足，您不是该学生的领队老师");
            }
        }

        // 执行更新（如填写 result, 修改 status 等）
        return scoreAppealService.updateById(scoreAppeal) ? Result.success("处理成功") : Result.error("处理失败");
    }

    /**
     * 学生主动撤回尚未处理的申诉
     * 
     * @param id 申诉记录 ID
     * @return 处理结果
     */
    @PutMapping("/{id}/withdraw")
    public Result<?> withdraw(@PathVariable Long id) {
        ScoreAppeal appeal = scoreAppealService.getById(id);
        if (appeal == null) {
            return Result.error("申诉记录不存在");
        }
        appeal.setStatus(2); // 2-已撤回
        return scoreAppealService.updateById(appeal) ? Result.success("已撤回") : Result.error("撤回失败");
    }
}
