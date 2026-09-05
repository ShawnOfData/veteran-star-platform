FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/veteran-server-*.jar app.jar

ENV TZ=Asia/Shanghai
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
ENV REDIS_HOST=redis
ENV REDIS_PORT=6379
ENV REDIS_PASSWORD=
ENV JWT_SECRET=yfRGxv3NjlvocLySH8FxzP9MXc6kfBAX8jf3R5zT
ENV AES_KEY=HNb6BbXuVGvWEaWS
ENV AES_IV=gfSuzTHRVHq2FEbL

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]