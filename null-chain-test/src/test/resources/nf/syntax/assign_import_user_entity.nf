// 导入自定义 Java 类型（使用 import type） UserEntity
import type com.gitee.huanminabc.test.nullchain.entity.UserEntity

// 创建 UserEntity 对象并设置属性
UserEntity user = new
user.setId(1)
user.setName("huanmin")
user.setAge(25)
user.setSex("男")

// 调用对象方法
String testResult = user.getTest("test_string")

// 导出用户对象
export user

