// ============================================
// var 作用域测试
// ============================================

// 1. 全局作用域变量
var globalVar = "全局变量"
echo "全局作用域: globalVar = {globalVar}"

// 2. if 语句块作用域
if true {
    var ifVar = "if块变量"
    echo "if块内: ifVar = {ifVar}"
    echo "if块内访问全局: globalVar = {globalVar}"
}

// 3. for 循环作用域
var beforeLoop = "循环前"
echo "循环前: beforeLoop = {beforeLoop}"

for i in 1..3 {
    var loopVar = "循环变量 " + i
    echo "循环内: loopVar = {loopVar}"
    echo "循环内访问全局: globalVar = {globalVar}"
    echo "循环内访问循环前: beforeLoop = {beforeLoop}"
}

// 4. while 循环作用域
var beforeWhile = "while前"
var whileCounter = 0
echo "while前: beforeWhile = {beforeWhile}, whileCounter = {whileCounter}"

while whileCounter < 2 {
    var whileInner = "while内部 " + whileCounter
    echo "while内: whileInner = {whileInner}"
    whileCounter = whileCounter + 1
}

// 5. 嵌套作用域
var outer = "外层"
echo "外层: outer = {outer}"

if true {
    var middle = "中层"
    echo "中层: middle = {middle}, outer = {outer}"
    
    if true {
        var inner = "内层"
        echo "内层: inner = {inner}, middle = {middle}, outer = {outer}"
    }
}

// 6. 变量重新赋值（作用域测试）
var counter = 0
echo "counter初始值 = {counter}"

for i in 1..3 {
    counter = counter + 1
    echo "counter在循环中 = {counter}"
}

echo "counter最终值 = {counter}"

// 7. var 和传统声明混合作用域
var varVar = "var变量"
Integer traditionalVar = 100
echo "混合声明: varVar = {varVar}, traditionalVar = {traditionalVar}"

if true {
    var varInIf = "var在if中"
    Integer traditionalInIf = 200
    echo "if中: varInIf = {varInIf}, traditionalInIf = {traditionalInIf}"
    echo "if中访问外部: varVar = {varVar}, traditionalVar = {traditionalVar}"
}

// 8. 手动指定类型的作用域
var autoType = "自动类型"
var manualType:String = "手动类型"
echo "类型作用域: autoType = {autoType}, manualType = {manualType}"

if true {
    var autoInIf = "自动类型在if中"
    var manualInIf:String = "手动类型在if中"
    echo "if中类型: autoInIf = {autoInIf}, manualInIf = {manualInIf}"
}

export "作用域测试完成"

