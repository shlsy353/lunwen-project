package com.project.controller;

import com.project.entity.Config;
import com.project.service.IConfigService;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置控制器
 * 用于获取核心业务相关的配置参数（如：报名截止开关、规则路径等）
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private IConfigService configService;

    /**
     * 根据配置名称（Key）读取指定的配置项
     * 
     * @param name 配置名称标识
     * @return 包含对应配置数值的实体结果
     */
    @GetMapping("/{name}")
    public Result<Config> getByName(@PathVariable String name) {
        return Result.success(configService.query().eq("name", name).one());
    }
}
