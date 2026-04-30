package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.StudentLeader;
import com.project.mapper.StudentLeaderMapper;
import com.project.service.IStudentLeaderService;
import org.springframework.stereotype.Service;

@Service
public class StudentLeaderServiceImpl extends ServiceImpl<StudentLeaderMapper, StudentLeader>
        implements IStudentLeaderService {
}
