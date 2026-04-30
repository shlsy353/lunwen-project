package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("registration")
public class Registration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Long studentId;
    private Integer status;
    private String reviewComment;
    private Long leaderId;
    private String ledStudents;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
