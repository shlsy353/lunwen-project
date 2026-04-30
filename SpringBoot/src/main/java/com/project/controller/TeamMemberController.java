package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.entity.TeamMember;
import com.project.service.ITeamMemberService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 团队成员关联管理控制器
 * 处理具体某个竞赛团队内部的人员组成、添加新成员及移除队员操作
 */
@RestController
@RequestMapping("/api/team-member")
public class TeamMemberController {

    @Autowired
    private ITeamMemberService teamMemberService;

    /**
     * 获取指定团队的全部成员列表
     * 
     * @param teamId 团队的主键 ID
     * @return 成员关联实体列表（包含用户 ID 及对应角色，如队长、队员）
     */
    @GetMapping("/list")
    public Result<List<TeamMember>> list(@RequestParam Long teamId) {
        QueryWrapper<TeamMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("team_id", teamId);
        return Result.success(teamMemberService.list(queryWrapper));
    }

    /**
     * 向某个特定团队添加新的成员（持久化关联记录）
     * 
     * @param teamMember 成员关联资料
     * @return 操作结果反馈
     */
    @PostMapping
    public Result<?> save(@RequestBody TeamMember teamMember) {
        return teamMemberService.save(teamMember) ? Result.success("成员已成功入队") : Result.error("添加新成员失败");
    }

    /**
     * 根据记录 ID 将某名成员从所属团队中移除
     * 
     * @param id 成员关联表的自增 ID
     * @return 处理动作状态
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return teamMemberService.removeById(id) ? Result.success("已成功移出队员") : Result.error("移除操作动作失败");
    }
}
