// 测试脚本：测试复杂场景，多个脚本，函数相互调用

import nf math, string, complex

Integer squareValue = math.square(5)
String wrapped = string.wrap("test")
String processed = complex.process(3)

String result = wrapped + "|" + squareValue + "|" + processed
export result

