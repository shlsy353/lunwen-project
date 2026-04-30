package com.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Announcement;
import com.project.service.IAnnouncementService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 竞赛公告管理控制器
 * 处理赛事通知、系统公告的发布、查询、更新及删除
 */
@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    @Autowired
    private IAnnouncementService announcementService;

    /**
     * 分页查询公告列表
     * 
     * @param pageNum  当前页码
     * @param pageSize 每页记录数
     * @return 公告分页数据列表
     */
    @GetMapping("/page")
    public Result<Page<Announcement>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        return Result.success(announcementService.page(page));
    }

    /**
     * 发布/新增一条新公告
     * 
     * @param announcement 公告实体数据
     * @return 操作结果
     */
    @PostMapping
    public Result<?> save(@RequestBody Announcement announcement) {
        return announcementService.save(announcement) ? Result.success() : Result.error("公告保存失败");
    }

    /**
     * 修改/编辑已发布的公告
     * 
     * @param announcement 公告实体数据 (需包含有效 ID)
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@RequestBody Announcement announcement) {
        return announcementService.updateById(announcement) ? Result.success() : Result.error("公告更新失败");
    }

    /**
     * 根据 ID 删除指定的公告
     * 
     * @param id 公告 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        return announcementService.removeById(id) ? Result.success() : Result.error("公告删除失败");
    }
}
