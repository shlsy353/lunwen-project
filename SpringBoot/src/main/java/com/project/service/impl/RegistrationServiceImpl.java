package com.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.Registration;
import com.project.mapper.RegistrationMapper;
import com.project.service.IRegistrationService;
import org.springframework.stereotype.Service;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration>
        implements IRegistrationService {
}
