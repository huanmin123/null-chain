// ============================================
// 赋值表达式作用域测试
// ============================================

// 1. 全局作用域变量
Integer globalVar = 100
echo "全局变量: {globalVar}"

// 2. if作用域中的赋值
if true {
    Integer localVar = 200
    echo "if作用域变量: {localVar}"
    globalVar = 150
    echo "修改后的全局变量: {globalVar}"
}
echo "if外部全局变量: {globalVar}"

// 3. for循环中的变量赋值
Integer loopVar = 0
for i in 1..5 {
    loopVar = loopVar + i
    echo "循环中 loopVar = {loopVar}"
}
echo "循环后 loopVar = {loopVar}"

// 4. 嵌套作用域中的赋值
Integer outer = 10
if true {
    Integer inner = 20
    outer = 15
    echo "内部 outer = {outer}, inner = {inner}"
}
echo "外部 outer = {outer}"

// 5. 多层嵌套作用域
Integer level1 = 1
if true {
    Integer level2 = 2
    if true {
        Integer level3 = 3
        level1 = 10
        level2 = 20
        echo "level1 = {level1}, level2 = {level2}, level3 = {level3}"
    }
    echo "level1 = {level1}, level2 = {level2}"
}
echo "level1 = {level1}"

export level1




