// ============================================
// FOR语句作用域测试
// ============================================

// 1. 循环变量作用域
Integer sum = 0
for i in 1..5 {
    sum = sum + i
    echo "循环中: i = {i}, sum = {sum}"
}
echo "循环后: sum = {sum}"

// 2. 循环中修改外部变量
Integer counter = 0
for i in 1..5 {
    counter = counter + 1
    echo "counter = {counter}"
}
echo "最终 counter = {counter}"

// 3. 嵌套循环中的变量作用域
Integer total = 0
for i in 1..3 {
    Integer innerSum = 0
    for j in 1..3 {
        innerSum = innerSum + j
        total = total + j
    }
    echo "i = {i}, innerSum = {innerSum}, total = {total}"
}
echo "最终 total = {total}"

// 4. 循环中的条件赋值
Integer maxValue = 0
for i in 1..10 {
    if i > maxValue {
        maxValue = i
    }
}
echo "maxValue = {maxValue}"

// 5. 多层嵌套作用域
Integer level1 = 10
for i in 1..2 {
    Integer level2 = 20
    if true {
        Integer level3 = 30
        level1 = level1 + i
        level2 = level2 + i
        echo "i = {i}, level1 = {level1}, level2 = {level2}, level3 = {level3}"
    }
    echo "i = {i}, level1 = {level1}, level2 = {level2}"
}
echo "最终 level1 = {level1}"

export level1

