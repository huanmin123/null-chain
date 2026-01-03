// ============================================
// var 类型推导测试
// ============================================

// 测试各种类型的自动推导

// 1. 整数类型推导
var int1 = 42
var int2 = -100
var int3 = 0
echo "整数: int1={int1}, int2={int2}, int3={int3}"

// 2. 浮点数类型推导
var double1 = 3.14
var double2 = -99.99
var double3 = 0.0
echo "浮点数: double1={double1}, double2={double2}, double3={double3}"

// 3. 字符串类型推导
var str1 = "单引号字符串"
var str2 = '双引号字符串'
var str3 = ""
echo "字符串: str1={str1}, str2={str2}, str3={str3}"

// 4. 布尔类型推导
var bool1 = true
var bool2 = false
echo "布尔值: bool1={bool1}, bool2={bool2}"

// 5. 表达式结果类型推导 - 整数运算
var add = 10 + 20
var sub = 50 - 30
var mul = 5 * 6
var div = 100 / 4
var mod = 17 % 5
echo "整数运算: add={add}, sub={sub}, mul={mul}, div={div}, mod={mod}"

// 6. 表达式结果类型推导 - 浮点数运算
var floatAdd = 1.5 + 2.5
var floatSub = 10.0 - 3.5
var floatMul = 2.5 * 3.0
var floatDiv = 10.0 / 3.0
echo "浮点数运算: floatAdd={floatAdd}, floatSub={floatSub}, floatMul={floatMul}, floatDiv={floatDiv}"

// 7. 字符串拼接类型推导
var concat1 = "Hello" + "World"
var concat2 = "Number: " + 123
var concat3 = 456 + " is a number"
echo "字符串拼接: concat1={concat1}, concat2={concat2}, concat3={concat3}"

// 8. 比较表达式类型推导
var gt = 10 > 5
var lt = 3 < 7
var eq = 10 == 10
var ne = 5 != 10
echo "比较运算: gt={gt}, lt={lt}, eq={eq}, ne={ne}"

// 9. 逻辑表达式类型推导
var andResult = true and false
var orResult = true or false
echo "逻辑运算: andResult={andResult}, orResult={orResult}"

// 10. 复杂表达式类型推导
var complex1 = (10 + 20) * 2
var complex2 = 100 / (5 + 5)
var complex3 = (1 + 2) * (3 + 4) - 5
echo "复杂表达式: complex1={complex1}, complex2={complex2}, complex3={complex3}"

// 11. 变量引用表达式类型推导
var base = 100
var derived1 = base + 50
var derived2 = base * 2
var derived3 = base / 4
echo "变量引用: base={base}, derived1={derived1}, derived2={derived2}, derived3={derived3}"

// 12. 手动指定类型覆盖推导
var autoType = 100
var manualType:Integer = 200
echo "类型指定: autoType={autoType}, manualType={manualType}"

export "类型推导测试完成"

