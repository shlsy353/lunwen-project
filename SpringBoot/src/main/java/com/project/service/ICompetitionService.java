package com.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.project.entity.Competition;

public interface ICompetitionService extends IService<Competition> {
    Page<Competition> getCompetitionPage(Integer pageNum, Integer pageSize, String name, Long typeId, Integer status);
}
