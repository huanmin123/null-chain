// 测试脚本：测试导入脚本的函数访问自己的全局变量

import nf utils

// 调用函数，函数内部会访问自己的全局变量 prefix 和 separator
String result = utils.formatUser("王五", 28)
export result

