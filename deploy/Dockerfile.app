# 后端容器：Java 17 运行环境
# 本地构建 jar 后上传，Dockerfile 只负责运行
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="veteran-server"
LABEL description="戎归·星辉 退役大学生积分管理系统 - 后端"

WORKDIR /app

# 时区
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 复制 jar（由 docker-compose 挂载或 COPY）
COPY app.jar app.jar

# 环境变量默认值（可被 docker-compose environment 覆盖）
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8" \
    SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]
