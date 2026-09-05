FROM openjdk:8-jre-slim

WORKDIR /app

COPY target/veteran-server-*.jar app.jar

ENV TZ=Asia/Shanghai
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=
ENV JWT_SECRET=veteran-jwt-secret-key-must-be-at-least-256-bits-long
ENV AES_KEY=veteran-aes-key-16char
ENV AES_IV=veteran-iv-16char

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]