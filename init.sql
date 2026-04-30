-- Database Initialization Script for College Competition Management System
-- Fixed schema typos and provided complete end-to-end workflow data.

CREATE DATABASE IF NOT EXISTS `project_sunhao` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `project_sunhao`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. User Table
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名/账号',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `name` VARCHAR(50) COMMENT '姓名',
    `avatar` VARCHAR(255) COMMENT '头像',
    `gender` TINYINT DEFAULT 1 COMMENT '性别: 1-男, 2-女',
    `phone` VARCHAR(20) COMMENT '手机',
    `email` VARCHAR(100) COMMENT '邮箱',
    `role` VARCHAR(20) DEFAULT 'STUDENT' COMMENT '角色: ADMIN, TEACHER, LEADER, STUDENT',
    `student_id` VARCHAR(50) COMMENT '学号（学生）',
    `teacher_id` VARCHAR(50) COMMENT '教工号（教师/领队）',
    `grade` VARCHAR(20) COMMENT '年级（学生）',
    `college_id` BIGINT COMMENT '学院ID',
    `major_id` BIGINT COMMENT '专业ID（学生）',
    `specialty_field` TEXT COMMENT '专业领域（领队教师）',
    `participated_competitions` VARCHAR(200) COMMENT '参与竞赛（学生）',
    `school` VARCHAR(100) COMMENT '学校',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `leader_status` TINYINT DEFAULT 0 COMMENT '领队申请状态: 0-未申请, 1-申请中, 2-已通过',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否已删除: 0-未删除, 1-已删除'
) ENGINE=InnoDB COMMENT='用户表';

-- 2. College Table
DROP TABLE IF EXISTS `college`;
CREATE TABLE `college` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '学院名称',
    `introduction` TEXT COMMENT '学院简介',
    `image` VARCHAR(255) COMMENT '图片',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='学院表';

-- 3. Major Table
DROP TABLE IF EXISTS `major`;
CREATE TABLE `major` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `college_id` BIGINT NOT NULL COMMENT '学院ID',
    `name` VARCHAR(100) NOT NULL COMMENT '专业名称',
    `introduction` TEXT COMMENT '专业简介',
    `image` VARCHAR(255) COMMENT '图片',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`college_id`) REFERENCES `college`(`id`)
) ENGINE=InnoDB COMMENT='专业表';

-- 4. Competition Type
DROP TABLE IF EXISTS `competition_type`;
CREATE TABLE `competition_type` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '竞赛类型名称',
    `introduction` TEXT COMMENT '简介',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否已删除: 0-未删除, 1-已删除'
) ENGINE=InnoDB COMMENT='竞赛类型';

-- 5. Competition Information
DROP TABLE IF EXISTS `competition`;
CREATE TABLE `competition` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL COMMENT '竞赛名称',
    `type_id` BIGINT NOT NULL COMMENT '竞赛类型ID',
    `purpose` TEXT COMMENT '竞赛目的',
    `content` TEXT COMMENT '竞赛内容',
    `competition_date` DATE COMMENT '竞赛时间',
    `location` TEXT COMMENT '地点',
    `level` VARCHAR(50) COMMENT '级别: 校级、市级、省级、国家级',
    `organizer` VARCHAR(200) COMMENT '举办单位',
    `undertaker` VARCHAR(200) COMMENT '承办单位',
    `image` VARCHAR(255) COMMENT '图片',
    `file` VARCHAR(255) COMMENT '附件文件',
    `min_players` INT DEFAULT 1 COMMENT '最小组队人数',
    `max_players` INT DEFAULT 1 COMMENT '最大组队人数',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 0-已结束, 1-进行中, 2-未开始',
    `naming_rule` VARCHAR(255) COMMENT '作品文件命名规则',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`type_id`) REFERENCES `competition_type`(`id`)
) ENGINE=InnoDB COMMENT='竞赛信息';

-- 6. Registration Information
DROP TABLE IF EXISTS `registration`;
CREATE TABLE `registration` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `competition_id` BIGINT NOT NULL COMMENT '竞赛ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-通过, 2-拒绝',
    `review_comment` TEXT COMMENT '审核回复',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `leader_id` BIGINT COMMENT '领队教师ID',
    `led_students` TEXT COMMENT '带领学生信息',
    FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB COMMENT='报名信息';

-- 7. Team
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `competition_id` BIGINT NOT NULL COMMENT '竞赛ID',
    `name` VARCHAR(100) NOT NULL COMMENT '队名',
    `leader_id` BIGINT NOT NULL COMMENT '领队教师ID',
    `teacher_ids` VARCHAR(255) COMMENT '其他指导教师ID(逗号分隔)',
    `budget` DECIMAL(10,2) COMMENT '经费预算',
    `project_description` TEXT COMMENT '项目描述',
    `work_file` VARCHAR(255) COMMENT '作品文件',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-通过, 2-拒绝',
    `review_comment` TEXT COMMENT '审核回复',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
    FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB COMMENT='参赛队伍';

-- 8. Team Member
DROP TABLE IF EXISTS `team_member`;
CREATE TABLE `team_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `team_id` BIGINT NOT NULL COMMENT '队伍ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `role` VARCHAR(50) COMMENT '角色: 队长、队员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`team_id`) REFERENCES `team`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB COMMENT='队伍成员';

-- 9. Work Submission
DROP TABLE IF EXISTS `work_submission`;
CREATE TABLE `work_submission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `competition_id` BIGINT NOT NULL COMMENT '竞赛ID',
    `team_id` BIGINT COMMENT '队伍ID',
    `school` VARCHAR(100) COMMENT '学校',
    `work_file` VARCHAR(255) COMMENT '作品文件',
    `work_features` TEXT COMMENT '作品特色',
    `score` DECIMAL(5,2) DEFAULT NULL COMMENT '作品评分',
    `comment` TEXT COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`),
    FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`)
) ENGINE=InnoDB COMMENT='作品提交';

-- 10. Score
DROP TABLE IF EXISTS `score`;
CREATE TABLE `score` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `competition_id` BIGINT NOT NULL COMMENT '竞赛ID',
    `team_id` BIGINT COMMENT '队伍ID',
    `student_id` BIGINT COMMENT '学生ID',
    `teacher_id` BIGINT NOT NULL COMMENT '评分教师ID',
    `score` DECIMAL(5,2) NOT NULL COMMENT '作品评分',
    `comment` TEXT COMMENT '评价内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
    FOREIGN KEY (`teacher_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB COMMENT='竞赛评分';

-- 11. Award
DROP TABLE IF EXISTS `award`;
CREATE TABLE `award` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `competition_id` BIGINT NOT NULL COMMENT '竞赛ID',
    `team_id` BIGINT COMMENT '队伍ID',
    `student_id` BIGINT COMMENT '学生ID',
    `award_level` VARCHAR(50) COMMENT '获奖等级: 一等奖、二等奖、三等奖',
    `award_amount` DECIMAL(10,2) COMMENT '获奖金额',
    `award_details` TEXT COMMENT '获奖明细',
    `remarks` TEXT COMMENT '备注',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`)
) ENGINE=InnoDB COMMENT='获奖记录';

-- 12. Score Appeal
DROP TABLE IF EXISTS `score_appeal`;
CREATE TABLE `score_appeal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `competition_id` BIGINT NOT NULL COMMENT '竞赛ID',
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `score` DECIMAL(5,2) COMMENT '成绩',
    `reason` TEXT COMMENT '申诉原因',
    `status` TINYINT DEFAULT 0 COMMENT '处理状态: 0-待处理, 1-已处理',
    `response` TEXT COMMENT '处理回复',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`),
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`)
) ENGINE=InnoDB COMMENT='成绩申诉';

-- 13. Announcement
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `introduction` TEXT COMMENT '简介',
    `image` VARCHAR(255) COMMENT '图片',
    `content` TEXT NOT NULL COMMENT '内容',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统公告';

-- 14. Config
DROP TABLE IF EXISTS `config`;
CREATE TABLE `config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL COMMENT '配置参数名称',
    `value` VARCHAR(255) COMMENT '配置参数值'
) ENGINE=InnoDB COMMENT='系统配置';

-- 15. Expense Management
DROP TABLE IF EXISTS `expense`;
CREATE TABLE `expense` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `competition_id` BIGINT NOT NULL COMMENT '竞赛ID',
    `team_id` BIGINT DEFAULT NULL COMMENT '队伍ID',
    `leader_id` BIGINT DEFAULT NULL COMMENT '领队教师ID',
    `student_id` BIGINT DEFAULT NULL COMMENT '报销人ID',
    `item_name` VARCHAR(200) COMMENT '报销项名称',
    `amount` DECIMAL(10,2) COMMENT '金额',
    `description` TEXT COMMENT '详情描述',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待审核, 1-通过, 2-拒绝',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`competition_id`) REFERENCES `competition`(`id`)
) ENGINE=InnoDB COMMENT='报销清单';

-- 16. Student-Leader Relation
DROP TABLE IF EXISTS `student_leader`;
CREATE TABLE `student_leader` (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL COMMENT '学生ID',
    `leader_id` BIGINT NOT NULL COMMENT '领队教师ID',
    `status` TINYINT DEFAULT 0 COMMENT '分配状态: 0-待确认, 1-已确认, 2-已拒绝, 3-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`student_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`leader_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='学生-领队关联表';

-- ==========================================================
-- Initial Data Population (Comprehensive Mock Data)
-- ==========================================================

-- 1. Colleges
INSERT INTO `college` (`id`, `name`, `introduction`, `image`) VALUES 
(1, '计算机工程学院', '致力于培养高素质计算机专业人才，拥有一流的师资力量和实验设备。', 'https://img.js.design/assets/placeholder/df661bda-0524-4e44-998a-7ed8a02d2fc5.png'),
(2, '软件学院', '专注于软件工程教育，培养具有创新精神的软件开发人才。', 'https://img.js.design/assets/placeholder/df661bda-0524-4e44-998a-7ed8a02d2fc5.png'),
(3, '信息工程学院', '涵盖通信、电子、自动化等多个专业方向。', 'https://img.js.design/assets/placeholder/df661bda-0524-4e44-998a-7ed8a02d2fc5.png');

-- 2. Majors
INSERT INTO `major` (`id`, `college_id`, `name`, `introduction`, `image`) VALUES 
(1, 1, '计算机科学与技术', '培养计算机科学理论与应用技术人才', 'https://img.js.design/assets/placeholder/df661bda-0524-4e44-998a-7ed8a02d2fc5.png'),
(2, 2, '软件工程', '培养软件设计、开发、测试等全方位人才', 'https://img.js.design/assets/placeholder/df661bda-0524-4e44-998a-7ed8a02d2fc5.png'),
(3, 1, '网络工程', '培养网络规划、设计、管理人才', 'https://img.js.design/assets/placeholder/df661bda-0524-4e44-998a-7ed8a02d2fc5.png'),
(4, 3, '物联网工程', '培养物联网系统设计与开发人才', 'https://img.js.design/assets/placeholder/df661bda-0524-4e44-998a-7ed8a02d2fc5.png');

-- 3. Users (Password: 123456)
INSERT INTO `user` (`id`, `username`, `password`, `name`, `role`, `avatar`, `gender`, `phone`, `email`, `student_id`, `teacher_id`, `grade`, `college_id`, `major_id`, `leader_status`) VALUES 
(1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 'ADMIN', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 1, '13800000000', 'admin@example.com', NULL, 'A001', NULL, 1, NULL, 0),
(2, 'teacher1', 'e10adc3949ba59abbe56e057f20f883e', '孙教授', 'LEADER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sun', 1, '13811110001', 'sun@school.edu.cn', NULL, 'T001', NULL, 1, NULL, 2),
(3, 'teacher2', 'e10adc3949ba59abbe56e057f20f883e', '周老师', 'TEACHER', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhou', 2, '13811110002', 'zhou@school.edu.cn', NULL, 'T002', NULL, 2, NULL, 1),
(4, 'student1', 'e10adc3949ba59abbe56e057f20f883e', '张心心', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhang', 2, '13822220001', 'zhang@stu.edu.cn', '20220801', NULL, '2022级', 1, 1, 0),
(5, 'student2', 'e10adc3949ba59abbe56e057f20f883e', '李小龙', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=li', 1, '13822220002', 'li@stu.edu.cn', '20220802', NULL, '2022级', 1, 1, 0),
(6, 'student3', 'e10adc3949ba59abbe56e057f20f883e', '王五', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=wang', 1, '13822220003', 'wang@stu.edu.cn', '20220803', NULL, '2022级', 2, 2, 0),
(7, 'student4', 'e10adc3949ba59abbe56e057f20f883e', '赵六', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=zhao', 2, '13822220004', 'zhao@stu.edu.cn', '20220804', NULL, '2022级', 2, 2, 0),
(8, 'student5', 'e10adc3949ba59abbe56e057f20f883e', '刘七', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=liu', 1, '13822220005', 'liu@stu.edu.cn', '20220805', NULL, '2022级', 1, 3, 0),
(9, 'student6', 'e10adc3949ba59abbe56e057f20f883e', '孙八', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=sun8', 1, '13822220006', 'sun@stu.edu.cn', '20220806', NULL, '2022级', 3, 4, 0);

-- Bulk Students (starting from ID 10)
INSERT INTO `user` (`id`, `username`, `password`, `name`, `role`, `avatar`, `gender`, `phone`, `email`, `student_id`, `grade`, `college_id`, `major_id`) VALUES 
(10, 'stu10', 'e10adc3949ba59abbe56e057f20f883e', '陈一', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=10', 1, '1390001', '10@ex.com', 'S010', '2023级', 1, 1),
(11, 'stu11', 'e10adc3949ba59abbe56e057f20f883e', '吴二', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=11', 2, '1390002', '11@ex.com', 'S011', '2023级', 1, 2),
(12, 'stu12', 'e10adc3949ba59abbe56e057f20f883e', '郑三', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=12', 1, '1390003', '12@ex.com', 'S012', '2023级', 2, 2),
(13, 'stu13', 'e10adc3949ba59abbe56e057f20f883e', '冯四', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=13', 2, '1390004', '13@ex.com', 'S013', '2023级', 2, 2),
(14, 'stu14', 'e10adc3949ba59abbe56e057f20f883e', '陈五', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=14', 1, '1390005', '14@ex.com', 'S014', '2023级', 3, 4),
(15, 'stu15', 'e10adc3949ba59abbe56e057f20f883e', '褚六', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=15', 2, '1390006', '15@ex.com', 'S015', '2023级', 3, 4),
(16, 'stu16', 'e10adc3949ba59abbe56e057f20f883e', '卫七', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=16', 1, '1390007', '16@ex.com', 'S016', '2022级', 1, 3),
(17, 'stu17', 'e10adc3949ba59abbe56e057f20f883e', '蒋八', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=17', 2, '1390008', '17@ex.com', 'S017', '2022级', 1, 3),
(18, 'stu18', 'e10adc3949ba59abbe56e057f20f883e', '沈九', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=18', 1, '1390009', '18@ex.com', 'S018', '2022级', 2, 2),
(19, 'stu19', 'e10adc3949ba59abbe56e057f20f883e', '韩十', 'STUDENT', 'https://api.dicebear.com/7.x/avataaars/svg?seed=19', 2, '1390010', '19@ex.com', 'S019', '2022级', 2, 2);

-- 4. Competition Types
INSERT INTO `competition_type` (`id`, `name`, `introduction`) VALUES 
(1, '程序设计竞赛', '考察编程能力和算法设计能力的竞赛'),
(2, '数学建模竞赛', '运用数学方法解决实际问题的竞赛'),
(3, '创新创业竞赛', '鼓励学生创新创业的综合性竞赛'),
(4, '电子设计竞赛', '考察电子电路设计与实现能力的竞赛'),
(5, '机器人竞赛', '机器人设计、制作与控制的竞赛');

-- 5. Competitions
INSERT INTO `competition` (`id`, `name`, `type_id`, `purpose`, `content`, `competition_date`, `location`, `level`, `organizer`, `undertaker`, `image`, `file`, `status`, `naming_rule`) VALUES 
(1, 'ACM程序设计大赛', 1, '旨在培养大学生算法设计、逻辑推演与高强度编程实现能力。', '本届ACM校赛采用ICPC国际标准...', '2026-05-15', '学校体育馆/机房A栋', '省级', '省计算机学会', '计算机工程学院', 'https://images.unsplash.com/photo-1517694712202-14dd9538aa97', '', 1, '学号_姓名_ACM.zip'),
(2, '全国大学生数学建模竞赛', 2, '培养学生利用数学工具解决社会热点。', '竞赛题目通常来源于工程技术...', '2026-09-10', '线上提交', '国家级', '中国工业与应用数学学会', '理学院', 'https://images.unsplash.com/photo-1509228468518-180dd48a5793', '', 1, '姓名_论文题目.pdf'),
(3, '“互联网+”大学生创新创业大赛', 3, '深化高等教育综合改革，激发大学生的创造力。', '作为覆盖面最广赛事...', '2026-06-20', '创意园区礼堂', '校级', '教务处', '创新创业教育学院', 'https://images.unsplash.com/photo-1519389950473-47ba0277781c', '', 1, '姓名_项目负责人.pdf'),
(4, '电子设计竞赛', 4, '引导学生通过动手实践提升电路设计。', '竞赛采取“半封闭式”模式...', '2026-07-01', '电工电子实验中心', '省级', '电子学会', '信息工程学院', 'https://images.unsplash.com/photo-1517077304055-6e89abbf09b0', '', 1, '学号_作品名称_源码.zip'),
(5, '人工智能挑战杯', 1, '推动人工智能前沿技术在校园内的普及。', '挑战杯聚焦计算机视觉...', '2026-08-15', 'AI科研大楼', '国家级', '中国人工智能学会', '计算机学院', 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e', '', 1, '姓名_模型说明.docx'),
(6, '机器人创意设计大赛', 5, '机器人设计、制作与控制。', '展示各类服务型、工业型机器人...', '2026-04-10', '工程实训中心', '省级', '机器人协会', '机械工程学院', 'https://images.unsplash.com/photo-1531746790731-6c4fd25081b2', '', 1, '队伍名_机器人.zip');

-- 6. Student-Leader Relations (Associate with Teacher1 - Sun Professor)
INSERT INTO `student_leader` (`student_id`, `leader_id`, `status`) VALUES 
(4, 2, 1), (5, 2, 1), (6, 2, 1), (7, 2, 1), (8, 2, 1), (9, 2, 1),
(10, 2, 1), (11, 2, 1), (12, 2, 1), (13, 2, 1), (14, 2, 1), (15, 2, 1),
(16, 2, 1), (17, 2, 1), (18, 2, 1), (19, 2, 1);

-- 7. Registrations
-- Leader Teacher (Sun - ID 2) registers students to various competitions
INSERT INTO `registration` (`competition_id`, `student_id`, `leader_id`, `status`, `led_students`) VALUES 
(1, 4, 2, 1, '张心心, 李小龙'), (1, 5, 2, 1, '李小龙, 张心心'), (1, 10, 2, 1, '陈一'),
(2, 4, 2, 1, '张心心'), (2, 11, 2, 1, '吴二'), (2, 12, 2, 0, '郑三'),
(3, 6, 2, 1, '王五, 赵六'), (3, 7, 2, 1, '赵六, 王五'), (3, 13, 2, 1, '冯四'),
(4, 8, 2, 1, '刘七'), (4, 14, 2, 1, '陈五'), (4, 15, 2, 1, '褚六'),
(5, 9, 2, 1, '孙八'), (5, 16, 2, 1, '卫七'), (5, 17, 2, 1, '蒋八'),
(6, 18, 2, 1, '沈九'), (6, 19, 2, 1, '韩十'), (6, 4, 2, 1, '张心心');

-- 8. Work Submissions
-- Integrated with copied images in public folders
INSERT INTO `work_submission` (`student_id`, `competition_id`, `school`, `work_file`, `work_features`, `score`, `comment`) VALUES 
(4, 1, '计算机大学', '/1/103-1.jpg', '基于深度优先搜索的最优解法', 95.0, '算法复杂度控制极佳'),
(5, 1, '计算机大学', '/1/103-2.jpg', '动态规划优化版本', 88.0, '边界条件处理得当'),
(10, 1, '计算机大学', '/1/103-3.jpg', '贪心策略实现', 85.0, '思路清晰'),
(4, 2, '计算机大学', '/2/103-4.jpg', '人口预测数学模型', 92.5, '模型稳定性高'),
(11, 2, '计算机大学', '/1/103-5.jpg', '供需平衡优化策略', 86.0, '计算过程详实'),
(6, 3, '计算机大学', '/3/103-6.jpg', '智能导游APP策划书', 90.0, '商业逻辑闭环'),
(13, 3, '计算机大学', '/3/103-7.jpg', '校园二手交易平台', 82.0, '技术栈先进'),
(8, 4, '计算机大学', '/4/103-8.jpg', '低功耗环境监测设备', 94.0, '硬件功耗极优'),
(14, 4, '计算机大学', '/4/103-9.jpg', '智能温湿度调节器', 89.0, '响应延迟低'),
(9, 5, '计算机大学', '/5/103-10.jpg', '复杂背景下的字符识别', 91.0, '识别率达99.8%'),
(16, 5, '计算机大学', '/5/103-11.jpg', '人群密集度检测系统', 87.0, '模型精简有效'),
(18, 6, '计算机大学', '/6/103-12.jpg', '避障循迹机器人设计', 93.0, '机械结构精巧'),
(19, 6, '计算机大学', '/6/103-13.jpg', '语音控制分拣机械臂', 88.5, '控制算法稳定');

-- 9. Scores (Teacher1 - ID 2 scores his led students)
INSERT INTO `score` (`competition_id`, `student_id`, `teacher_id`, `score`, `comment`) VALUES 
(1, 4, 2, 95.0, '表现极其出色'),
(1, 5, 2, 88.0, '逻辑缜密'),
(2, 4, 2, 92.5, '数学功底夯实'),
(3, 6, 2, 90.0, '很有市场潜力'),
(4, 8, 2, 94.0, '电子设计能力强'),
(5, 9, 2, 91.0, 'AI算法应用成熟'),
(6, 18, 2, 93.0, '硬件制作精细');

-- 10. Awards
INSERT INTO `award` (`competition_id`, `student_id`, `award_level`, `award_amount`, `award_details`) VALUES 
(1, 4, '一等奖', 5000.00, '程序设计大赛冠军'),
(1, 5, '二等奖', 3000.00, '总分第三名'),
(2, 4, '一等奖', 4000.00, '模型创新奖'),
(4, 8, '一等奖', 4500.00, '全场最佳硬件奖');

-- 11. Expenses
INSERT INTO `expense` (`competition_id`, `student_id`, `leader_id`, `item_name`, `amount`, `description`, `status`) VALUES 
(1, 4, 2, '往返差旅费', 840.50, 'ACM省赛交通及住宿费', 1),
(1, 5, 2, '打印费', 56.00, '参赛资料打印', 1),
(2, 4, 2, '软件购买', 299.00, 'Matlab工具包订阅', 1),
(4, 8, 2, '电子元器件', 1250.00, 'FPGA开发板及传感器套件', 0),
(6, 18, 2, '耗材采购', 450.00, '机器人结构件及电机', 0);

-- 12. Appeals
INSERT INTO `score_appeal` (`competition_id`, `student_id`, `score`, `reason`, `status`, `response`) VALUES 
(3, 7, 72.0, '认为专家评审对商业模式的理解有误，希望能二审。', 0, NULL),
(5, 17, 78.0, '评分标准未对轻量化模型给予足够权重。', 0, NULL),
(1, 10, 85.0, '由于系统原因部分提交记录延时。', 1, '经查属实，已恢复成绩。');

-- 13. Announcements
INSERT INTO `announcement` (`title`, `introduction`, `content`) VALUES 
('2026年度学科竞赛指导计划', '助力每一位怀揣梦想的学子。', '本年度将重点投入ACM、互联网+及电赛。'),
('关于加强竞赛报销规范的通知', '请各位领队教师注意。', '所有的差旅报销需附带完整的电子发票凭证。'),
('恭喜孙教授团队荣获国奖', '学院荣誉墙更新。', '我院孙教授指导的张心心团队荣获数学建模国赛一等奖！');

-- 14. Config
INSERT INTO `config` (`name`, `value`) VALUES 
('system_name', '竞赛管理云平台'),
('support_contact', 'tech@support.edu.cn');

SET FOREIGN_KEY_CHECKS = 1;
