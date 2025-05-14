# 数据库连接指南

本指南将帮助您正确配置和连接到MySQL数据库。

## 前提条件

1. 安装MySQL 8.0
   - 下载地址：https://dev.mysql.com/downloads/mysql/
   - 选择适合您操作系统的版本
   - 按照安装向导完成安装

2. 安装MySQL Workbench（可选）
   - 下载地址：https://dev.mysql.com/downloads/workbench/
   - 这是一个图形化的MySQL管理工具

## 配置步骤

1. 创建数据库
   ```sql
   CREATE DATABASE student_management
   CHARACTER SET utf8mb4
   COLLATE utf8mb4_unicode_ci;
   ```

2. 创建用户并授权
   ```sql
   CREATE USER 'student_admin'@'localhost' IDENTIFIED BY '000000';
   GRANT ALL PRIVILEGES ON student_management.* TO 'student_admin'@'localhost';
   FLUSH PRIVILEGES;
   ```

3. 导入数据库结构
   ```sql
   USE student_management;
   SOURCE schema.sql;
   ```

4. 导入示例数据
   ```sql
   SOURCE data.sql;
   ```

5. 导入查询视图和存储过程
   ```sql
   SOURCE queries.sql;
   ```

## 应用程序配置

1. 修改application.properties
   ```properties
   # 数据库连接配置
   spring.datasource.url=jdbc:mysql://localhost:3306/student_management?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   spring.datasource.username=student_admin
   spring.datasource.password=000000
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   
   # JPA配置
   spring.jpa.hibernate.ddl-auto=none
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
   
   # 连接池配置
   spring.datasource.hikari.maximum-pool-size=5
   spring.datasource.hikari.minimum-idle=1
   spring.datasource.hikari.connection-timeout=10000
   ```

## 常见问题

1. 连接被拒绝
   - 检查MySQL服务是否运行
   - 验证用户名和密码
   - 确认数据库名称正确
   - 检查防火墙设置

2. 字符集问题
   - 确保使用utf8mb4字符集
   - 检查表和列的字符集设置
   - 验证连接字符串中的字符集配置

3. 权限问题
   - 检查用户权限
   - 确认用户可以访问指定数据库
   - 验证用户可以执行所需操作

## 数据库维护

1. 备份数据
   ```bash
   mysqldump -u student_admin -p student_management > backup.sql
   ```

2. 恢复数据
   ```bash
   mysql -u student_admin -p student_management < backup.sql
   ```

3. 优化数据库
   ```sql
   OPTIMIZE TABLE student, course, score, user, system_log;
   ```

## 安全建议

1. 密码安全
   - 使用强密码
   - 定期更改密码
   - 不要在代码中硬编码密码

2. 网络安全
   - 限制数据库访问IP
   - 使用SSL/TLS加密连接
   - 关闭不必要的端口

3. 权限控制
   - 遵循最小权限原则
   - 定期审查用户权限
   - 删除不使用的账户

## 性能优化

1. 索引优化
   - 为常用查询创建索引
   - 避免过多索引
   - 定期维护索引

2. 查询优化
   - 使用EXPLAIN分析查询
   - 优化JOIN操作
   - 避免SELECT *

3. 配置优化
   - 调整连接池大小
   - 配置适当的缓存
   - 优化内存使用 