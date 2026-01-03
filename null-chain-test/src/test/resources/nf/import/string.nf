// 字符串处理工具脚本

// 默认分隔符
String defaultSeparator = ","
String defaultPrefix = "["
String defaultSuffix = "]"

// 连接字符串数组
fun join(String separator, String... parts)String {
    String result = ""
    Integer count = 0
    for part in parts {
        if count > 0 {
            result = result + separator
        }
        result = result + part
        count = count + 1
    }
    return result
}

// 重复字符串
fun repeat(String str, Integer times)String {
    String result = ""
    Integer start = 1
    Integer end = times
    for i in start..end {
        result = result + str
    }
    return result
}

// 包装字符串（支持默认参数：1个参数使用默认前缀和后缀，3个参数使用自定义前缀和后缀）
fun wrap(String str, String... args)String {
    Integer count = 0
    String prefix = ""
    String suffix = ""
    for arg in args {
        if count == 0 {
            prefix = arg
        } else if count == 1 {
            suffix = arg
        }
        count = count + 1
    }
    if count == 0 {
        return defaultPrefix + str + defaultSuffix
    } else if count == 2 {
        return prefix + str + suffix
    } else {
        return defaultPrefix + str + defaultSuffix
    }
}

