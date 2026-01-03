# 函数功能测试脚本说明

## 测试脚本列表

1. **function_basic.nf** - 基础函数测试（单返回值）
   - 函数定义和调用
   - 不同类型的参数

2. **function_multi_return.nf** - 多返回值函数测试
   - 返回多个值的函数定义
   - 多返回值函数调用和赋值

3. **function_in_expression.nf** - 函数在表达式中的使用
   - 函数调用参与表达式计算
   - 多个函数调用组合

4. **function_varargs.nf** - 可变参数函数测试
   - 可变参数定义（使用...）
   - 可变参数函数调用

5. **function_recursive.nf** - 递归函数测试
   - 阶乘函数
   - 斐波那契函数

6. **function_complex.nf** - 复杂函数场景测试
   - 多返回值函数
   - 复杂计算逻辑

7. **function_scope.nf** - 函数作用域测试
   - 函数作用域隔离
   - 全局变量访问

8. **function_integration.nf** - 函数集成测试
   - 多个函数组合使用
   - 综合场景

9. **function_demo.nf** - 函数功能演示脚本
   - 展示所有函数功能
   - 适合快速测试

## 运行测试

### 方式1：运行测试类

```bash
# 运行所有函数测试
mvn test -Dtest=FunctionTest

# 运行单个测试方法
mvn test -Dtest=FunctionTest#testBasicFunction
```

### 方式2：直接运行脚本文件

使用 `NfMain.run()` 方法运行脚本文件：

```java
import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestDemo {
    public static void main(String[] args) {
        String script = FileReadUtil.readAllStr(new File("function_demo.nf"));
        Object result = NfMain.run(script, log, null);
        System.out.println("执行结果: " + result);
    }
}
```

## 函数语法说明

### 函数定义

```nf
fun 函数名(参数列表)返回值类型列表 {
    函数体
    return 返回值1, 返回值2, ...
}
```

**示例：**
```nf
// 单返回值
fun add(int a, int b)Integer {
    return a + b
}

// 多返回值
fun getNameAndAge(String name, int age)String,Integer {
    return name, age
}

// 可变参数
fun sum(int first, int... rest)Integer {
    Integer total = first
    for item in rest {
        total = total + item
    }
    return total
}
```

### 函数调用

```nf
// 单返回值赋值
Integer result = add(10, 20)

// 多返回值赋值
var name:String, age:Integer = getNameAndAge("张三", 25)

// 函数调用在表达式中
Integer result = multiply(5, 6) + multiply(2, 3)
```

## 注意事项

1. 函数定义必须在调用之前
2. 函数参数支持可变参数（使用 `类型... 参数名`）
3. 函数返回值支持多返回值（用逗号分隔）
4. 函数调用可以参与表达式计算
5. 函数作用域是隔离的，函数内部变量不会影响外部

