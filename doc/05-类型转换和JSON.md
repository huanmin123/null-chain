# 类型转换和JSON

本文档介绍类型转换、JSON序列化/反序列化、对象复制等功能。

## 类型转换

### type() - 通过 Class 转换

```java
// 类型恢复：将 Object 转换为具体类型
User user = Null.of(someObject)
    .type(User.class)
    .orElse(new User());
```

### type() - 通过实例转换

```java
// 通过实例推断类型
User user = Null.of(someObject)
    .type(new User())
    .orElse(new User());
```

**注意**：
- 类型转换不支持跨类型转换（如 String 转 Integer）
- 主要用于类型恢复和类型断言
- 等效于强制类型转换：`User user = (User)obj`

## JSON 操作

### json() - 对象转 JSON

```java
// 将 Java 对象转换为 JSON 字符串
String json = Null.of(user)
    .json()
    .orElse("{}");
```

### fromJson() - JSON 转对象（通过 Class）

```java
// 将 JSON 字符串转换为指定类型的对象
User user = Null.of(jsonString)
    .fromJson(User.class)
    .orElse(new User());
```

### fromJson() - JSON 转对象（通过实例）

```java
// 通过实例推断类型
User user = Null.of(jsonString)
    .fromJson(new User())
    .orElse(new User());
```

## 对象复制

### copy() - 浅拷贝

```java
// 对对象进行浅拷贝
User copiedUser = Null.of(user)
    .copy()
    .orElse(new User());
```

**说明**：
- 浅拷贝会复制对象的基本结构
- 引用类型的字段仍然指向原对象，不会进行深度复制

### deepCopy() - 深拷贝

```java
// 对对象进行深拷贝
User deepCopiedUser = Null.of(user)
    .deepCopy()
    .orElse(new User());
```

**注意**：
- 深拷贝会完全复制对象及其所有嵌套对象
- 需要拷贝的类必须实现 `Serializable` 接口，包括内部类
- 否则会抛出 `NotSerializableException` 异常

### pick() - 字段提取

```java
// 提取单个字段
String name = Null.of(user)
    .pick(User::getName)
    .orElse("未知用户");

// 提取多个字段
Object[] fields = Null.of(user)
    .pick(User::getName, User::getEmail, User::getAge)
    .orElse(new Object[0]);
```

## 完整示例

```java
import com.gitee.huanminabc.nullchain.Null;

public class ConvertExample {
    
    public void typeConversion() {
        // 类型恢复
        Object obj = getUserData();
        User user = Null.of(obj)
            .type(User.class)
            .orElse(new User());
    }
    
    public void jsonOperations() {
        User user = new User();
        user.setName("张三");
        user.setAge(25);
        
        // 对象转 JSON
        String json = Null.of(user)
            .json()
            .orElse("{}");
        // 结果: {"name":"张三","age":25}
        
        // JSON 转对象
        String jsonString = "{\"name\":\"李四\",\"age\":30}";
        User userFromJson = Null.of(jsonString)
            .fromJson(User.class)
            .orElse(new User());
    }
    
    public void objectCopy() {
        User original = new User();
        original.setName("张三");
        original.setAge(25);
        
        // 浅拷贝
        User shallowCopy = Null.of(original)
            .copy()
            .orElse(new User());
        
        // 深拷贝
        User deepCopy = Null.of(original)
            .deepCopy()
            .orElse(new User());
    }
    
    public void fieldExtraction() {
        User user = new User();
        user.setName("张三");
        user.setEmail("zhangsan@example.com");
        user.setAge(25);
        
        // 提取单个字段
        String name = Null.of(user)
            .pick(User::getName)
            .orElse("未知");
        
        // 提取多个字段
        Object[] fields = Null.of(user)
            .pick(User::getName, User::getEmail, User::getAge)
            .orElse(new Object[0]);
    }
}
```

## 使用场景

### 类型转换场景

- 当某个操作导致类型推导为 Object 时
- 需要将 Object 类型转换为具体类型时
- 类型擦除后的类型恢复

### JSON 场景

- API 接口的请求/响应序列化
- 配置文件读取和写入
- 数据持久化
- 跨系统数据交换

### 对象复制场景

- 数据备份
- 对象克隆
- 避免修改原对象
- 数据传输

## 注意事项

1. **类型转换**：不支持跨类型转换，主要用于类型恢复
2. **深拷贝**：需要实现 Serializable 接口
3. **JSON 序列化**：使用 FastJSON 作为底层实现
4. **字段提取**：返回的是 Object 数组，需要手动转换类型

