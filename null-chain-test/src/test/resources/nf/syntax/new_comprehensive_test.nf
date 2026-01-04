// ==================== new 关键字全面测试脚本 ====================
// 本脚本测试 new 关键字在各种场景下的使用

// ------------------- 场景1: new 内置类型 -------------------
// ArrayList 是内置类型，无需导入即可使用
echo "=== 场景1: new 内置类型 ArrayList ==="
ArrayList list = new
list.add("item1")
list.add("item2")
list.add("item3")
echo "ArrayList size: ${list.size()}"
echo "ArrayList first item: ${list.get(0)}"

// ------------------- 场景2: new 内置类型 HashMap -------------------
echo "=== 场景2: new 内置类型 HashMap ==="
HashMap map = new
map.put("key1", "value1")
map.put("key2", 100)
echo "HashMap size: ${map.size()}"
echo "HashMap key1: ${map.get('key1')}"

// ------------------- 场景3: new 内置类型 HashSet -------------------
echo "=== 场景3: new 内置类型 HashSet ==="
HashSet set = new
set.add("element1")
set.add("element2")
set.add("element1")  // 重复元素不会被添加
echo "HashSet size: ${set.size()}"

// ------------------- 场景4: new 内置类型 LinkedList -------------------
echo "=== 场景4: new 内置类型 LinkedList ==="
LinkedList linkedList = new
linkedList.add("first")
linkedList.add("second")
echo "LinkedList size: ${linkedList.size()}"

// ------------------- 场景5: 导入并 new 自定义类型 -------------------
echo "=== 场景5: 导入并 new 自定义类型 UserEntity ==="
import type com.gitee.huanminabc.test.nullchain.entity.UserEntity

UserEntity user = new
user.setId(100)
user.setName("测试用户")
user.setAge(30)
user.setSex("男")
echo "UserEntity id: ${user.getId()}"
echo "UserEntity name: ${user.getName()}"
echo "UserEntity age: ${user.getAge()}"

// ------------------- 场景6: new 后立即调用方法 -------------------
echo "=== 场景6: new 后立即调用方法链 ==="
ArrayList list2 = new
list2.add("test")
String result = list2.get(0)
echo "链式调用结果: ${result}"

// ------------------- 场景7: 多次 new 创建多个对象 -------------------
echo "=== 场景7: 多次 new 创建多个对象 ==="
UserEntity user1 = new
user1.setName("用户1")
UserEntity user2 = new
user2.setName("用户2")
echo "user1 name: ${user1.getName()}"
echo "user2 name: ${user2.getName()}"

// ------------------- 场景8: new 对象作为参数传递 -------------------
echo "=== 场景8: new 对象作为参数使用 ==="
ArrayList paramList = new
paramList.add("param1")
paramList.add("param2")
int paramSize = paramList.size()
echo "参数列表大小: ${paramSize}"

// ------------------- 场景9: 嵌套使用 new -------------------
echo "=== 场景9: 嵌套集合使用 new ==="
ArrayList outerList = new
outerList.add("outer1")
ArrayList innerList = new
innerList.add("inner1")
outerList.add(innerList)
echo "外层列表大小: ${outerList.size()}"

// ------------------- 场景10: 导入多个类型并 new -------------------
echo "=== 场景10: 导入多个类型并 new ==="
import type com.gitee.huanminabc.test.nullchain.entity.UserExtEntity
UserEntity baseUser = new
baseUser.setName("基础用户")
UserExtEntity extUser = new
extUser.setName("扩展用户")
echo "baseUser name: ${baseUser.getName()}"
echo "extUser name: ${extUser.getName()}"

// ------------------- 场景11: 使用 var 声明并 new -------------------
echo "=== 场景11: 使用 var 声明并 new ==="
import type com.gitee.huanminabc.test.nullchain.entity.UserExtEntity
var varUser:UserExtEntity=new
varUser.setName("var声明的用户")
echo "varUser name: ${varUser.getName()}"
echo "varUser age: ${varUser.getAge()}"

// ==================== 导出最终结果 ====================
echo "=== 测试完成，导出结果 ==="
export user
