
# Null-Chain 空值安全链式编程框架

[![Maven Central](https://img.shields.io/maven-central/v/com.gitee.huanminabc/null-chain-core.svg)](https://mvnrepository.com/artifact/com.gitee.huanminabc/null-chain-core)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/Java-8+-green.svg)](https://www.oracle.com/java/)

## 项目简介

Null-Chain 是一个强大的空值安全链式编程框架，提供了类似 Optional 但功能更丰富的链式 API。通过 Null 链，可以优雅地处理可能为空的值，避免 NullPointerException。

## 主要特性

- 🛡️ **空值安全**：所有操作都是空值安全的，不会抛出 NullPointerException
- 🔗 **链式调用**：支持流畅的链式编程风格
- 🎯 **类型安全**：通过泛型保证类型安全
- 🚀 **高性能**：延迟执行和短路操作，性能优异
- 📦 **模块化**：提供多个模块，按需引入
- 🌐 **Spring集成**：完美集成 Spring Boot 生态
- 📝 **完整日志**：提供完整的操作日志追踪
- 🔄 **异步支持**：支持异步操作和并发处理

## 快速开始

### Maven 依赖

```xml
<!-- 核心模块 -->
<dependency>
    <groupId>com.gitee.huanminabc</groupId>
    <artifactId>null-chain-core</artifactId>
    <version>1.1.1-RELEASE</version>
</dependency>

<!-- Spring Boot 集成 -->
<dependency>
    <groupId>com.gitee.huanminabc</groupId>
    <artifactId>null-chain-boot-starter</artifactId>
    <version>1.1.1-RELEASE</version>
</dependency>
```

### 基本使用

```java
// 基本使用
String result = Null.of(user)
    .map(User::getName)
    .map(String::toUpperCase)
    .orElse("UNKNOWN");

// 集合操作
List<String> names = Null.of(users)
    .stream()
    .map(User::getName)
    .filter(Objects::nonNull)
    .collect(Collectors.toList());

// 条件判断
String message = Null.of(user)
    .ifGo(u -> u.getAge() > 18)
    .map(User::getName)
    .orElse("未成年用户");
```

## 模块说明

- **null-chain-core**：核心模块，提供基础的链式操作功能
- **null-chain-boot-starter**：Spring Boot 自动配置模块
- **null-chain-boot-dubbo**：Dubbo 集成模块
- **null-chain-work**：工作流模块
- **null-chain-test**：测试模块

## 详细文档

📖 **完整使用教程**：https://www.yuque.com/huanmin-4bkaa/wfakqr?# 《Java-空链》

## 版本历史

- **1.1.1-RELEASE**：当前版本
  - 完善了日志常量管理
  - 优化了代码注释和文档
  - 修复了类型安全问题
  - 增强了异常处理机制

## 许可证

本项目采用 MIT 许可证，详情请参见 [LICENSE](LICENSE) 文件。

## 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进项目。

## 联系方式

- 作者：huanmin
- 项目地址：https://gitee.com/huanminabc/null-chain