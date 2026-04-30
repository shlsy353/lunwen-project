package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.CompetitionType;
import com.project.service.ICompetitionTypeService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 竞赛分类管理控制器
 * 处理竞赛类型的增删改查及下拉列表展示
 */
@RestController
@RequestMapping("/api/competition-type")
public class CompetitionTypeController {

    @Autowired
    private ICompetitionTypeService competitionTypeService;

    /**
     * 分页查询竞赛分类信息
     * 
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @param name     分类名称（模糊查询）
     * @return 分页结果封装
     */
    @GetMapping("/page")
    public Result<Page<CompetitionType>> page(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<CompetitionType> page = new Page<>(pageNum, pageSize);
        QueryWrapper<CompetitionType> queryWrapper = new QueryWrapper<>();
        if (name != null) {
            queryWrapper.like("name", name);
        }
        return Result.success(competitionTypeService.page(page, queryWrapper));
    }

    /**
     * 获取所有竞赛分类的列表
     * 通常用于前端配置竞赛时的类型选择下拉菜单
     * 
     * @return 分类实体列表
     */
    @GetMapping("/list")
    public Result<List<CompetitionType>> list() {
        return Result.success(competitionTypeService.list());
    }

    /**
     * 新增竞赛分类
     * 
     * @param competitionType 分类实体
     * @return 操作状态
     */
    @PostMapping
    public Result<?> save(@RequestBody CompetitionType competitionType) {
        return competitionTypeService.save(competitionType) ? Result.success("保存成功") : Result.error("保存失败");
    }

    /**
     * 更新竞赛分类资料
     * 
     * @param competitionType 待更新的实体
     * @return 操作状态
     */
    @PutMapping
    public Result<?> update(@RequestBody CompetitionType competitionType) {
        return competitionTypeService.updateById(competitionType) ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 根据 ID 删除指定的竞赛分类
     * 
     * @param id 分类 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return competitionTypeService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
