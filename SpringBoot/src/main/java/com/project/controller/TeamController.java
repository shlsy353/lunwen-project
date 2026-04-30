package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Team;
import com.project.service.ITeamService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 参赛团队管理控制器
 * 处理学生自由组建的竞赛团队、团队分页查询及我的团队列表
 */
@RestController
@RequestMapping("/api/team")
public class TeamController {

    @Autowired
    private ITeamService teamService;

    /**
     * 分页查询团队信息
     * 
     * @param pageNum  首页码
     * @param pageSize 条数/每页
     * @param leaderId (可选) 筛选特定领队老师名下的团队
     * @param name     (可选) 按照团队名称进行模糊查询
     * @return 分页数据 Result 封装
     */
    @GetMapping("/page")
    public Result<Page<Team>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long leaderId,
            @RequestParam(required = false) String name) {
        Page<Team> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (leaderId != null) {
            queryWrapper.eq("leader_id", leaderId);
        }
        if (name != null) {
            queryWrapper.like("name", name);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(teamService.page(page, queryWrapper));
    }

    @Autowired
    private com.project.service.ITeamMemberService teamMemberService;

    /**
     * 业务接口：创建一个新的竞赛团队并同步添加其初始成员
     * 
     * @param teamDTO 包含 Team 基础信息及初始成员 ID 列表的数据传输对象
     * @return 业务处理状态
     */
    @PostMapping("/create")
    public Result<?> createTeam(@RequestBody com.project.controller.dto.TeamDTO teamDTO) {
        Team team = teamDTO.getTeam();
        boolean success = teamService.save(team);

        // 成功创建团队本体后，批量持久化其团队成员映射记录
        if (success && teamDTO.getMemberIds() != null) {
            for (Long studentId : teamDTO.getMemberIds()) {
                com.project.entity.TeamMember member = new com.project.entity.TeamMember();
                member.setTeamId(team.getId());
                member.setStudentId(studentId);
                member.setRole("队员");
                teamMemberService.save(member);
            }
        }
        return success ? Result.success("团队创建成功") : Result.error("创建失败");
    }

    @Autowired
    private com.project.util.JwtUtil jwtUtil;

    /**
     * 接口：获取与当前登录用户（学生）相关的全部所属团队
     * 根据身份 Token 反查学生参与的全部 TeamMember 联表记录，进而获取团队详单
     * 
     * @param authHeader 携带 Token 的请求头
     * @return 学生加入的团队列表
     */
    @GetMapping("/my-teams")
    public Result<java.util.List<Team>> listMyTeams(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Result.error("认证失败：用户未登录");
            }
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserId(token);

            // 1. 先查询成员关联表
            QueryWrapper<com.project.entity.TeamMember> q = new QueryWrapper<>();
            q.eq("student_id", userId);
            java.util.List<com.project.entity.TeamMember> members = teamMemberService.list(q);

            if (members.isEmpty()) {
                return Result.success(new java.util.ArrayList<>());
            }

            // 2. 依据关联记录提取团队实体
            java.util.List<Long> teamIds = members.stream().map(com.project.entity.TeamMember::getTeamId)
                    .collect(java.util.stream.Collectors.toList());
            java.util.List<Team> teams = teamService.listByIds(teamIds);
            return Result.success(teams);
        } catch (Exception e) {
            return Result.error("获取所属团队列表异常");
        }
    }

    /**
     * 保存/新增一个团队实体（纯实体操作）
     * 
     * @param team 团队资料
     * @return 处理结果
     */
    @PostMapping
    public Result<?> save(@RequestBody Team team) {
        return teamService.save(team) ? Result.success("保存成功") : Result.error("保存失败");
    }

    /**
     * 更新团队的基本资料
     * 
     * @param team 待更新项
     * @return 处理动作结果
     */
    @PutMapping
    public Result<?> update(@RequestBody Team team) {
        return teamService.updateById(team) ? Result.success("资料更新成功") : Result.error("更新失败");
    }

    /**
     * 根据 ID 解散或删除特定的团队
     * 
     * @param id 团队 ID
     * @return 业务反馈
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return teamService.removeById(id) ? Result.success("团队已解散") : Result.error("删除动作失败");
    }
}
