package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Major;
import com.project.service.IMajorService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 专业信息管理控制器
 * 处理各学院下属专业的维护，如名称编辑、对应学院关联及展示
 */
@RestController
@RequestMapping("/api/major")
public class MajorController {

    @Autowired
    private IMajorService majorService;

    /**
     * 分页查询专业列表信息
     * 
     * @param pageNum  当前页码
     * @param pageSize 条数/每页
     * @param name     专业名称进行模糊搜索
     * @return 分页数据的 Result 封装
     */
    @GetMapping("/page")
    public Result<Page<Major>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<Major> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Major> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        return Result.success(majorService.page(page, queryWrapper));
    }

    /**
     * 获取指定学院或系统的全部专业列表
     * 这里根据 collegeId 进行了过滤处理，若不传则获取全部
     * 
     * @param collegeId (可选) 筛选某个特定学院的专业
     * @return 符合条件的专业实体集合
     */
    @GetMapping("/list")
    public Result<List<Major>> list(@RequestParam(required = false) Long collegeId) {
        QueryWrapper<Major> queryWrapper = new QueryWrapper<>();
        if (collegeId != null) {
            queryWrapper.eq("college_id", collegeId);
        }
        return Result.success(majorService.list(queryWrapper));
    }

    /**
     * 新增专业并关联其所在学院
     * 
     * @param major 专业实体数据
     * @return 保存结果信息
     */
    @PostMapping
    public Result<?> save(@RequestBody Major major) {
        return majorService.save(major) ? Result.success("添加成功") : Result.error("添加失败");
    }

    /**
     * 更新专业基础资料（名称或学院 ID 归属）
     * 
     * @param major 包含更新项的实体
     * @return 更新结果
     */
    @PutMapping
    public Result<?> update(@RequestBody Major major) {
        return majorService.updateById(major) ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 根据 ID 删除对应的专业记录
     * 
     * @param id 专业主键 ID
     * @return 状态信息
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return majorService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }
}
