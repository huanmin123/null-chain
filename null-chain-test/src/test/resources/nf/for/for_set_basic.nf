// ============================================
// FOR语句Set迭代基础测试
// ============================================

// 1. 基本Set迭代
HashSet set = new
set.add(1)
set.add(2)
set.add(3)
set.add(4)
set.add(5)

for item in set {
    echo "item = {item}"
}

// 2. Set元素求和
HashSet numbers = new
numbers.add(10)
numbers.add(20)
numbers.add(30)
numbers.add(40)

Integer sum = 0
for num in numbers {
    sum = sum + num
}
echo "Set元素总和 = {sum}"

// 3. Set中字符串处理
HashSet names = new
names.add("Alice")
names.add("Bob")
names.add("Charlie")

for name in names {
    echo "Name: {name}"
}

// 4. Set中的条件判断
HashSet scores = new
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

// 5. 空Set处理
HashSet emptySet = new
Integer count = 0
for item in emptySet {
    count = count + 1
}
echo "空Set迭代次数 = {count}"

// 6. Set中使用continue
HashSet evens = new
evens.add(2)
evens.add(3)
evens.add(5)
evens.add(6)

Integer evenSum = 0
for num in evens {
    if num % 2 != 0 {
        continue
    }
    evenSum = evenSum + num
    echo "偶数: {num}, 累计和: {evenSum}"
}
echo "偶数总和 = {evenSum}"

// 7. Set中使用break
HashSet findSet = new
findSet.add(10)
findSet.add(20)
findSet.add(30)
findSet.add(40)
findSet.add(50)

Integer found = 0
for num in findSet {
    if num == 30 {
        found = num
        echo "找到目标值: {num}"
        break
    }
}
echo "找到的值 = {found}"

// 8. Set去重测试
HashSet duplicates = new
duplicates.add(1)
duplicates.add(2)
duplicates.add(2)
duplicates.add(3)

HashSet uniqueSet = new
for item in duplicates {
    uniqueSet.add(item)
}

Integer uniqueCount = 0
for item in uniqueSet {
    uniqueCount = uniqueCount + 1
}
echo "去重后元素数量 = {uniqueCount}"

export sum
export highScoreCount
export evenSum
export found
export uniqueCount

