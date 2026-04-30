package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.WorkSubmission;
import com.project.mapper.WorkSubmissionMapper;
import com.project.service.IWorkSubmissionService;
import org.springframework.stereotype.Service;

@Service
public class WorkSubmissionServiceImpl extends ServiceImpl<WorkSubmissionMapper, WorkSubmission>
        implements IWorkSubmissionService {
}
