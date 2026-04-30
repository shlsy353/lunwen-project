package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("expense")
public class Expense {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Long teamId;
    private Long leaderId;
    private Long studentId;
    private String itemName;
    private BigDecimal amount;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
}
