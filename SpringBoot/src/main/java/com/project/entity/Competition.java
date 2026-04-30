package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("competition")
public class Competition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Long typeId;
    private String purpose;
    private String content;
    private LocalDate competitionDate;
    private String location;
    private String level;
    private String organizer;
    private String undertaker;
    private String image;
    private String file;
    private Integer minPlayers;
    private Integer maxPlayers;
    private Integer status;
    private String namingRule;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
