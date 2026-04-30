package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("award")
public class Award {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Long teamId;
    private Long studentId;
    private String awardLevel;
    private BigDecimal awardAmount;
    private String awardDetails;
    private String remarks;
    private LocalDateTime createTime;
}
