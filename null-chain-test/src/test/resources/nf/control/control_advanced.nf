// ============================================
// 控制语句高级测试
// ============================================

// 1. 嵌套循环中的break
Integer outerBreak = 0
for i in 1..5 {
    for j in 1..5 {
        if j == 3 {
            outerBreak = i * j
            break
        }
    }
    if i == 3 {
        break
    }
}
echo "outerBreak = {outerBreak}"

// 2. 嵌套循环中的breakall
Integer breakallResult = 0
for i in 1..10 {
    for j in 1..10 {
        for k in 1..10 {
            if i * j * k == 100 {
                breakallResult = i * j * k
                echo "找到 i={i}, j={j}, k={k}, 乘积={breakallResult}"
                breakall
            }
        }
    }
}
echo "breakallResult = {breakallResult}"

// 3. continue在多层循环中
Integer continueSum = 0
for i in 1..5 {
    if i == 2 {
        continue
    }
    for j in 1..3 {
        if j == 2 {
            continue
        }
        continueSum = continueSum + i * j
    }
}
echo "continueSum = {continueSum}"

// 4. break与条件组合
Integer breakWithCondition = 0
for i in 1..20 {
    if i % 7 == 0 && i > 10 {
        breakWithCondition = i
        break
    }
}
echo "breakWithCondition = {breakWithCondition}"

// 5. continue与条件组合
Integer continueWithCondition = 0
for i in 1..20 {
    if i % 3 == 0 {
        continue
    }
    if i % 5 == 0 {
        continue
    }
    continueWithCondition = continueWithCondition + i
}
echo "continueWithCondition = {continueWithCondition}"

export continueWithCondition

