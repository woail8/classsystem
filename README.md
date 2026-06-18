# 校园在线学习平台

一个面向局域网场景的校园在线学习平台，支持课程与班级管理、签到、作业、考试、资料库、通知等核心教学功能。

项目采用“前后端分离开发、Spring Boot 统一托管部署”的 C/S 模式：

- 开发阶段：前端通过 Vite 运行，接口代理到后端
- 部署阶段：前端构建产物同步到 Spring Boot，由后端统一提供页面、API 和 WebSocket
- 使用阶段：用户机只需浏览器访问服务机地址，无需安装 Node、Java 或源码环境

## 功能列表

- 用户登录与 JWT 鉴权
- 课程创建、班级创建、邀请码加入
- 管理员发布签到、作业、考试、资料、通知
- 成员签到、提交作业、在线考试
- 考试预置题库、自动判分、作答详情查看
- WebSocket 实时通知与签到提醒
- MySQL 数据持久化

## 技术栈

### 前端

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Axios
- WebSocket

### 后端

- Java 8
- Spring Boot 2.7.18
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring WebSocket
- JWT
- BCrypt
- Maven

### 数据与存储

- MySQL 8
- 本地文件存储

## 项目结构

```text
.
├─ src/                          # 前端源码
├─ server/                       # Spring Boot 服务端
├─ scripts/                      # 构建辅助脚本
├─ start-cs.bat                  # 一键启动
├─ start-cs.ps1
├─ stop-cs.bat                   # 一键停止
├─ stop-cs.ps1
├─ TECHNICAL-DOC.md              # 技术架构文档
└─ README.md
```

## 运行环境

部署或本地运行前请准备：

- Node.js 18 及以上
- npm
- Java 8
- Maven 3.8+
- MySQL 8
- Docker Desktop 或 Docker Engine（可选，用于容器化部署）

## 数据库配置

默认数据库配置位于 `server/src/main/resources/application.yml`：

- 数据库名：`campus_learning`
- 用户名：`root`
- 密码：`123456`
- 端口：`3306`

如你的本机配置不同，请先修改 `application.yml` 中的以下配置：

```yml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/campus_learning?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
    username: root
    password: 123456
```

首次运行前可手动创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS campus_learning
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

## 快速启动

### 方式一：一键启动

在 Windows 下可直接双击：

- `start-cs.bat`

脚本会自动执行以下操作：

- 检查 Node、npm、Maven
- 检查 MySQL 3306 端口
- 自动安装前端依赖（如未安装）
- 自动构建前端并同步到后端静态目录
- 自动启动 Spring Boot
- 自动打开 `http://localhost:8080`

停止服务可双击：

- `stop-cs.bat`

### 方式二：手动启动

前端构建并同步到后端：

```bash
npm install
npm run build:cs
```

启动后端：

```bash
cd server
mvn spring-boot:run
```

启动后访问：

- 本机：`http://localhost:8080`
- 局域网其他设备：`http://服务机IP:8080`

## Docker 部署

如果希望他人拉取仓库后更快部署，推荐使用 Docker。

### 1. 准备环境变量

复制一份环境变量模板：

```bash
copy .env.example .env
```

如果是 Linux / macOS：

```bash
cp .env.example .env
```

然后按需修改以下内容：

- MySQL 密码
- JWT 密钥
- 服务端口

### 2. 启动容器

在项目根目录执行：

```bash
docker compose up -d --build
```

首次启动会完成以下流程：

- 构建前端并同步到后端静态目录
- 构建 Spring Boot 应用镜像
- 启动 MySQL 8
- 启动应用服务

### 3. 访问系统

启动完成后访问：

- 本机：`http://localhost:8080`
- 局域网：`http://服务机IP:8080`

### 4. 停止容器

```bash
docker compose down
```

如果希望连卷一起删除：

```bash
docker compose down -v
```

## 开发模式

如果需要前后端联调开发：

启动前端：

```bash
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```

启动后端：

```bash
cd server
mvn spring-boot:run
```

开发环境下：

- 前端地址：`http://127.0.0.1:5173`
- 后端地址：`http://127.0.0.1:8080`

Vite 已配置 `/api` 和 `/ws` 代理到后端。

## 打包部署

如果需要生成后端可执行包：

```bash
npm run package:cs
```

生成产物：

```text
server/target/campus-learning-server-0.0.1-SNAPSHOT.jar
```

启动方式：

```bash
java -jar server/target/campus-learning-server-0.0.1-SNAPSHOT.jar
```

## 环境变量

项目支持通过环境变量覆盖默认配置，常用项如下：

- `SERVER_PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATASOURCE_DRIVER_CLASS_NAME`
- `CAMPUS_JWT_SECRET`
- `CAMPUS_JWT_EXPIRE_HOURS`
- `CAMPUS_FILE_UPLOAD_PATH`

模板文件见：

- `.env.example`

## 默认测试账号

系统启动时会自动初始化以下账号：

- `teacher01 / 123456`
- `student01 / 123456`
- `test / 1234`

同时会自动预置 5 套小学难度考试题卷，覆盖：

- 语文
- 数学
- 英语

## 文档说明

- 部署说明：`server/DEPLOYMENT-CS.md`
- 技术文档：`TECHNICAL-DOC.md`

## 常见问题

### 1. 页面打不开

请确认：

- 后端是否已启动
- `8080` 端口是否被占用
- Windows 防火墙是否放通 `8080`

### 2. 无法连接数据库

请确认：

- MySQL 已启动
- `application.yml` 中账号密码正确
- 数据库 `campus_learning` 存在，或允许自动创建

### 3. 局域网其他设备无法访问

请确认：

- 使用的是服务机真实局域网 IP，而不是 `localhost`
- 用户机与服务机在同一局域网
- 服务机未拦截 `8080` 端口

### 4. Docker 启动失败

请确认：

- Docker 已正确安装并启动
- `8080` 和 `3306` 端口未被占用
- `.env` 中数据库和 JWT 配置合法

## 后续建议

如果要用于更正式的部署环境，建议继续完善：

- 将数据库配置抽离为环境变量
- 将文件存储从本地磁盘升级为对象存储
- 引入数据库迁移工具
- 增加 Docker / docker-compose 部署方案
