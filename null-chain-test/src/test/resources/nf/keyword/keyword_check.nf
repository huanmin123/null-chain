// ============================================
// 关键字检查测试
// 测试所有关键字不能作为变量名、函数名、参数名
// ============================================

// 测试1: var 声明时使用关键字作为变量名（应该报错）
// Integer fun = 100  // 应该报错：fun 是关键字

// 测试2: 函数名使用关键字（应该报错）
// fun return(int x)Integer { return x }  // 应该报错：return 是关键字

// 测试3: 函数参数使用关键字（应该报错）
// fun test(int if)Integer { return if }  // 应该报错：if 是关键字

// 测试4: 正常的函数定义（应该通过）
fun add(int a, int b)Integer {
    return a + b
}

// 测试5: 全局关键字检查
// Integer global = 100  // 应该报错：global 是关键字

// 测试6: 使用 globalVar 作为变量名（应该通过，因为不是关键字）
Integer globalVar = 100
String globalStr = "test"

// 测试7: 正常的函数调用和变量使用
Integer result = add(5, 10)
echo "result = {result}"

export result
