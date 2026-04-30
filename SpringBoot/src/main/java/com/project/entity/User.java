package com.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 统一存储管理员、教师、领队、学生的基本信息
 */
@Data
@TableName("user")
public class User {
    /** 唯一主键 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名/账号 */
    private String username;

    /** 加密密码 */
    private String password;

    /** 用户姓名 */
    private String name;

    /** 头像链接 */
    private String avatar;

    /** 性别 (1-男, 2-女) */
    private Integer gender;

    /** 手机号 */
    private String phone;

    /** 电子邮件 */
    private String email;

    /** 核心职能角色 (ADMIN, TEACHER, LEADER, STUDENT) */
    private String role;

    /** 学号 (学生角色专用) */
    private String studentId;

    /** 教工号 (教师/领队角色专用) */
    private String teacherId;

    /** 入学年级 (学生角色专用) */
    private String grade;

    /** 所属学院关联 ID */
    private Long collegeId;

    /** 所属专业关联 ID (学生角色专用) */
    private Long majorId;

    /** 研究领域或特长 (领队/教师角色专用的展示内容) */
    private String specialtyField;

    /** 已参加的竞赛历史 (学生端展示) */
    private String participatedCompetitions;

    /** 就读/职读学校名称 */
    private String school;

    /** 用户账号创建时间 */
    private LocalDateTime createTime;

    /** 逻辑删除标记 (0-未删除, 1-已删除) */
    private Integer isDeleted;

    /** 资料最后更新时间 */
    private LocalDateTime updateTime;

    /** 领队教师资格状态 (0-未申请, 1-申请中, 2-已通过审核) */
    private Integer leaderStatus;
}
