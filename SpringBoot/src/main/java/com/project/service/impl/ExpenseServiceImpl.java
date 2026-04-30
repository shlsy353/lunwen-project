package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.Expense;
import com.project.mapper.ExpenseMapper;
import com.project.service.IExpenseService;
import org.springframework.stereotype.Service;

@Service
public class ExpenseServiceImpl extends ServiceImpl<ExpenseMapper, Expense> implements IExpenseService {
}
