# veteran-star-platform · 戎归·星辉

> 退役大学生综合管理平台 —— 一套积分制的全栈 Web 服务系统。

面向高校退役大学生群体，围绕 **档案采集 → 审核认定 → 积分核发 → 荣誉排行 → 成长服务** 业务主线，提供学生档案管理、服役/受奖/证书审核、自动积分、排行榜、就业机会发布与报名、简历在线生成、AI 个人画像等能力。包含学生端（移动 H5）、管理端（Web）、后端 API 与微信小程序四端。

## 功能特性

- **学生端（Vue 3 + Vant 4）**：注册登录、档案填报、服役经历/受奖/技能证书维护、机会广场报名与收藏、我的积分与积分排行、简历在线制作与 PDF 下载、AI 个人画像、系统设置
- **管理端（Vue 3 + Element Plus）**：数据看板、学生管理、档案材料审核（审核通过自动核发积分）、积分调整、机会与公告发布、报名审核、系统参数与数据字典维护
- **后端（Spring Boot 3.2）**：RESTful API（统一 /api 前缀）、JWT + Redis 白名单认证、AES 敏感字段加密、接口限流防护、Knife4j 接口文档
- **微信小程序**：复用后端接口的轻量访问入口

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Java 17 · Spring Boot 3.2 · MyBatis-Plus · Spring Security + JWT |
| 存储 | MySQL 8.0 · Redis 7 |
| 学生端 | Vue 3 · Vant 4 · Pinia · Vite |
| 管理端 | Vue 3 · Element Plus · ECharts · Vite |
| 小程序 | 微信原生小程序 |
| 部署 | Docker Compose · Nginx · 阿里云短信（Mock 降级） |

## 目录结构

```
veteran-star-platform/
├── veteran-server/          # 后端服务（Spring Boot，端口 8080）
├── veteran-mobile/          # 学生端移动网页（开发端口 3001）
├── veteran-admin/           # 管理端 Web（开发端口 3000）
├── deploy/                  # 生产部署（Docker Compose + Nginx）
└── soldier.sql              # 数据库初始化脚本（建表 + 字典 + 演示数据）
```

## 快速开始（本地开发）

**环境要求**：JDK 17、Maven 3.6+、Node.js 18+、MySQL 8.0、Redis

```bash
# 1. 初始化数据库（默认库名 soldier，账号 root/123456）
mysql -uroot -p123456 < soldier.sql

# 2. 启动后端（8080，需先启动本机 MySQL 与 Redis）
cd veteran-server
mvn spring-boot:run

# 3. 启动学生端（3001，/api、/uploads 已代理到 8080）
cd veteran-mobile
npm install && npm run dev

# 4. 启动管理端（3000）
cd veteran-admin
npm install && npm run dev
```

- 接口文档：`http://localhost:8080/doc.html`
- 健康检查：`http://localhost:8080/actuator/health`

## 生产部署

生产环境由 4 个容器组成（mysql / redis / app / nginx），对外 80 端口，Nginx 按设备类型分流学生端与管理端，并反向代理 `/api/**`、直出 `/uploads/**` 静态文件。

```bash
# 本地构建后端 jar 与前端 dist，放入 deploy/ 目录后上传服务器
cd veteran-server && mvn clean package -DskipTests
cd veteran-admin && npm run build
cd veteran-mobile && npm run build

# 服务器上（已装 Docker 与 compose 插件）
cp .env.example .env   # 修改 DB_ROOT_PASSWORD / JWT_SECRET / AES_KEY 等
./deploy.sh
```

> 注意：`.env` 中 `AES_KEY` / `AES_IV` 必须与本地开发环境一致，否则本地注册学生的加密手机号无法在生产解密。

## 安全设计

- JWT 认证 + Redis 白名单（登出即失效），管理员与学生令牌隔离
- 学生敏感字段（手机号）AES 加密存储
- 提交类接口 60 秒限次防护 + 全局限流
- 上传文件类型与大小校验（2MB/5MB）

## 免责声明

- `deploy/docker-compose.yml` 中的 `APP_BASE_URL`、`.env` 等为部署占位示例，实际生产配置请自行替换
- 仓库内数据库脚本仅含演示/字典数据，不含任何真实用户数据

## License

仅用于学习交流，请勿直接用于商业用途。
