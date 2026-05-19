# 智能宠物管理系统 · 后端

Spring Boot 3.3 REST API 服务，提供用户认证、宠物档案、领养/救助、寻宠、论坛、实时定位、AI 品种识别转发、PetFace 2.0 个体识别与 AI 寻宠闭环接口。

## 技术栈

- **Spring Boot 3.3** · Spring MVC · Spring Validation
- **MyBatis 3.0** · MySQL 8.0
- **JWT** 用户鉴权
- **Knife4j / SpringDoc** OpenAPI 接口文档
- **Lombok** · SLF4J
- **JDK 21**
- **Python FastAPI AI 服务**：品种识别 1.0/1.1 与 PetFace-ID 2.0

## 本地运行

```bash
# 1. 初始化数据库
mysql -h127.0.0.1 -P3306 -uroot -p123456 < init.sql
mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/20260519_petface_closed_loop.sql

# 2. 启动 Python AI 服务
cd ../ai_recognition
conda activate pet
export KMP_DUPLICATE_LIB_OK=TRUE
PET_BREED_MODEL_DIR=models_extended python inference_server.py

# 3. 启动后端
cd ../backEnd
mvn spring-boot:run
```

服务地址：

```text
API 服务：http://localhost:8080
接口文档：http://localhost:8080/swagger-ui/index.html
AI 服务：http://localhost:8000
```

## 配置说明

配置文件：`src/main/resources/application.yml`

| 配置项 | 默认值 | 说明 |
| --- | --- | --- |
| `server.port` | `8080` | 后端服务端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/petSql...` | 数据库地址 |
| `spring.datasource.username` | `root` | 数据库用户名 |
| `spring.datasource.password` | `123456` | 数据库密码 |
| `file.upload-path` | `${user.dir}/uploads` | 图片上传本地目录 |
| `file.access-path` | `/upload/**` | 静态图片访问映射 |
| `ai.base-url` | `http://localhost:8000` | Python AI 服务基础地址 |
| `ai.recognize-url` | `http://localhost:8000/api/recognize` | 品种识别接口 |
| `ai.petface-embed-url` | `http://localhost:8000/api/petface/embed` | PetFace 特征提取接口 |
| `ai.petface-verify-url` | `http://localhost:8000/api/petface/verify` | PetFace 同宠验证接口 |

图片访问示例：

```text
http://localhost:8080/upload/images/demo_petface_xxx_profile_1.png
```

## 核心功能闭环

### AI 寻宠闭环

```text
宠物建档 -> 上传多张宠物照片 -> 后端调用 PetFace 提取个体特征
-> 特征写入 pet_face_embedding -> 用户上传寻宠图片
-> 后端检索相似宠物 -> 返回宠物档案与主人联系方式
-> 发布寻宠启事后保存匹配记录
```

当前支持：

- 每只宠物多图建档
- 多图 embedding 融合
- 检索时使用单图最大相似度与融合特征兜底
- 寻宠帖子保存匹配记录
- 演示数据：60 只 demo 宠物，猫狗各 30，只需恢复备份即可复现

## 接口总览

| 模块 | 方法 | 路径 | 说明 | 认证 |
| --- | --- | --- | --- | --- |
| 用户 | POST | `/api/user/register` | 注册 | 否 |
| 用户 | POST | `/api/user/login` | 登录，返回 JWT | 否 |
| 宠物 | POST | `/api/pet/create` | 创建宠物档案 | 是 |
| 宠物 | GET | `/api/pet/list` | 我的宠物列表 | 是 |
| 宠物 | GET | `/api/pet/{id}` | 宠物详情 | 是 |
| 宠物 | PUT | `/api/pet/{id}` | 更新宠物信息 | 是 |
| 宠物 | DELETE | `/api/pet/{id}` | 删除宠物档案 | 是 |
| 宠物 | GET | `/api/pet/chip/{chipNumber}` | 芯片号查询 | 否 |
| 宠物档案 | GET | `/api/pet/directory` | 宠物档案大厅 | 否 |
| PetFace | POST | `/api/pet/{petId}/face/rebuild` | 重建宠物个体特征 | 是 |
| PetFace | GET | `/api/pet/{petId}/face/status` | 查询个体特征状态 | 是 |
| PetFace | POST | `/api/ai/petface/verify` | 两张图片同宠验证 | 否 |
| PetFace | POST | `/api/ai/petface/search` | 上传图片检索相似宠物 | 否 |
| PetFace | POST | `/api/lost/{lostPostId}/petface/matches` | 保存寻宠匹配结果 | 是 |
| PetFace | GET | `/api/lost/{lostPostId}/petface/matches` | 查询寻宠匹配结果 | 否 |
| AI识别 | POST | `/api/ai/recognize` | 调用 Python 服务识别宠物品种 | 否 |
| 领养 | POST | `/api/adoption/create` | 发布领养帖 | 是 |
| 领养 | GET | `/api/adoption/list` | 领养帖列表 | 否 |
| 寻宠 | POST | `/api/lost/create` | 发布寻宠帖 | 是 |
| 寻宠 | GET | `/api/lost/list` | 寻宠帖列表 | 否 |
| 上传 | POST | `/api/upload/image` | 上传单张图片 | 否 |
| 上传 | POST | `/api/upload/images` | 上传多张图片 | 否 |
| 定位 | GET/POST | `/api/location/*` | 宠物定位与轨迹 | 是 |

需要认证的接口请在请求头加：

```text
Authorization: Bearer <token>
```

## 数据库

数据库名：`petSql`。

初始化：

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 < init.sql
```

PetFace 2.0 增量：

```bash
mysql -h127.0.0.1 -P3306 -uroot -p123456 < sql/20260519_petface_closed_loop.sql
```

主要数据表：

| 表名 | 说明 |
| --- | --- |
| `User` | 用户账号 |
| `Pet` | 宠物档案 |
| `Image` | 帖子/宠物图片 |
| `PostAdoption` | 领养/救助帖子 |
| `PostLost` | 寻宠启事 |
| `pet_face_embedding` | PetFace 2.0 宠物个体特征 |
| `pet_match_record` | 寻宠相似匹配记录 |
| `pet_location` | 宠物定位记录 |
| `ForumPost` / `Comment` | 论坛帖子与评论 |
| `favorites` | 收藏记录 |
| `notifications` | 消息通知 |

## Demo 数据与部署资产

Git 仓库只保存代码、SQL 和说明文档，不保存模型、图片和数据库备份。部署/演示需要额外传输资产包：

```text
deploy_packages/pets_deploy_assets_20260519.zip
```

资产包内容：

- `ai_recognition/models_extended/`：1.1 增强版品种识别模型
- `ai_recognition/models/petface/`：PetFace-ID 2.0 模型
- `backEnd/uploads/`：60 条 demo 宠物建档图与推荐测试图
- `backEnd/backups/petface_demo_60_current_20260519_214639/petSql_full.sql`：60 条 demo 数据库备份
- `backEnd/sql/`：数据库增量 SQL
- `backEnd/docs/部署资产同步说明_20260519.md`：完整部署说明

完整说明见：

```text
docs/部署资产同步说明_20260519.md
```

## 项目结构

```text
src/main/java/ynu/pet/
├── controller/     # REST 接口层
├── service/        # 业务逻辑层
│   └── impl/
├── mapper/         # MyBatis Mapper
├── entity/         # 数据库实体
├── dto/            # 请求/响应 DTO
├── config/         # CORS、Swagger、MVC 静态资源映射
├── interceptor/    # JWT 拦截器
├── exception/      # 全局异常处理
└── utils/          # JWT、文件上传、密码等工具
```

## 已完成能力

- [x] 用户注册 / 登录 / JWT 鉴权
- [x] 宠物档案 CRUD
- [x] 宠物多图建档
- [x] PetFace 2.0 个体特征建立
- [x] AI 寻宠相似检索
- [x] 寻宠匹配记录保存与查询
- [x] AI 品种识别服务转发
- [x] 领养/救助帖子
- [x] 寻宠启事
- [x] 图片上传与静态资源访问
- [x] 宠物定位与轨迹记录
- [x] 全局异常处理
- [x] OpenAPI 接口文档
- [x] CORS 跨域配置

