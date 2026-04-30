package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.CompetitionType;
import com.project.mapper.CompetitionTypeMapper;
import com.project.service.ICompetitionTypeService;
import org.springframework.stereotype.Service;

@Service
public class CompetitionTypeServiceImpl extends ServiceImpl<CompetitionTypeMapper, CompetitionType>
        implements ICompetitionTypeService {
}
