// ============================================
// instanceof 测试 - 测试类型判断和父子关系
// ============================================

// 1. 基本类型判断
Object obj1 = 100
echo "obj1 = {obj1}"
if obj1 instanceof Integer {
    echo "obj1 是 Integer 类型"
} else {
    echo "obj1 不是 Integer 类型"
}

// 2. 多态测试 - 子类是父类的实例
ArrayList list = new
list.add(1)
list.add(2)

Object obj2 = list
echo ""
echo "obj2 实际类型是 ArrayList，声明类型是 Object"
if obj2 instanceof ArrayList {
    echo "obj2 是 ArrayList 类型（精确类型匹配）"
} else {
    echo "obj2 不是 ArrayList 类型"
}

if obj2 instanceof Object {
    echo "obj2 是 Object 类型（父类匹配，子类实例）"
} else {
    echo "obj2 不是 Object 类型"
}

// 3. 接口判断
if list instanceof List {
    echo "list 是 java.util.List 接口的实现类"
} else {
    echo "list 不是 java.util.List 接口的实现类"
}

// 4. 多个 instanceof 连用
echo ""
Object obj3 = "hello"
if obj3 instanceof String {
    echo "obj3 是 String"
} else if obj3 instanceof Integer {
    echo "obj3 是 Integer"
} else {
    echo "obj3 是其他类型"
}

// 5. 全限定名类型判断
echo ""
Object obj4 = list
if obj4 instanceof java.util.ArrayList {
    echo "obj4 是 java.util.ArrayList（使用全限定名）"
} else {
    echo "obj4 不是 java.util.ArrayList"
}

// 6. null 值判断
Object obj5 = null
echo ""
if obj5 instanceof String {
    echo "null 是 String（这是错的）"
} else {
    echo "null 不是任何类型 instanceof 返回 false"
}

export obj1
export obj2
export obj3
export obj4
// 不 export obj5，因为它是 null，可能导致 export 逻辑出现问题
