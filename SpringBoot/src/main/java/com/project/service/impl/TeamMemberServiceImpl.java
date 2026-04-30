package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.TeamMember;
import com.project.mapper.TeamMemberMapper;
import com.project.service.ITeamMemberService;
import org.springframework.stereotype.Service;

@Service
public class TeamMemberServiceImpl extends ServiceImpl<TeamMemberMapper, TeamMember> implements ITeamMemberService {
}
