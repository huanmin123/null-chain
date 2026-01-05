# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Null-Chain 是一个空值安全的链式编程框架，提供了类似 Optional 但功能更丰富的链式 API，用于优雅地处理空值并避免 NullPointerException。项目支持 Java 8 到 Java 25 全版本。

## 构建和测试命令

```bash
# 编译整个项目（跳过测试）
mvn clean install -DskipTests

# 编译并运行所有测试
mvn clean install

# 仅编译不打包
mvn clean compile

# 运行单个测试类
mvn test -Dtest=NullCalculateTest

# 运行 null-chain-core 模块的测试
cd null-chain-core && mvn test

# 运行 null-chain-test 模块的集成测试
cd null-chain-test && mvn test
```

## 模块架构

项目采用多模块 Maven 结构，各模块职责如下：

### null-chain-core（核心模块）
- **入口类**：`com.gitee.huanminabc.nullchain.Null` - 提供静态工厂方法创建 Null 链
- **核心接口**：
  - `NullChain<T>` - 链式操作的核心接口
  - `NullConvert<T>` - 类型转换操作
  - `NullFinality<T>` - 终结操作（orElse、get 等）
  - `NullWorkFlow<T>` - 工作流和工具调用
- **leaf 包**：各种功能实现
  - `stream` - 空值安全的流操作（NullStream、NullIntStream 等）
  - `calculate` - 数值计算（NullCalculate）
  - `date` - 日期时间操作
  - `http` - HTTP 请求封装（基于 OkHttp）
  - `json` - JSON 序列化和反序列化
  - `check` - 多级判空工具（NullCheck）
- **language 包**：NF 脚本语言解析器（基于 JEXL3 和 ByteBuddy）
- **task/tool 包**：任务和工具接口定义

### null-chain-work（工作模块）
- 提供预置的工具类实现（file、hash、base64 等）
- 提供预置的任务类实现
- 依赖 null-chain-core

### null-chain-test（测试模块）
- 包含所有功能的单元测试和集成测试
- 依赖 null-chain-work
- 注意：此模块配置了 skip install/deploy，不参与发布流程

### null-chain-boot-starter（Spring Boot 集成）
- 提供 Spring Boot 自动配置
- 集成 Spring 生态

### null-chain-boot-dubbo（Dubbo 集成）
- 提供 Dubbo RPC 集成
- 支持分布式场景下的空值安全处理

## 核心设计原则

### 空值判断规则
Null-Chain 按以下顺序判断值是否为空：
1. null 引用
2. 空字符串 ""
3. 纯空格字符串 "   "
4. 空集合（size=0）
5. 空数组（length=0）

### 使用要求
使用 Null-Chain 操作的实体类必须满足：
1. 必须实现标准的 getter/setter 方法
2. 必须提供无参构造函数
3. 所有字段必须使用包装类型（Integer、Long 等，不能用 int、long）
4. 推荐使用 Lombok 的 @Data 注解

### 短路机制
所有链式操作都采用短路机制：
- 一旦遇到空值，后续操作全部跳过
- 不会抛出 NullPointerException
- 所有方法都是空值安全的

## 重要开发注意事项

### 添加新功能时的步骤
1. 在 `null-chain-core` 的 `core` 包中定义接口（如 `NullChain`）
2. 在 `core` 包中创建实现类（如 `NullChainBase`）
3. 如需扩展接口，在 `core/ext` 包中创建扩展接口
4. 在 `Null` 入口类中添加静态方法暴露新功能

### 自定义工具和任务
- 工具类实现 `NullTool<T, R>` 接口
- 任务类实现 `NullTask<T>` 接口
- 在 `null-chain-work` 模块中注册工具和任务
- 通过 `Null.of(value).tool(ToolClass.class)` 调用工具

### NF 脚本语言
NF 脚本是基于 JEXL3 的动态脚本语言，支持：
- 运行时执行动态表达式
- 字节码增强（基于 ByteBuddy）
- 类型安全的表达式求值
- 详细文档参考 `doc/11-NF脚本.md`

### 关键依赖
- **jcommon**：作者的基础工具库（父 POM 管理）
- **commons-jexl3**：表达式引擎
- **okhttp3**：HTTP 客户端
- **jackson**：JSON 处理
- **byte-buddy**：字节码操作
- **caffeine**：高性能缓存

## 测试规范

- 测试类统一放在 `null-chain-test/src/test/java` 下
- 测试类命名规则：`XxxTest.java`
- 核心功能的测试必须覆盖空值场景
- 集成测试应使用 `null-chain-test` 模块

## 文档维护

项目文档位于 `doc/` 目录，包含：
- `01-项目介绍.md` - 设计哲学和核心概念
- `02-快速入门.md` - 基础用法示例
- `03-核心API.md` - 主要 API 详解
- `04-集合和流操作.md` - Stream 操作
- `05-类型转换和JSON.md` - 类型转换
- `06-日期操作.md` - 日期处理
- `07-HTTP请求.md` - HTTP 操作
- `08-计算操作.md` - 数值计算
- `09-多级判空.md` - NullCheck 工具
- `10-任务和工具.md` - 自定义扩展
- `11-NF脚本.md` - 脚本语言
- `12-最佳实践.md` - 使用建议
- `13-空链扩展类.md` - NullExt 扩展

添加新功能时，应在对应文档中补充说明。
