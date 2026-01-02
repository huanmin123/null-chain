// ============================================
// FOR语句Map键值对迭代基础测试
// ============================================

// 1. 基本Map迭代
Map map = new
map.put("name", "Alice")
map.put("age", "25")
map.put("city", "Beijing")

for k, v in map {
    echo "Key: {k}, Value: {v}"
}

// 2. Map值累加
Map scores = new
scores.put("math", 90)
scores.put("english", 85)
scores.put("science", 95)

Integer totalScore = 0
for k, v in scores {
    totalScore = totalScore + v
    echo "科目: {k}, 分数: {v}, 累计: {totalScore}"
}
echo "总分 = {totalScore}"

// 3. Map键和值的不同处理
Map userInfo = new
userInfo.put("username", "john_doe")
userInfo.put("email", "john@example.com")
userInfo.put("phone", "123-456-7890")

for key, value in userInfo {
    if key == "email" {
        echo "邮箱地址: {value}"
    } else if key == "phone" {
        echo "电话号码: {value}"
    } else {
        echo "{key}: {value}"
    }
}

// 4. Map中查找特定值
Map priceMap = new
priceMap.put("apple", 5)
priceMap.put("banana", 3)
priceMap.put("orange", 4)
priceMap.put("grape", 8)

String expensiveItem = ""
Integer maxPrice = 0
for fruit, price in priceMap {
    if price > maxPrice {
        maxPrice = price
        expensiveItem = fruit
    }
}
echo "最贵的水果: {expensiveItem}, 价格: {maxPrice}"

// 5. 空Map处理
Map emptyMap = new
Integer count = 0
for k, v in emptyMap {
    count = count + 1
}
echo "空Map迭代次数 = {count}"

// 6. Map中使用continue
Map dataMap = new
dataMap.put("valid1", 100)
dataMap.put("skip", 0)
dataMap.put("valid2", 200)
dataMap.put("ignore", 0)

Integer validSum = 0
for k, v in dataMap {
    if v == 0 {
        continue
    }
    validSum = validSum + v
    echo "有效数据: {k} = {v}"
}
echo "有效数据总和 = {validSum}"

// 7. Map中使用break
Map searchMap = new
searchMap.put("item1", "value1")
searchMap.put("item2", "value2")
searchMap.put("target", "found!")
searchMap.put("item4", "value4")

String result = ""
for k, v in searchMap {
    if k == "target" {
        result = v
        echo "找到目标: {k} = {v}"
        break
    }
}
echo "查找结果 = {result}"

export totalScore
export expensiveItem
export maxPrice
export validSum
export result

