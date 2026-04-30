package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("team")
public class Team {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private String name;
    private Long leaderId;
    private String teacherIds;
    private BigDecimal budget;
    private String projectDescription;
    private String workFile;
    private Integer status;
    private String reviewComment;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
