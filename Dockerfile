FROM node:20-alpine AS frontend-builder
WORKDIR /app

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml* ./
COPY tsconfig.json vite.config.ts tailwind.config.js postcss.config.js index.html ./
COPY public ./public
COPY src ./src
COPY scripts ./scripts

RUN npm install
RUN npm run build:cs

FROM maven:3.9.8-eclipse-temurin-8 AS backend-builder
WORKDIR /build

COPY server/pom.xml ./server/pom.xml
RUN mvn -f ./server/pom.xml dependency:go-offline

COPY server ./server
COPY --from=frontend-builder /app/server/src/main/resources/static ./server/src/main/resources/static

RUN mvn -f ./server/pom.xml clean package -DskipTests

FROM eclipse-temurin:8-jre
WORKDIR /app

ENV TZ=Asia/Shanghai

COPY --from=backend-builder /build/server/target/campus-learning-server-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
