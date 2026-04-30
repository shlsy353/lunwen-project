package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_leader")
public class StudentLeader {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long leaderId;
    private Integer status; // 0-Pending, 1-Approved, 2-Rejected
    private LocalDateTime createTime;
}
