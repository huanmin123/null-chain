# 核心API

本文档介绍 `NullChain` 接口的核心方法，这是 Null-Chain 框架的基础接口。

## 创建 Null 链

### 基本创建方法

```java
// 创建包含值的链
NullChain<String> chain1 = Null.of("value");

// 创建空链
NullChain<String> chain2 = Null.empty();

// 从可能为 null 的值创建
User user = getUserById(1);
NullChain<User> chain3 = Null.of(user);
```

### 集合和数组创建

```java
// 列表
List<String> list = Arrays.asList("a", "b", "c");
NullChain<List<String>> listChain = Null.of(list);

// 数组
String[] array = {"a", "b", "c"};
NullChain<String[]> arrayChain = Null.of(array);

// Map
Map<String, String> map = new HashMap<>();
NullChain<Map<String, String>> mapChain = Null.of(map);

// Set
Set<String> set = new HashSet<>();
NullChain<Set<String>> setChain = Null.of(set);
```

### 从 Optional 创建

```java
Optional<String> optional = Optional.of("value");
NullChain<String> chain = Null.of(optional);
```

## 映射操作

### map() - 值映射

```java
// 基本映射
String name = Null.of(user)
    .map(User::getName)
    .orElse("UNKNOWN");

// 链式映射
String upperName = Null.of(user)
    .map(User::getName)
    .map(String::toUpperCase)
    .orElse("UNKNOWN");

// 多级映射
String roleName = Null.of(user)
    .map(User::getRole)
    .map(Role::getName)
    .orElse("无角色");
```

### map() - 带参数的映射

```java
// Map 取值
Map<String, String> map = new HashMap<>();
map.put("key", "value");
String value = Null.of(map)
    .map(Map::get, "key")
    .orElse("default");

// JSONObject 取值
JSONObject jsonObject = new JSONObject();
jsonObject.put("content", "value");
String content = Null.of(jsonObject)
    .map(JSONObject::getString, "content")
    .orElse("default");
```

### flatChain() - 扁平化链

```java
// 处理返回 NullChain 的映射函数
String result = Null.of(user)
    .flatChain(u -> Null.of(u.getProfile())
        .map(Profile::getDescription))
    .orElse("无描述");
```

### flatOptional() - 扁平化 Optional

```java
// 处理返回 Optional 的映射函数
String result = Null.of(user)
    .flatOptional(u -> u.getProfile()
        .map(Profile::getDescription))
    .orElse("无描述");
```

## 条件判断

### ifGo() - 条件分支

```java
// 只有满足条件才继续执行
String result = Null.of(user)
    .ifGo(u -> u.getAge() > 18)
    .map(User::getName)
    .orElse("未成年用户");
```

### ifNeGo() - 反向条件分支

```java
// 条件为 false 时才继续执行
String result = Null.of(user)
    .ifNeGo(u -> u.getAge() < 18)
    .map(User::getName)
    .orElse("未成年用户");
```

### of() - 条件判断（返回空值则中断）

```java
// 如果返回 null，链变为空链
String result = Null.of(user)
    .of(User::getName)  // 如果用户名为空，链变为空链
    .map(String::toUpperCase)
    .orElse("UNKNOWN");
```

### isNull() - 空值检查

```java
// 检查字段是否为空，为空则继续执行
String result = Null.of(user)
    .isNull(User::getName)     // 检查姓名是否为空
    .isNull(User::getEmail)   // 检查邮箱是否为空
    .map(User::getProfile)    // 只有前面的节点满足条件才继续
    .orElse("默认配置");
```

## 默认值处理

### orElse() - 固定默认值

```java
String name = Null.of(user)
    .map(User::getName)
    .orElse("默认名称");
```

### orElse() - Supplier 默认值

```java
String name = Null.of(user)
    .map(User::getName)
    .orElse(() -> "用户" + user.getId());
```

### orElseNull() - 返回 null

```java
String name = Null.of(user)
    .map(User::getName)
    .orElseNull();
```

### or() - 使用 Supplier 提供默认值

```java
String result = Null.of(user)
    .map(User::getName)
    .or(() -> "默认用户名")
    .orElse("未知用户");
```

### or() - 使用固定值作为默认值

```java
String result = Null.of(user)
    .map(User::getName)
    .or("默认用户名")
    .orElse("未知用户");
```

## 执行操作

### then() - 执行操作但不改变值

```java
Null.of(user)
    .then(() -> log.info("处理用户数据"))
    .map(User::getName)
    .orElse("未知用户");
```

### peek() - 执行操作但不改变值（带参数）

```java
Null.of(user)
    .peek(u -> log.info("处理用户: {}", u.getName()))
    .map(User::getName)
    .orElse("未知用户");
```

## 终结操作

### get() - 获取值（抛出异常）

```java
String name = Null.of(user)
    .map(User::getName)
    .get();  // 如果为空，抛出运行时异常
```

### getSafe() - 安全获取值（抛出检查异常）

```java
try {
    String name = Null.of(user)
        .map(User::getName)
        .getSafe();  // 如果为空，抛出 NullChainCheckException
} catch (NullChainCheckException e) {
    // 处理空值情况
}
```

### get() - 抛出自定义异常

```java
String name = Null.of(user)
    .map(User::getName)
    .get(() -> new IllegalArgumentException("用户名不能为空"));
```

### get() - 抛出带自定义消息的异常

```java
String name = Null.of(user)
    .map(User::getName)
    .get("用户{}的姓名不能为空", user.getId());
```

### ifPresent() - 如果存在则执行

```java
Null.of(user)
    .map(User::getName)
    .ifPresent(name -> System.out.println("用户名: " + name));
```

### ifPresentOrElse() - 如果存在则执行，否则执行其他操作

```java
Null.of(user)
    .map(User::getName)
    .ifPresentOrElse(
        name -> System.out.println("用户名: " + name),
        () -> System.out.println("用户名为空")
    );
```

### is() - 判断是否为空

```java
boolean isEmpty = Null.of(user)
    .map(User::getName)
    .is();  // 返回 true 表示值为空
```

### non() - 判断是否不为空

```java
boolean isNotEmpty = Null.of(user)
    .map(User::getName)
    .non();  // 返回 true 表示值不为空
```

### collect() - 创建收集器

```java
// 收集器用于保留链中不同类型的值
NullCollect collect = Null.of(user)
    .map(User::getProfile)
    .map(Profile::getSettings)
    .collect();

// 可以同时访问链中的多个值
User user = collect.get(User.class);
Profile profile = collect.get(Profile.class);
Settings settings = collect.get(Settings.class);
```

### length() - 获取值的长度

```java
// 获取值的长度，如果值是null那么返回0
int len = Null.of(user)
    .map(User::getName)
    .length();
```

### capture() - 抓取未知异常

```java
// 抓取未知异常，自行处理异常逻辑
Null.of(user)
    .map(User::getName)
    .capture(e -> System.out.println("发生异常：" + e.getMessage()));
```

### doThrow() - 抛出自定义消息的异常

```java
Null.of(user)
    .map(User::getName)
    .doThrow(IllegalArgumentException.class, "获取用户姓名时发生异常：用户{}", user.getId());
```

## 方法总结

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `map()` | 值映射 | `NullChain<R>` |
| `flatChain()` | 扁平化链 | `NullChain<R>` |
| `flatOptional()` | 扁平化 Optional | `NullChain<R>` |
| `ifGo()` | 条件分支 | `NullChain<T>` |
| `ifNeGo()` | 反向条件分支 | `NullChain<T>` |
| `of()` | 条件判断 | `NullChain<T>` |
| `isNull()` | 空值检查 | `NullChain<T>` |
| `then()` | 执行操作 | `NullChain<T>` |
| `peek()` | 执行操作（带参数） | `NullChain<T>` |
| `or()` | 默认值 | `NullChain<T>` |
| `orElse()` | 获取默认值 | `T` |
| `orElseNull()` | 返回 null | `T` |
| `get()` | 获取值 | `T` |
| `getSafe()` | 安全获取值 | `T` |
| `is()` | 判断是否为空 | `boolean` |
| `non()` | 判断是否不为空 | `boolean` |
| `ifPresent()` | 如果存在则执行 | `void` |
| `ifPresentOrElse()` | 如果存在则执行，否则执行其他 | `void` |
| `collect()` | 创建收集器 | `NullCollect` |
| `length()` | 获取长度 | `int` |

