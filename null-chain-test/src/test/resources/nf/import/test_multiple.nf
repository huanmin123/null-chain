// 测试脚本：测试导入多个脚本

import nf utils, math, config

Integer sum = math.add(10, 20)
String userInfo = utils.formatUser("李四", 30)
String appInfo = config.appName

String result = userInfo + "|" + sum + "|" + appInfo
export result

