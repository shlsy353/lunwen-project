package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.entity.Competition;
import com.project.entity.ScoreAppeal;
import com.project.entity.User;
import com.project.service.ICompetitionService;
import com.project.service.IRegistrationService;
import com.project.service.IScoreAppealService;
import com.project.service.IUserService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表板统计数据控制器
 * 主要为管理端首页提供图表及关键指标的统计数据
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ICompetitionService competitionService;

    @Autowired
    private IRegistrationService registrationService;

    @Autowired
    private IScoreAppealService scoreAppealService;

    /**
     * 获取管理端首页的汇总统计指标
     * 涵盖：总用户量、活动中赛事量、总报名数、待处理申诉及待审核教师
     * 
     * @return 包含多个统计指标的 Map 数据封装
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 统计系统中总活跃用户量
        stats.put("userCount", userService.count());

        // 统计处于“正常报名”状态 (status=1) 的竞赛项目数量
        long activeCompetitions = competitionService.count(new QueryWrapper<Competition>().eq("status", 1));
        stats.put("activeCompetitionCount", activeCompetitions);

        // 获取系统累积的总报名人次
        stats.put("registrationCount", registrationService.count());

        // 统计所有待处理状态的成绩申诉件数 (status=0)
        long pendingAppeals = scoreAppealService.count(new QueryWrapper<ScoreAppeal>().eq("status", 0));
        stats.put("pendingAppealCount", pendingAppeals);

        // 统计目前正在申请“领队教师资格”的人数 (leader_status=1)
        long pendingLeaders = userService.count(new QueryWrapper<User>().eq("leader_status", 1));
        stats.put("pendingLeaderCount", pendingLeaders);

        return Result.success(stats);
    }
}
