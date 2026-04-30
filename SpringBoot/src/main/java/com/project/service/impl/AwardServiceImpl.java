package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.Award;
import com.project.mapper.AwardMapper;
import com.project.service.IAwardService;
import org.springframework.stereotype.Service;

@Service
public class AwardServiceImpl extends ServiceImpl<AwardMapper, Award> implements IAwardService {
}
