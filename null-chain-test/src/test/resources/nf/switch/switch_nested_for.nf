// ============================================
// SWITCH嵌套FOR测试
// ============================================

// switch中嵌套for
Integer switchSum = 0
switch 1 {
    case 1
        for i in 1..5 {
            switchSum = switchSum + i
        }
    case 2
        switchSum = 0
}
echo "switchSum = {switchSum}"

export switchSum

