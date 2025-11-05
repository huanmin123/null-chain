
//导入Java类型 , 常用的内置类型可以不用导入
import com.gitee.huanminabc.test.nullchain.entity.UserEntity

//导入任务, 起一个别名名这样后面操作任务的时候比较方便
task com.gitee.huanminabc.test.nullchain.task.Test1Task as test1
task com.gitee.huanminabc.test.nullchain.task.Test2Task as test2


//定义变量
String a="123"  //定义字符串

Integer b=123  //定义整数

Double c=123.0  //定义浮点数

Boolean c1=true  //定义布尔值
Boolean d=false  //定义布尔值

UserEntity user = new
user.setName("huanmin")
echo "打印用户:",user


//调用任务
run test1( a,b )
//调用任务绑定e
run test1( a,b ) -> e:String
//绑定已经存在的变量
run test1( a ,b )  -> f:String

//多任务并发, 任务1和任务2并发执行
run test1( a ,b ) ,test2( a ,b )
//绑定一个自定义变量
run test1( a ,b ) ,test2( a ,b )  -> g:Map
echo "打印并发任务:",g

echo "测试打印: ","____","123:{c}",\t,c,\t,123,\n


Map newMap = new
newMap.put("a",a)
newMap.put("b",b)
echo "打印Map:",newMap



echo "测试判断"

if c1 {
    echo "c & d 1"
    if c &&  d  || c {
        echo "c & d 2"
    }else {
         echo "c & d 22"
     }
} else if d {
    echo "c & d 3"
}else if c1 && d {
     echo "c & d 4"
 }else {
    echo "c & d 5"
}

echo "测试Switch"

switch a {
  case "123" , "456"
    echo "123"
        if d {
            echo "c & d 2"
        }else {
             echo "c & d 22"
        }
  case "712"
    echo "712"
  default
    echo "default"
}

echo "测试循环"
for i in 1..10 {
//      if i==3 {
//         break
//      }
     echo "value:{i}"
    for i in 1..10 {
        echo "value:{i}"
        if i==5 {
            continue
        }
//         if i==9 {
//            breakall
//         }
    }
}


echo "测试循环往Map中添加数据"
 Map  h = new
 for i in 1..10 {
   run test1() -> v:String
   h.put("test"+i,v)
 }

echo  "打印map:{h}"

String uid=UUID.randomUUID().toString()
echo uid

export a