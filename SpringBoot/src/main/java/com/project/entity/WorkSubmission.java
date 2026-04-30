package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work_submission")
public class WorkSubmission {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long studentId;
    private Long competitionId;
    private Long teamId;
    private String school;
    private String workFile;
    private String workFeatures;
    private Double score;
    private String comment;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
