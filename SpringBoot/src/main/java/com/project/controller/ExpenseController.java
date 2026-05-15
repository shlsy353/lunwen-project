package com.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.entity.Expense;
import com.project.service.IExpenseService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 报销管理控制器
 * 处理学生/领队老师的竞赛费用报销申请、查询及状态变更
 */
@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

    @Autowired
    private IExpenseService expenseService;

    /**
     * 分页查询报销记录列表
     * 
     * @param pageNum       当前页码
     * @param pageSize      每页记录数
     * @param studentId     申请学生的 ID
     * @param leaderId      关联领队的 ID
     * @param itemName      报销项目名称（模糊查询）
     * @param competitionId 关联竞赛的 ID
     * @param status        报销状态 (0-待处理, 1-已通过, 2-已拒绝, 3-已撤回)
     * @return 报销记录的分页列表
     */
    @GetMapping("/page")
    public Result<Page<Expense>> getPage(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long leaderId,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Integer status) {
        Page<Expense> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Expense> queryWrapper = new QueryWrapper<>();

        if (studentId != null) {
            queryWrapper.eq("student_id", studentId);
        }
        if (leaderId != null) {
            queryWrapper.eq("leader_id", leaderId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (itemName != null && !itemName.isEmpty()) {
            queryWrapper.like("item_name", itemName);
        }
        if (competitionId != null) {
            queryWrapper.eq("competition_id", competitionId);
        }

        queryWrapper.orderByDesc("create_time");
        return Result.success(expenseService.page(page, queryWrapper));
    }

    /**
     * 提交新的报销申请
     * 
     * @param expense 报销实体数据
     * @return 操作结果
     */
    @PostMapping
    public Result<?> save(@RequestBody Expense expense) {
        return expenseService.save(expense) ? Result.success("提交成功") : Result.error("提交失败");
    }

    /**
     * 用户主动撤回尚未处理的报销申请
     * 
     * @param id 报销记录 ID
     * @return 操作结果
     */
    @PutMapping("/{id}/withdraw")
    public Result<?> withdraw(@PathVariable Long id) {
        Expense expense = expenseService.getById(id);
        if (expense == null) {
            return Result.error("报销记录不存在");
        }
        expense.setStatus(3); // 设置状态为：3-已撤回
        return expenseService.updateById(expense) ? Result.success("已撤回") : Result.error("撤回失败");
    }

    /**
     * 更新报销申请的状态 (领队或管理员审核)
     * 
     * @param id     报销记录 ID
     * @param status 目标状态代码
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Expense expense = expenseService.getById(id);
        if (expense == null) {
            return Result.error("报销记录不存在");
        }
        expense.setStatus(status);
        return expenseService.updateById(expense) ? Result.success("处理完成") : Result.error("更新失败");
    }

    /**
     * 更新报销申请
     *
     * @param expense 报销实体数据
     * @return 操作结果
     */
    @PutMapping
    public Result<?> update(@RequestBody Expense expense) {
        return expenseService.updateById(expense) ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除报销申请
     *
     * @param id 报销记录 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return expenseService.removeById(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

}
