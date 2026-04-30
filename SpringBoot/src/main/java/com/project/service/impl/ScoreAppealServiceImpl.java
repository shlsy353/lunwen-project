package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.ScoreAppeal;
import com.project.mapper.ScoreAppealMapper;
import com.project.service.IScoreAppealService;
import org.springframework.stereotype.Service;

@Service
public class ScoreAppealServiceImpl extends ServiceImpl<ScoreAppealMapper, ScoreAppeal> implements IScoreAppealService {
}
