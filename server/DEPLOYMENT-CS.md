# 校园在线学习平台 C/S 部署说明

## 目标
- 服务机统一运行服务端程序，并同时提供前端网页与后端 API。
- 用户机不需要安装 Node、Java、源码或任何额外依赖。
- 用户机只需通过浏览器访问服务机局域网地址即可使用系统。

## 服务机打包
在项目根目录执行：

```bash
npm run package:cs
```

执行完成后，服务端可部署产物位于：

```text
server/target/campus-learning-server-0.0.1-SNAPSHOT.jar
```

## 服务机启动
在服务机上运行：

```bash
java -jar server/target/campus-learning-server-0.0.1-SNAPSHOT.jar
```

默认监听端口：

```text
8080
```

## 用户机访问
假设服务机局域网 IP 为 `192.168.1.100`，则用户机直接访问：

```text
http://192.168.1.100:8080
```

或登录页：

```text
http://192.168.1.100:8080/login
```

## 当前架构说明
- 前端页面由 Spring Boot 直接托管。
- 前端通过同源地址访问：
  - REST API：`/api/...`
  - WebSocket：`/ws/notifications?token=...`
- 文件下载地址也采用同源相对路径，确保用户机可直接下载。

## 注意事项
- 服务机需要放通 `8080` 端口。
- 用户机与服务机必须位于同一局域网，或能互相访问。
- 如果需要改端口，可在 `server/src/main/resources/application.yml` 中调整 `server.port`。
