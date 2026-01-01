package com.gitee.huanminabc.nullchain.common;

/**
 * Null链日志消息常量类
 * 
 * <p>该类包含了Null链框架中所有使用的日志消息常量，用于统一管理日志输出格式。
 * 通过使用常量而不是硬编码字符串，提高了代码的可维护性和一致性。</p>
 * 
 * <h3>日志消息格式说明：</h3>
 * <ul>
 *   <li>以"?"结尾的消息表示操作失败或遇到空值</li>
 *   <li>以"->"结尾的消息表示操作成功</li>
 *   <li>消息前缀表示操作类型（如"Null.of"、"map"、"filter"等）</li>
 * </ul>
 * 
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 在代码中使用日志常量
 * linkLog.append(NullLog.OF_Q);        // 输出: " Null.of?"
 * linkLog.append(NullLog.OF_ARROW);    // 输出: " Null.of->"
 * }</pre>
 * 
 * @author huanmin
 * @since 1.1.1
 * @version 1.1.1
 */
public final class NullLog {
    
    /**
     * 私有构造函数，防止实例化
     */
    private NullLog() {}


    public static final String OF_Q = " Null.of?";
    public static final String OF_ARROW = " Null.of->";

    public static final String OF_STREAM_ARROW = " Null.ofStream->";
    public static final String OF_STREAM_UNSUPPORTED_SOURCE = "ofStream? Unsupported source type";

    public static final String TO_STREAM_ARROW = " Null.toStream->";

    public static final String OF_HTTP_Q = " Null.ofHttp?";
    public static final String OF_HTTP_ARROW = " Null.ofHttp->";

    public static final String OF_CALC_Q = " Null.ofCalc?";
    public static final String OF_CALC_ARROW = " Null.ofCalc->";

    // workflow/tool/task
    public static final String TOOL_Q = "tool? ";
    public static final String TOOL_ARROW = "tool->";
    public static final String TOOL_PARAM_VALIDATION_FAILED = " 工具参数校验失败";
    public static final String TOOL_INIT_FAILED = " 初始化失败: ";
    public static final String TOOL_RUN_FAILED = " 运行失败: ";

    public static final String TASK_IN = ">>";
    public static final String TASK_Q = "task? ";
    public static final String TASK_ARROW = "task->";
    public static final String TASK_PARAM_VALIDATION_FAILED = " 任务参数校验失败";

    public static final String NFTASK_Q = "nfTask? ";
    public static final String NFTASK_ARROW = "nfTask->";

    public static final String NFTASKS_Q = "nfTasks? ";

    // date leaf
    public static final String DATE_FORMAT_Q = "dateFormat? ";
    public static final String DATE_FORMAT_ARROW = "dateFormat->";
    public static final String DATE_OFFSET_Q = "dateOffset? ";
    public static final String DATE_OFFSET_ARROW = "dateOffset->";
    public static final String DATE_COMPARE_Q = "dateCompare? ";
    public static final String DATE_COMPARE_ARROW = "dateCompare->";
    public static final String DATE_BETWEEN_Q = "dateBetween? ";
    public static final String DATE_BETWEEN_ARROW = "dateBetween->";

    // stream leaf
    public static final String STREAM_MAP_Q = "map? ";
    public static final String STREAM_MAP_ARROW = "map->";
    public static final String STREAM_FILTER_Q = "filter? ";
    public static final String STREAM_FILTER_ARROW = "filter->";
    public static final String STREAM_SORTED_Q = "sorted? ";
    public static final String STREAM_SORTED_ARROW = "sorted->";
    public static final String STREAM_DISTINCT_Q = "distinct? ";
    public static final String STREAM_DISTINCT_ARROW = "distinct->";
    public static final String STREAM_LIMIT_Q = "limit? ";
    public static final String STREAM_LIMIT_ARROW = "limit->";
    public static final String STREAM_SKIP_Q = "skip? ";
    public static final String STREAM_SKIP_ARROW = "skip->";
    public static final String STREAM_THEN_Q = "then? ";
    public static final String STREAM_THEN_ARROW = "then->";
    public static final String STREAM_FLATMAP_Q = "flatMap? ";
    public static final String STREAM_FLATMAP_ARROW = "flatMap->";
    public static final String STREAM_MAP_TO_INT_Q = "mapToInt? ";
    public static final String STREAM_MAP_TO_INT_ARROW = "mapToInt->";
    public static final String STREAM_MAP_TO_LONG_Q = "mapToLong? ";
    public static final String STREAM_MAP_TO_LONG_ARROW = "mapToLong->";
    public static final String STREAM_MAP_TO_DOUBLE_Q = "mapToDouble? ";
    public static final String STREAM_MAP_TO_DOUBLE_ARROW = "mapToDouble->";

    public static final String STREAM_MAX_Q = "max? ";
    public static final String STREAM_MAX_ARROW = "max->";
    public static final String STREAM_FIND_FIRST_Q = "findFirst? ";
    public static final String STREAM_FIND_FIRST_ARROW = "findFirst->";
    public static final String STREAM_FIND_ANY_Q = "findAny? ";
    public static final String STREAM_FIND_ANY_ARROW = "findAny->";
    public static final String STREAM_REDUCE_Q = "reduce? ";
    public static final String STREAM_REDUCE_ARROW = "reduce->";
    public static final String STREAM_MIN_Q = "min? ";
    public static final String STREAM_MIN_ARROW = "min->";
    public static final String STREAM_ALL_MATCH_Q = "allMatch? ";
    public static final String STREAM_ALL_MATCH_ARROW = "allMatch->";
    public static final String STREAM_ANY_MATCH_Q = "anyMatch? ";
    public static final String STREAM_ANY_MATCH_ARROW = "anyMatch->";
    public static final String STREAM_NONE_MATCH_Q = "noneMatch? ";
    public static final String STREAM_NONE_MATCH_ARROW = "noneMatch->";
    public static final String STREAM_FOR_EACH_Q = "forEach? ";
    public static final String STREAM_COLLECT_Q = "collect? ";
    public static final String STREAM_COLLECT_ARROW = "collect->";
    public static final String STREAM_TO_LIST_Q = "toList? ";
    public static final String STREAM_TO_LIST_ARROW = "toList->";

    public static final String STREAM_TO_SET_Q = "toSet? ";
    public static final String STREAM_TO_SET_ARROW = "toSet->";
    public static final String STREAM_TO_ARRAY_Q = "toArray? ";
    public static final String STREAM_TO_ARRAY_ARROW = "toArray->";
    public static final String STREAM_FLATMAP_TO_INT_Q = "flatMapToInt? ";
    public static final String STREAM_FLATMAP_TO_INT_ARROW = "flatMapToInt->";
    public static final String STREAM_FLATMAP_TO_LONG_Q = "flatMapToLong? ";
    public static final String STREAM_FLATMAP_TO_LONG_ARROW = "flatMapToLong->";
    public static final String STREAM_FLATMAP_TO_DOUBLE_Q = "flatMapToDouble? ";
    public static final String STREAM_FLATMAP_TO_DOUBLE_ARROW = "flatMapToDouble->";
    public static final String STREAM_INT_MAP_Q = "intMap? ";
    public static final String STREAM_INT_MAP_ARROW = "intMap->";
    public static final String STREAM_INT_FLATMAP_Q = "intFlatMap? ";
    public static final String STREAM_INT_FLATMAP_ARROW = "intFlatMap->";
    public static final String STREAM_LONG_MAP_Q = "longMap? ";
    public static final String STREAM_LONG_MAP_ARROW = "longMap->";
    public static final String STREAM_LONG_FLATMAP_Q = "longFlatMap? ";
    public static final String STREAM_LONG_FLATMAP_ARROW = "longFlatMap->";
    public static final String STREAM_DOUBLE_MAP_Q = "doubleMap? ";
    public static final String STREAM_DOUBLE_MAP_ARROW = "doubleMap->";
    public static final String STREAM_DOUBLE_FLATMAP_Q = "doubleFlatMap? ";
    public static final String STREAM_DOUBLE_FLATMAP_ARROW = "doubleFlatMap->";
    public static final String STREAM_BOXED_Q = "boxed? ";
    public static final String STREAM_BOXED_ARROW = "boxed->";

    public static final String STREAM_PARALLEL_VALUE_NOT_STREAM = "parallel? value must be a Stream";
    public static final String STREAM_COLLECT_COLLECTOR_NULL = "collect? collector must not be null";

    // calculate leaf
    public static final String CALC_ADD_ARROW = "add->";
    public static final String CALC_SUB_ARROW = "subtract->";
    public static final String CALC_MUL_ARROW = "multiply->";
    public static final String CALC_DIV_Q = "divide? ";
    public static final String CALC_DIV_ARROW = "divide->";
    public static final String CALC_NEGATE_ARROW = "negate->";
    public static final String CALC_ABS_ARROW = "abs->";
    public static final String CALC_MAX_ARROW = "max->";
    public static final String CALC_MIN_ARROW = "min->";
    public static final String CALC_POW_ARROW = "pow->";
    public static final String CALC_ROUND_Q = "round? ";
    public static final String CALC_ROUND_ARROW = "round->";
    public static final String CALC_RESULT_PICK_VALUE_NULL = "result? pickValue取值器不能是空";
    public static final String CALC_MAP_Q = "map? ";
    public static final String CALC_MAP_ARROW = "map->";

    // json leaf
    public static final String JSON_Q = "json? ";
    public static final String JSON_ARROW = "json->";

    // http leaf
    public static final String HTTP_CONNECT_TIMEOUT_ARROW = "connectTimeout->";
    public static final String HTTP_WRITE_TIMEOUT_ARROW = "writeTimeout->";
    public static final String HTTP_READ_TIMEOUT_ARROW = "readTimeout->";
    public static final String HTTP_PROXY_ARROW = "proxy->";
    public static final String HTTP_CONNECTION_POOL_ARROW = "connectionPool->";
    public static final String HTTP_ADD_HEADER_ARROW = "addHeader->";
    public static final String HTTP_GET_ARROW = "get->";
    public static final String HTTP_GET_Q = "get? ";
    public static final String HTTP_POST_ARROW = "post->";
    public static final String HTTP_POST_Q = "post? ";
    public static final String HTTP_PUT_ARROW = "put->";
    public static final String HTTP_PUT_Q = "put? ";
    public static final String HTTP_DEL_ARROW = "del->";
    public static final String HTTP_DEL_Q = "del? ";
    public static final String HTTP_DOWNLOAD_FILE_ARROW = "downloadFile->";
    public static final String HTTP_DOWNLOAD_FILE_Q = "downloadFile? ";
    public static final String HTTP_DOWNLOAD_FILE_PATH_NULL = "downloadFile? 本地文件路径不能为空";
    public static final String HTTP_TO_BYTES_ARROW = "toBytes->";
    public static final String HTTP_TO_BYTES_Q = "toBytes? ";
    public static final String HTTP_TO_INPUTSTREAM_ARROW = "toInputStream->";
    public static final String HTTP_TO_INPUTSTREAM_Q = "toInputStream? ";
    public static final String HTTP_TO_STR_ARROW = "toStr->";
    public static final String HTTP_TO_STR_Q = "toStr? ";
    public static final String HTTP_TO_SSE_ARROW = "toSSE->";
    public static final String HTTP_TO_SSE_Q = "toSSE? ";

    public static final String HTTP_TO_WEBSOCKET_ARROW = "toWebSocket->";
    public static final String HTTP_TO_WEBSOCKET_Q = "toWebSocket? ";


    // core chain base
    public static final String CHAIN_OF_Q = "of?";
    public static final String CHAIN_OF_ARROW = "of->";
    public static final String CHAIN_OF_PARAM_NULL = "of? 传参不能为空";
    public static final String CHAIN_IFGO_Q = "ifGo?";
    public static final String CHAIN_IFGO_ARROW = "ifGo->";
    public static final String CHAIN_IFGO_PARAM_NULL = "ifGo? 传参不能为空";
    public static final String CHAIN_IFNEGO_Q = "ifNeGo?";
    public static final String CHAIN_IFNEGO_ARROW = "ifNeGo->";
    public static final String CHAIN_IFNEGO_PARAM_NULL = "ifNeGo? 传参不能为空";
    public static final String CHAIN_ISNULL_Q = "isNull?";
    public static final String CHAIN_ISNULL_ARROW = "isNull->";
    public static final String CHAIN_ISNULL_PARAM_NULL = "isNull? 传参不能为空";
    public static final String CHAIN_OFANY_Q = "ofAny? ";
    public static final String CHAIN_OFANY_ARROW = "ofAny->";
    public static final String CHAIN_OFANY_PARAM_NULL = "ofAny? 传参不能为空";
    public static final String CHAIN_OFANY_INDEX = "ofAny? 第";
    public static final String CHAIN_THEN_Q = "then? ";
    public static final String CHAIN_THEN_ARROW = "then->";
    public static final String CHAIN_THEN_PARAM_NULL = "then? 传参不能为空";
    public static final String CHAIN_THEN2_Q = "then2? ";
    public static final String CHAIN_THEN2_ARROW = "then2->";
    public static final String CHAIN_THEN2_PARAM_NULL = "then2? 传参不能为空";
    public static final String CHAIN_MAP_Q = "map?";
    public static final String CHAIN_MAP_ARROW = "map->";
    public static final String CHAIN_MAP_PARAM_NULL = "map? 传参不能为空";
    public static final String CHAIN_MAP2_Q = "map2?";
    public static final String CHAIN_MAP2_ARROW = "map2->";
    public static final String CHAIN_MAP2_PARAM_NULL = "map2? 传参不能为空";
    public static final String CHAIN_FLATCHAIN_Q = "flatChain?";
    public static final String CHAIN_FLATCHAIN_ARROW = "flatChain->";
    public static final String CHAIN_FLATCHAIN_PARAM_NULL = "flatChain? 传参不能为空";
    public static final String CHAIN_FLATOPTIONAL_Q = "flatOptional?";
    public static final String CHAIN_FLATOPTIONAL_ARROW = "flatOptional->";
    public static final String CHAIN_FLATOPTIONAL_PARAM_NULL = "flatOptional? 传参不能为空";
    public static final String CHAIN_OR_Q = "or?";
    public static final String CHAIN_OR_ARROW = "or->";
    public static final String CHAIN_OR_PARAM_NULL = "or? 传参不能为空";

    // copy leaf
    public static final String COPY_ARROW = "copy->";
    public static final String COPY_Q = "copy? ";
    public static final String DEEP_COPY_ARROW = "deepCopy->";
    public static final String DEEP_COPY_Q = "deepCopy? ";
    public static final String PICK_ARROW = "pick->";
    public static final String PICK_Q = "pick? ";
    public static final String PICK_PARAM_NULL = "pick? 传参不能为空";

    // finality leaf
    public static final String IF_PRESENT_Q = "...ifPresent? ";
    public static final String IF_PRESENT_OR_ELSE_ACTION_Q = "...ifPresentOrElse-action? ";
    public static final String IF_PRESENT_OR_ELSE_EMPTY_ACTION_Q = "...ifPresentOrElse-emptyAction? ";
    public static final String CAPTURE_PARAM_NULL = "...capture? 参数不能为空";
    public static final String GET_SAFE_Q = "...getSafe? ";
    public static final String GET_SAFE_EXCEPTION_NULL = "...getSafe? 异常处理器不能为空";
    public static final String OR_ELSE_DEFAULT_NULL = "...orElse? 默认值不能是空的";
    public static final String OR_ELSE_SUPPLIER_NULL = "...orElse? 默认值不能为空";
    public static final String OR_ELSE_Q = "...orElse? ";

    // convert leaf
    public static final String TYPE_ARROW = "type->";
    public static final String TYPE_Q = "type? ";
    public static final String TYPE_CLASS_NULL = "转换类型不能为空";
    public static final String TYPE_MISMATCH = "类型不匹配 ";

    // kernel ext
    public static final String NULL_EXT_Q = "NullExt?";
    public static final String NULL_EXT_DOT = " NullExt.";

    // collect
    public static final String NULL_COLLECT_GET_Q = "NullCollect.get?";
    public static final String NULL_COLLECT_GET_ARROW = "NullCollect.get->";

    // kernel abstract
    public static final String SERIALIZE_NULL_VALUE = "{} 序列化时发现值是空的";

    // kernel
    public static final String ASYNC_ARROW = "async->";

    // check leaf
    public static final String OF_CHECK = "Null.ofCheck";
    public static final String OF_CHECK_Q = "Null.ofCheck?";
    public static final String OF_CHECK_ARROW = "Null.ofCheck->";
    public static final String CHECK_ISNULL_Q = "isNull?";
    public static final String CHECK_ISNULL_ARROW = "isNull->";
    public static final String CHECK_ISNULL_PARAM_NULL = "isNull? 传参不能为空";
    public static final String CHECK_MAP_Q = "map?";
    public static final String CHECK_MAP_ARROW = "map->";
    public static final String CHECK_MAP_PARAM_NULL = "map? 传参不能为空";
    public static final String CHECK_DOTHROW_PARAM_NULL = "doThrow? 异常类型不能为空";
}
