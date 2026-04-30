package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("score")
public class Score {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Long teamId;
    private Long studentId;
    private Long teacherId;
    private BigDecimal score;
    private String comment;
    private LocalDateTime createTime;
}
