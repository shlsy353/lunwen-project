# 高校学科竞赛管理系统

基于 Spring Boot 3 + Vue 3 + TypeScript 的现代化高校学科竞赛管理系统。

## 技术栈

### 后端
- Spring Boot 3.2.0
- **Backend Service**: Port `2301`
- **Frontend Dev Server**: Port `2302`
- MyBatis Plus 3.5.5
- MySQL 8.0
- JWT 认证
- Hutool 工具库

### 前端
- Vue 3
- TypeScript
- Vite
- Element Plus
- Vue Router 4
- Pinia
- Axios

## 项目结构

```
sunhao/
├── SpringBoot/          # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/project/
│   │       │   ├── entity/      # 实体类
│   │       │   ├── mapper/      # MyBatis Mapper
│   │       │   ├── service/     # 业务逻辑
│   │       │   ├── controller/  # REST API
│   │       │   ├── config/      # 配置类
│   │       │   └── util/        # 工具类
│   │       └── resources/
│   │           ├── application.yml
│   │           └── mapper/      # MyBatis XML
│   └── pom.xml
├── vue/                 # 前端项目
│   ├── src/
│   │   ├── api/         # API 接口
│   │   ├── components/  # 通用组件
│   │   ├── layout/      # 布局组件
│   │   ├── router/      # 路由配置
│   │   ├── stores/      # Pinia 状态管理
│   │   ├── utils/       # 工具函数
│   │   └── views/       # 页面视图
│   └── package.json
└── init.sql             # 数据库初始化脚本
```

## 快速开始

### 数据库配置

1. 确保 MySQL 服务运行在端口 3308
2. 执行数据库初始化脚本：

```bash
mysql -u root -p -P 3308 < init.sql
```

### 后端启动

```bash
cd SpringBoot
mvn clean install
mvn spring-boot:run
```

后端将运行在 http://localhost:2301

### 前端启动

```bash
cd vue
npm install
npm run dev
```

前端将运行在 http://localhost:2302

## 功能模块

### 管理员功能
- **教师管理**：维护教师账号及基本信息
- **学生管理**：维护学生账号及基本信息
- **领队教师管理**：维护领队教师账号及基本信息
- **竞赛类型管理**：按 A/B 类等标准分类竞赛
- **竞赛信息管理**：发布竞赛、审核竞赛申请
- **学院管理**：维护学校学院架构
- **专业管理**：维护学院下属专业
- **获奖情况管理**：统计全校比赛获奖数据
- **系统管理**：公告、资讯及系统基础配置

### 教师功能
- **基础功能**：注册、登录、个人中心
- **教学资源**：题目类型管理、竞赛题库管理
- **竞赛管理**：竞赛类型管理、竞赛信息管理、参赛申请管理
- **成绩与评估**：竞赛评分管理、参赛名单管理、晋级名单管理、获奖名单管理
- **赛后管理**：竞赛总结管理、报销清单管理、成绩申诉管理、参赛信息管理

### 领队教师功能
- **基础功能**：注册、登录、个人中心
- **资源浏览**：题目类型管理、竞赛题库管理
- **参加竞赛**：竞赛类型管理、竞赛信息管理、参赛申请管理、报名信息管理
- **结果跟踪**：竞赛评分管理、参赛名单管理、晋级名单管理、获奖名单管理
- **赛后与统计**：竞赛总结管理、报销清单管理、成绩申诉管理、参赛信息管理、往年成绩管理、获奖情况管理

### 学生功能
- **门户功能**：首页、竞赛信息浏览、竞赛资讯查看
- **个人中心**：注册、登录、个人基本资料维护
- **学习备赛**：竞赛题库练习、竞赛类型了解
- **参赛流程**：竞赛信息管理、报名信息管理、竞赛评分管理、参名单管理
- **赛后反馈**：晋级名单管理、获奖名单管理、竞赛总结管理、报销清单管理、成绩申诉管理
- **数据查询**：参赛信息管理、往年成绩管理、获奖情况管理

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher1 | 123456 |
| 领队教师 | leader1 | 123456 |
| 学生 | student1 | 123456 |

## API 文档

后端 APIBase URL: `http://localhost:2301/api`

主要接口：
- `/api/user/login` - 用户登录
- `/api/user/register` - 用户注册
- `/api/competition/*` - 竞赛相关接口
- `/api/registration/*` - 报名相关接口
- `/api/team/*` - 队伍相关接口
- `/api/score/*` - 评分相关接口

## 开发说明

- 后端包路径：`com.project`
- 数据库名称：`project-sunhao`
- 数据库端口：3308
- 数据库用户：root
- 后端端口：2302
- 前端端口：2302

## License

MIT
