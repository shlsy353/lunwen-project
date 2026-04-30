package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Award;
import com.project.service.IAwardService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 获奖管理控制器
 * 处理学生获奖记录的查询、颁奖及删除记录
 */
@RestController
@RequestMapping("/api/award")
public class AwardController {

    @Autowired
    private IAwardService awardService;

    /**
     * 分页查询获奖记录列表
     * 
     * @param pageNum       当前页码
     * @param pageSize      每页显示记录数
     * @param competitionId 筛选指定的竞赛项目
     * @param studentId     筛选指定的学生
     * @return 获奖记录分页数据
     */
    @GetMapping("/page")
    public Result<Page<Award>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Long studentId) {
        Page<Award> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Award> queryWrapper = new QueryWrapper<>();
        if (competitionId != null) {
            queryWrapper.eq("competition_id", competitionId);
        }
        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        return Result.success(awardService.page(page, queryWrapper));
    }

    /**
     * 给指定的项目/学生颁发奖项
     * 
     * @param award 获奖信息实体
     * @return 操作结果
     */
    @PostMapping
    public Result<?> save(@RequestBody Award award) {
        return awardService.save(award) ? Result.success("颁奖成功") : Result.error("颁奖失败");
    }

    /**
     * 根据 ID 撤回或删除某条获奖记录
     * 
     * @param id 获奖记录 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return awardService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
