package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.WorkSubmission;
import com.project.service.IWorkSubmissionService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.web.bind.annotation.*;

/**
 * 作品提交与评审管理控制器
 * 处理学生作品提交轨迹的分页查询、提交及领队/评委在线评分
 */
@RestController
@RequestMapping("/api/work-submission")
public class WorkSubmissionController {

    @Autowired
    private IWorkSubmissionService workSubmissionService;

    @Autowired
    private com.project.service.ICompetitionService competitionService;

    /**
     * 分页查询作品提交列表信息
     */
    @GetMapping("/page")
    public Result<Page<WorkSubmission>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Long studentId) {
        Page<WorkSubmission> page = new Page<>(pageNum, pageSize);
        QueryWrapper<WorkSubmission> queryWrapper = new QueryWrapper<>();
        if (competitionId != null) {
            queryWrapper.eq("competition_id", competitionId);
        }
        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        queryWrapper.orderByDesc("create_time");
        return Result.success(workSubmissionService.page(page, queryWrapper));
    }

    /**
     * 学生端：正式提交竞赛作品
     */
    @PostMapping
    public Result<?> save(@RequestBody WorkSubmission workSubmission) {
        // 校验竞赛时间：比赛开始后/结束后逻辑
        com.project.entity.Competition competition = competitionService.getById(workSubmission.getCompetitionId());
        if (competition == null)
            return Result.error("关联竞赛不存在");

        // 如果设定了比赛日期且当前日期已晚于比赛日期，禁止提交
        if (competition.getCompetitionDate() != null
                && competition.getCompetitionDate().isBefore(java.time.LocalDate.now())) {
            return Result.error("竞赛已进入评测阶段或已结束，无法再提交作品");
        }

        return workSubmissionService.save(workSubmission) ? Result.success("提交成功") : Result.error("提交失败");
    }

    /**
     * 学生端：撤回作品提交
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        WorkSubmission submission = workSubmissionService.getById(id);
        if (submission == null)
            return Result.error("作品记录不存在");

        // 1. 检查是否已经打分
        if (submission.getScore() != null) {
            return Result.error("作品已经经过老师评审打分，无法撤回");
        }

        // 2. 检查提交时间（超过 24 小时禁止撤回）
        if (submission.getCreateTime() != null && submission.getCreateTime().plusHours(24)
                .isBefore(java.time.LocalDateTime.now())) {
            return Result.error("作品已提交超过 24 小时，已进入归档状态，无法撤回");
        }

        // 3. 检查竞赛时间
        com.project.entity.Competition competition = competitionService.getById(submission.getCompetitionId());
        if (competition != null && competition.getCompetitionDate() != null &&
                competition.getCompetitionDate().isBefore(java.time.LocalDate.now())) {
            return Result.error("所属竞赛活动已截止，无法撤回作品");
        }

        return workSubmissionService.removeById(id) ? Result.success("已成功撤回作品") : Result.error("撤回失败");
    }

    /**
     * 管理端/领队端：更新作品状态（打分与评价）
     */
    @PutMapping
    public Result<?> update(@RequestBody WorkSubmission workSubmission) {
        if (workSubmission.getId() == null)
            return Result.error("ID不能为空");

        // 使用 LambdaUpdateWrapper 支持局部打分（不覆盖其他未传字段）
        LambdaUpdateWrapper<WorkSubmission> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(WorkSubmission::getId, workSubmission.getId());

        boolean hasUpdate = false;
        if (workSubmission.getScore() != null) {
            updateWrapper.set(WorkSubmission::getScore, workSubmission.getScore());
            hasUpdate = true;
        }
        if (workSubmission.getComment() != null) {
            updateWrapper.set(WorkSubmission::getComment, workSubmission.getComment());
            hasUpdate = true;
        }

        // 如果没有涉及到分数或评价的修改，按照普通的整型实体更新处理
        if (!hasUpdate) {
            return workSubmissionService.updateById(workSubmission) ? Result.success("更新成功") : Result.error("更新失败");
        }

        return workSubmissionService.update(updateWrapper) ? Result.success("评分/评价成功") : Result.error("打分失败");
    }
}
