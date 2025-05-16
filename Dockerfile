# 使用官方 OpenJDK 21 作为基础镜像
FROM eclipse-temurin:21-jdk-alpine

# 设置工作目录
WORKDIR /app

# 设置时区为中国时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制 Maven 构建文件
COPY pom.xml .

# 复制源代码
COPY src ./src

# 复制 Maven 包装器
COPY .mvn ./.mvn
COPY mvnw .

# 设置环境变量
ENV MAVEN_CONFIG=/root/.m2
ENV JAVA_OPTS="-Xms512m -Xmx1024m"

# 构建应用
RUN ./mvnw clean package -DskipTests

# 暴露应用端口
EXPOSE 8080

# 设置启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar target/bank-transaction-system-1.0-SNAPSHOT.jar"] 