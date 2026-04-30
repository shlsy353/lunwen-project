package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score_appeal")
public class ScoreAppeal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Long studentId;
    private BigDecimal score;
    private String reason;
    private Integer status;
    private String response;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
