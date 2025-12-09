# 第一步：构建环境 - 编译代码并生成JAR文件
FROM eclipse-temurin:17-jdk-jammy as builder

WORKDIR /app

# 复制Maven包装器和项目配置文件
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# 为mvnw脚本添加执行权限
RUN chmod +x ./mvnw

# 下载依赖项
RUN ./mvnw dependency:go-offline

# 复制应用程序源代码
COPY src ./src

# 打包应用程序
RUN ./mvnw clean package -DskipTests

# 第二步：运行环境 - 仅包含JRE和JAR文件
FROM eclipse-temurin:17-jre-jammy

# 设置工作目录
WORKDIR /app

# 从构建环境中仅复制JAR文件
COPY --from=builder /app/target/*.jar app.jar

# 创建上传目录
RUN mkdir -p uploads/avatars

# 应用程序暴露的端口
EXPOSE 9090

# 启动应用程序
ENTRYPOINT ["java", "-jar", "app.jar"]