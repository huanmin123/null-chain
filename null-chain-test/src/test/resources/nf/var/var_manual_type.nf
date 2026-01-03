// ============================================
// var 手动指定类型测试
// ============================================

// 1. 手动指定 String 类型
var str1:String = "字符串1"
var str2:String = "字符串2"
echo "手动String: str1={str1}, str2={str2}"

// 2. 手动指定 Integer 类型
var int1:Integer = 100
var int2:Integer = -50
var int3:Integer = 0
echo "手动Integer: int1={int1}, int2={int2}, int3={int3}"

// 3. 手动指定 Double 类型
var double1:Double = 3.14159
var double2:Double = -99.99
var double3:Double = 0.0
echo "手动Double: double1={double1}, double2={double2}, double3={double3}"

// 4. 手动指定 Boolean 类型
var bool1:Boolean = true
var bool2:Boolean = false
echo "手动Boolean: bool1={bool1}, bool2={bool2}"

// 5. 手动指定类型 - 表达式结果
var sum:Integer = 10 + 20 + 30
var product:Integer = 2 * 3 * 4
var division:Double = 100.0 / 3.0
echo "表达式手动类型: sum={sum}, product={product}, division={division}"

// 6. 手动指定类型 - 字符串拼接
var message:String = "Hello" + " " + "World"
var greeting:String = "Hi" + " " + "there"
echo "字符串拼接手动类型: message={message}, greeting={greeting}"

// 7. 手动指定类型 - 变量引用
var base:Integer = 100
var derived:Integer = base + 50
echo "变量引用手动类型: base={base}, derived={derived}"

// 8. 手动指定类型 - 复杂表达式
var result:Integer = (10 + 20) * 2 - 5
var calculation:Double = (100.0 + 50.0) / 2.0
echo "复杂表达式手动类型: result={result}, calculation={calculation}"

// 9. 手动指定类型 - 比较表达式
var isGreater:Boolean = 10 > 5
var isEqual:Boolean = 10 == 10
var isLess:Boolean = 5 < 10
echo "比较表达式手动类型: isGreater={isGreater}, isEqual={isEqual}, isLess={isLess}"

// 10. 手动指定类型 - 逻辑表达式
var andResult:Boolean = true and false
var orResult:Boolean = true or false
echo "逻辑表达式手动类型: andResult={andResult}, orResult={orResult}"

// 11. 混合自动推导和手动指定
var auto = "自动"
var manual:String = "手动"
var combined:String = auto + " " + manual
echo "混合类型: auto={auto}, manual={manual}, combined={combined}"

// 12. 手动指定类型在不同作用域
var global:String = "全局手动类型"
echo "全局: global={global}"

if true {
    var local:String = "局部手动类型"
    echo "if中: local={local}, global={global}"
}

export "手动类型测试完成"

