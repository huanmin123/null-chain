// ============================================
// FOR语句列表迭代基础测试
// ============================================

// 1. 基本列表迭代
ArrayList list = new
list.add(1)
list.add(2)
list.add(3)
list.add(4)
list.add(5)

for item in list {
    echo "item = {item}"
}

// 2. 列表元素求和
ArrayList numbers = new
numbers.add(10)
numbers.add(20)
numbers.add(30)
numbers.add(40)

Integer sum = 0
for num in numbers {
    sum = sum + num
}
echo "列表元素总和 = {sum}"

// 3. 列表中字符串处理
ArrayList names = new
names.add("Alice")
names.add("Bob")
names.add("Charlie")

for name in names {
    echo "Hello, {name}!"
}

// 4. 列表中的条件判断
ArrayList scores = new
scores.add(85)
scores.add(92)
scores.add(78)
scores.add(95)
scores.add(88)

Integer highScoreCount = 0
for score in scores {
    if score >= 90 {
        highScoreCount = highScoreCount + 1
        echo "高分: {score}"
    }
}
echo "90分以上的数量 = {highScoreCount}"

// 5. 空列表处理
ArrayList emptyList = new
Integer count = 0
for item in emptyList {
    count = count + 1
}
echo "空列表迭代次数 = {count}"

// 6. 列表中使用continue
ArrayList evens = new
evens.add(2)
evens.add(3)
evens.add(4)
evens.add(5)
evens.add(6)

Integer evenSum = 0
for num in evens {
    if num % 2 != 0 {
        continue
    }
    evenSum = evenSum + num
    echo "偶数: {num}"
}
echo "偶数总和 = {evenSum}"

// 7. 列表中使用break
ArrayList findList = new
findList.add(10)
findList.add(20)
findList.add(30)
findList.add(40)
findList.add(50)

Integer found = 0
for num in findList {
    if num == 30 {
        found = num
        echo "找到目标值: {num}"
        break
    }
}
echo "找到的值 = {found}"

export sum
export highScoreCount
export evenSum
export found

