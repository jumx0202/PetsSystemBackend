# 智能宠物管理系统 · 后端

Spring Boot 3.3 REST API 服务，提供宠物管理、用户认证、帖子管理等核心接口。

## 技术栈

- **Spring Boot 3.3** · Spring MVC · Spring Validation
- **MyBatis 3.0** · MySQL 8.0
- **JWT**（jjwt 0.12）用户鉴权
- **Knife4j / SpringDoc**  接口文档（OpenAPI 3）
- **Lombok** · SLF4J
- **JDK 21**

## 本地运行

```bash
# 1. 先启动数据库（在项目根目录）
docker compose up -d

# 2. 启动后端
mvn spring-boot:run
```

- API 服务：`http://localhost:8080`
- 接口文档：`http://localhost:8080/swagger-ui/index.html`

## 配置说明

配置文件：`src/main/resources/application.yml`

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | 8080 | 服务端口 |
| `spring.datasource.url` | `localhost:3306/petSql` | 数据库地址 |
| `spring.datasource.username` | root | 数据库用户名 |
| `spring.datasource.password` | 123456 | 数据库密码 |
| `upload.path` | `/Users/org/pet-uploads` | 图片上传本地路径 |
| `upload.url-prefix` | `http://localhost:8080/upload` | 图片访问前缀 |

## 接口总览

| 模块 | 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|------|
| 用户 | POST | `/api/user/register` | 注册 | 否 |
| 用户 | POST | `/api/user/login` | 登录，返回 JWT | 否 |
| 宠物 | POST | `/api/pet/create` | 创建宠物档案 | 是 |
| 宠物 | GET | `/api/pet/list` | 我的宠物列表 | 是 |
| 宠物 | GET | `/api/pet/{id}` | 宠物详情 | 是 |
| 宠物 | PUT | `/api/pet/{id}` | 更新宠物信息 | 是 |
| 宠物 | DELETE | `/api/pet/{id}` | 删除宠物档案 | 是 |
| 宠物 | GET | `/api/pet/chip/{chipNumber}` | 芯片号查询 | 否 |
| 领养 | POST | `/api/adoption/create` | 发布领养帖 | 是 |
| 领养 | GET | `/api/adoption/list` | 领养帖列表 | 否 |
| 寻宠 | POST | `/api/lost/create` | 发布寻宠帖 | 是 |
| 寻宠 | GET | `/api/lost/list` | 寻宠帖列表 | 否 |
| 上传 | POST | `/api/upload/image` | 上传单张图片 | 否 |
| 上传 | POST | `/api/upload/images` | 上传多张图片 | 否 |
| 行为记录 | GET/POST | `/api/behavior/*` | 行为监测记录 | 是 |

> 需要认证的接口请在请求头加：`Authorization: Bearer <token>`

## 数据库

数据库名：`petSql`，初始化脚本：`init.sql`

如果是首次启动 Docker 数据库，`init.sql` 会自动创建正确表结构。若本地已经用旧脚本创建过数据库，请执行一次 `fix_pet_table.sql`，用于修正 `Pet` 表中的 `pet_type`、`gender`、`avatar` 字段。

主要数据表：

| 表名 | 说明 |
|------|------|
| `User` | 用户账号 |
| `Pet` | 宠物档案 |
| `PostAdoption` | 领养帖子 |
| `PostLost` | 寻宠帖子 |
| `Image` | 帖子/宠物图片 |
| `behavior_models` | AI 模型记录 |
| `behavior_logs` | 行为监测日志 |
| `behavior_daily_stats` | 行为统计日报 |
| `favorites` | 收藏记录 |
| `notifications` | 消息通知 |

## 项目结构

```
src/main/java/ynu/pet/
├── controller/     # REST 接口层
├── service/        # 业务逻辑层
│   └── impl/
├── mapper/         # MyBatis Mapper
├── entity/         # 数据库实体
├── dto/            # 请求/响应 DTO
├── config/         # 配置类（CORS、Swagger、MVC）
├── interceptor/    # JWT 拦截器
├── exception/      # 全局异常处理
└── utils/          # 工具类（JWT、文件上传、密码）
```

## 开发进度

### ✅ 已完成

- [x] 用户注册 / 登录 / JWT 鉴权拦截器
- [x] 宠物档案 CRUD 接口
- [x] 领养帖子 CRUD 接口
- [x] 寻宠帖子 CRUD 接口
- [x] 图片上传（本地磁盘存储 + 静态资源映射）
- [x] 行为监测记录接口（基础版）
- [x] 全局异常处理
- [x] OpenAPI 接口文档（Knife4j）
- [x] CORS 跨域配置

### 📋 待完成

- [ ] AI 品种识别接口（`/api/ai/recognize`，转发至 Python 推理服务）
- [ ] AI 行为分析接口（`/api/ai/behavior`）
- [ ] 收藏 / 关注接口
- [ ] 消息通知接口
- [ ] 健康管理接口（体重记录、疫苗提醒）
