package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Score;
import com.project.service.IScoreService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 成绩管理控制器
 * 处理学生在各竞赛中的分录查询、打分展示及成绩流水管理
 */
@RestController
@RequestMapping("/api/score")
public class ScoreController {

    @Autowired
    private IScoreService scoreService;

    /**
     * 分页查询学生的竞赛得分记录
     * 支持多维度的参数过滤：按竞赛项目、按学生个人、或按团队进行查询。
     * 
     * @param pageNum       首页码
     * @param pageSize      条数/每页
     * @param competitionId 筛选特定的竞赛项目
     * @param studentId     筛选特定的学生
     * @param teamId        筛选某个参赛团队
     * @return 分页数据的 Result 封装（包含成绩及评价相关数据）
     */
    @GetMapping("/page")
    public Result<Page<Score>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long teamId) {
        Page<Score> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Score> queryWrapper = new QueryWrapper<>();

        // 分别对传入的各个业务 ID 进行条件筛选
        if (competitionId != null) {
            queryWrapper.eq("competition_id", competitionId);
        }
        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        if (teamId != null) {
            queryWrapper.eq("team_id", teamId);
        }

        // 显示最新的成绩评定情况
        queryWrapper.orderByDesc("create_time");
        return Result.success(scoreService.page(page, queryWrapper));
    }

    /**
     * 保存一条新的成绩登记记录（适用于不通过作品评审，直接录入的情况）
     * 
     * @param score 包含分值与关联信息的成绩实体
     * @return 处理状态
     */
    @PostMapping
    public Result<?> save(@RequestBody Score score) {
        return scoreService.save(score) ? Result.success("评分记录已保存") : Result.error("操作失败");
    }
}
