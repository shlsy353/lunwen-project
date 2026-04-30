package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("competition_type")
public class CompetitionType {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String introduction;
    private LocalDateTime createTime;
    private Integer isDeleted;
}
