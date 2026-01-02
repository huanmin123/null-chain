// ============================================
// FOR语句迭代高级测试（列表和Map）
// ============================================

// 1. 列表和Map混合使用
ArrayList fruits = new
fruits.add("apple")
fruits.add("banana")
fruits.add("orange")

Map prices = new
prices.put("apple", 5)
prices.put("banana", 3)
prices.put("orange", 4)

Integer totalCost = 0
for fruit in fruits {
    Integer price = prices.get(fruit)
    totalCost = totalCost + price
    echo "水果: {fruit}, 价格: {price}, 累计: {totalCost}"
}
echo "总花费 = {totalCost}"

// 2. 嵌套列表迭代
ArrayList matrix = new
ArrayList row1 = new
row1.add(1)
row1.add(2)
row1.add(3)
ArrayList row2 = new
row2.add(4)
row2.add(5)
row2.add(6)
ArrayList row3 = new
row3.add(7)
row3.add(8)
row3.add(9)

matrix.add(row1)
matrix.add(row2)
matrix.add(row3)

Integer matrixSum = 0
for row in matrix {
    for element in row {
        matrixSum = matrixSum + element
    }
}
echo "矩阵元素总和 = {matrixSum}"

// 3. 列表中使用breakall（多层循环）
ArrayList outerList = new
outerList.add(1)
outerList.add(2)
outerList.add(3)

ArrayList innerList = new
innerList.add(10)
innerList.add(20)
innerList.add(30)

Integer found = 0
for a in outerList {
    for b in innerList {
        if a * b == 60 {
            found = a * b
            echo "找到目标: a={a}, b={b}, 乘积={found}"
            breakall
        }
    }
}
echo "查找结果 = {found}"

// 4. Map的键和值类型多样性
Map mixedMap = new
mixedMap.put("count", 100)
mixedMap.put("price", 29.99)
mixedMap.put("available", true)
mixedMap.put("name", "Product A")

for key, value in mixedMap {
    if value instanceof Integer {
        echo "{key} (Integer): {value}"
    } else if value instanceof Double {
        echo "{key} (Double): {value}"
    } else if value instanceof Boolean {
        echo "{key} (Boolean): {value}"
    } else {
        echo "{key} (String): {value}"
    }
}

// 5. 列表修改外部变量
ArrayList data = new
data.add(5)
data.add(10)
data.add(15)
data.add(20)

Integer max = 0
Integer min = 100
for num in data {
    if num > max {
        max = num
    }
    if num < min {
        min = num
    }
}
echo "最大值: {max}, 最小值: {min}"

// 6. Map中嵌套Map
Map nestedMap = new
Map inner1 = new
inner1.put("value", 10)
Map inner2 = new
inner2.put("value", 20)

nestedMap.put("group1", inner1)
nestedMap.put("group2", inner2)

for groupName, groupMap in nestedMap {
    Integer value = groupMap.get("value")
    echo "组: {groupName}, 值: {value}"
}

// 7. 列表和Map的作用域测试
ArrayList scopeList = new
scopeList.add(1)
scopeList.add(2)
scopeList.add(3)

Integer outerVar = 100
for item in scopeList {
    Integer innerVar = item * 10
    echo "外部: {outerVar}, 元素: {item}, 内部: {innerVar}"
    outerVar = outerVar + item
}
echo "最终外部变量: {outerVar}"

export totalCost
export matrixSum
export found
export max
export min
export outerVar

