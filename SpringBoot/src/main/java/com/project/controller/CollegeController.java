package com.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.entity.College;
import com.project.service.ICollegeService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 学院信息管理控制器
 * 提供学院的基础信息维护，包括新增、编辑、删除以及分页查询
 */
@RestController
@RequestMapping("/api/college")
public class CollegeController {

    @Autowired
    private ICollegeService collegeService;

    /**
     * 分页查询学院信息
     * 
     * @param pageNum  当前页码
     * @param pageSize 每页包含记录数
     * @param name     按照学院名称进行模糊查询
     * @return 分页数据的 Result 封装
     */
    @GetMapping("/page")
    public Result<Page<College>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<College> page = new Page<>(pageNum, pageSize);
        QueryWrapper<College> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        return Result.success(collegeService.page(page, queryWrapper));
    }

    /**
     * 获取所有学院的精简列表
     * 常用于前端下拉框数据源
     * 
     * @return 所有学院实体组成的列表
     */
    @GetMapping("/list")
    public Result<List<College>> list() {
        return Result.success(collegeService.list());
    }

    /**
     * 新增一个学院项目
     * 
     * @param college 学院实体
     * @return 操作状态
     */
    @PostMapping
    public Result<?> save(@RequestBody College college) {
        return collegeService.save(college) ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新已有学院的基本信息
     * 
     * @param college 需要更新的实体数据
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@RequestBody College college) {
        return collegeService.updateById(college) ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 根据学院 ID 删除记录
     * 
     * @param id 学院 ID
     * @return 操作提示息
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return collegeService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
