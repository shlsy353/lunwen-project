package com.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.Competition;
import com.project.mapper.CompetitionMapper;
import com.project.service.ICompetitionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements ICompetitionService {

    @Override
    public Page<Competition> getCompetitionPage(Integer pageNum, Integer pageSize, String name, Long typeId,
            Integer status) {
        Page<Competition> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Competition> queryWrapper = new QueryWrapper<>();

        if (StringUtils.hasText(name)) {
            queryWrapper.like("name", name);
        }

        if (typeId != null) {
            queryWrapper.eq("type_id", typeId);
        }

        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.orderByDesc("create_time");
        return this.page(page, queryWrapper);
    }
}
