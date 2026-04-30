package com.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Competition;
import com.project.service.ICompetitionService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 竞赛管理控制器
 * 处理竞赛项目的创建、查询、更新及删除操作
 */
@RestController
@RequestMapping("/api/competition")
public class CompetitionController {

    @Autowired
    private ICompetitionService competitionService;

    /**
     * 分页查询竞赛列表
     * 
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param name     竞赛名称（支持模糊搜索）
     * @param typeId   竞赛类型 ID
     * @param status   竞赛状态
     * @return 包含竞赛分页数据的 Result 对象
     */
    @GetMapping("/page")
    public Result<Page<Competition>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long typeId,
            @RequestParam(required = false) Integer status) {
        Page<Competition> page = competitionService.getCompetitionPage(pageNum, pageSize, name, typeId, status);
        return Result.success(page);
    }

    /**
     * 根据 ID 获取竞赛详情
     * 
     * @param id 竞赛 ID
     * @return 竞赛详情实体
     */
    @GetMapping("/{id}")
    public Result<Competition> getById(@PathVariable Long id) {
        return Result.success(competitionService.getById(id));
    }

    /**
     * 保存/新增一个竞赛项目
     * 
     * @param competition 竞赛实体数据
     * @return 操作结果
     */
    @PostMapping
    public Result<?> save(@RequestBody Competition competition) {
        return competitionService.save(competition) ? Result.success() : Result.error("保存失败");
    }

    /**
     * 更新竞赛项目资料
     * 
     * @param competition 需要更新的竞赛实体数据
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@RequestBody Competition competition) {
        return competitionService.updateById(competition) ? Result.success() : Result.error("更新失败");
    }

    /**
     * 根据 ID 删除指定的竞赛项目
     * 
     * @param id 竞赛 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return competitionService.removeById(id) ? Result.success() : Result.error("删除失败");
    }

    /**
     * 批量删除竞赛项目
     * 
     * @param ids 竞赛 ID 列表
     * @return 操作结果
     */
    @PostMapping("/batch")
    public Result<?> removeBatch(@RequestBody List<Long> ids) {
        return competitionService.removeByIds(ids) ? Result.success() : Result.error("删除失败");
    }
}
