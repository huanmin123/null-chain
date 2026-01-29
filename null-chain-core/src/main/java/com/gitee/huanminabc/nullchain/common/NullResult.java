package com.gitee.huanminabc.nullchain.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.NullExt;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.enums.ResponseStatusEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

// import javax.xml.ws.WebServiceException; // 已移除：Java 9+ 不再包含此包
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * Null结果封装类 - 提供统一的接口返回数据格式
 * 
 * <p>该类提供了统一的接口返回数据格式，支持成功和失败两种状态的结果封装。
 * 通过标准化的返回格式，为API接口提供一致的数据结构。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>结果封装：封装接口返回的结果数据</li>
 *   <li>状态管理：管理成功和失败状态</li>
 *   <li>消息处理：提供错误消息和成功消息</li>
 *   <li>链路追踪：支持链路ID追踪</li>
 *   <li>JSON序列化：支持JSON序列化和反序列化</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>序列化支持：支持JSON序列化</li>
 *   <li>链路追踪：支持链路ID追踪</li>
 *   <li>状态管理：提供清晰的状态管理</li>
 * </ul>
 * 
 * @param <T> 结果数据的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see Serializable 序列化接口
 * @see ResponseStatusEnum 响应状态枚举
 */
@Slf4j
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
@JSONType(serialzeFeatures = SerializerFeature.WriteMapNullValue)
public class NullResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * 链路ID生成函数，可以自行修改，用于打印日志的链路ID，便于追踪
     * 
     * <p>默认返回空字符串，可以通过设置此函数来自定义链路ID的生成逻辑。</p>
     */
    public static Supplier<String> TRACE_ID_FUN = () -> "";

    /**
     * 链路追踪ID，用于追踪请求链路
     */
    private String traceId;
    {
        traceId = getContextId();
    }

    /**
     * 成功标志，true表示操作成功，false表示操作失败
     */
    private boolean success = true;

    /**
     * 返回处理消息，用于描述操作结果
     */
    private String message = "操作成功！";

    /**
     * 返回代码，用于标识操作状态
     */
    private Integer code =  ResponseStatusEnum.SUCCESS.getCode();

    /**
     * 返回数据对象，包含实际的业务数据
     */
    private T data;

    /**
     * 获取结果体，如果是正常的返回则返回NullChain，否则抛出异常
     * 
     * <p>此方法不会序列化到JSON中，仅用于内部获取数据。</p>
     * 
     * @return NullChain包装的数据，如果操作失败则抛出异常
     * @throws RuntimeException 如果操作失败
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public NullChain<T> getBody() {
        if (this.isSuccess()) {
            return Null.of(this.data);
        }
        throw new RuntimeException(this.getMessage());
    }

    /**
     * 支持对象继承NullExt无NullChain方式安全获取数据
     * 
     * <p>如果是正常的返回则返回数据对象，否则抛出异常。
     * 此方法要求data对象必须继承NullExt接口，否则会抛出异常。</p>
     * 
     * <p>此方法不会序列化到JSON中，仅用于内部获取数据。</p>
     * 
     * @return 数据对象，必须是NullExt类型
     * @throws RuntimeException 如果操作失败、数据为null或数据不是NullExt类型
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public T getExtBody() {
        if (this.isSuccess())  {
            if ( Null.non(this.data)){
                //对象如果没有继承NULLExt那么就会抛出异常
                if (this.data instanceof NullExt){
                    return this.data;
                }else{
                    throw new RuntimeException("data is not NullExt");
                }
            }else{
                throw new RuntimeException("data is null");
            }
        }
        throw new RuntimeException(this.getMessage());
    }

    /**
     * 判断结果数据是否为空
     * 
     * <p>此方法不会序列化到JSON中，仅用于内部判断。</p>
     * 
     * @return 如果数据为空返回true，否则返回false
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public boolean isNull() {
        return this.getBody().is();
    }

    /**
     * 判断结果数据是否不为空
     * 
     * <p>此方法不会序列化到JSON中，仅用于内部判断。</p>
     * 
     * @return 如果数据不为空返回true，否则返回false
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public boolean nonNull() {
        return this.getBody().non();
    }

    /**
     * 创建错误结果（使用默认错误码500）
     * 
     * @param <T> 结果数据类型
     * @param msg 错误消息
     * @return 错误结果对象
     */
    public static <T> NullResult<T> error(String msg) {
        return error(ResponseStatusEnum.FAILED.getCode(), msg);
    }

    /**
     * 创建错误结果（使用指定的错误码）
     * 
     * @param <T> 结果数据类型
     * @param code 错误码
     * @return 错误结果对象，消息为null
     */
    public static <T> NullResult<T> error(int code) {
        return error(code, null);
    }

    /**
     * 创建错误结果（使用响应状态枚举）
     * 
     * @param <T> 结果数据类型
     * @param code 响应状态枚举
     * @return 错误结果对象
     */
    public static <T> NullResult<T> error(ResponseStatusEnum code) {
        return error(code.getCode(), code.getMessage());
    }

    /**
     * 创建错误结果（使用响应状态枚举和自定义消息）
     * 
     * @param <T> 结果数据类型
     * @param code 响应状态枚举
     * @param msg 错误消息
     * @return 错误结果对象
     */
    public static <T> NullResult<T> error(ResponseStatusEnum code, String msg) {
        return error(code.getCode(), msg);
    }

    /**
     * 创建错误结果（使用指定的错误码和消息）
     * 
     * @param <T> 结果数据类型
     * @param code 错误码
     * @param msg 错误消息
     * @return 错误结果对象
     */
    public static <T> NullResult<T> error(int code, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setCode(code);
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    /**
     * 创建成功结果（无数据）
     * 
     * @param <T> 结果数据类型
     * @return 成功结果对象
     */
    public static <T> NullResult<Void> success() {
        NullResult<Void> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(ResponseStatusEnum.SUCCESS.getCode());
        r.setMessage("成功");
        return r;
    }

    /**
     * 创建成功结果（使用NullChain数据）
     * 
     * @param <T> 结果数据类型
     * @param data NullChain包装的数据
     * @return 成功结果对象
     */
    public static <T> NullResult<T> success(NullChain<T> data) {
        return success(data, "成功");
    }

    /**
     * 创建成功结果（使用数据对象）
     * 
     * @param <T> 结果数据类型
     * @param data 数据对象
     * @return 成功结果对象
     */
    public static <T> NullResult<T> success(T data) {
        return success(data, "成功");
    }

    /**
     * 创建成功结果（使用NullChain数据和自定义消息）
     * 
     * @param <T> 结果数据类型
     * @param data NullChain包装的数据
     * @param msg 成功消息
     * @return 成功结果对象
     */
    public static <T> NullResult<T> success(NullChain<T> data, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(ResponseStatusEnum.SUCCESS.getCode());
        r.setData(data.orElseNull());
        r.setMessage(msg);
        return r;
    }


    /**
     * 创建成功结果（使用数据对象和自定义消息）
     * 
     * @param <T> 结果数据类型
     * @param data 数据对象
     * @param msg 成功消息
     * @return 成功结果对象
     */
    public static <T> NullResult<T> success(T data, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(ResponseStatusEnum.SUCCESS.getCode());
        r.setData(data);
        r.setMessage(msg);
        return r;
    }

    /**
     * 创建成功结果（使用自定义代码和消息）
     * 
     * @param <T> 结果数据类型
     * @param code 成功代码
     * @param msg 成功消息
     * @return 成功结果对象
     */
    public static <T> NullResult<T> success(Integer code, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    /**
     * 获取上下文ID
     * 
     * <p>通过TRACE_ID_FUN函数获取链路追踪ID，如果获取失败则返回空字符串。</p>
     * 
     * @return 链路追踪ID
     */
    private static String getContextId() {
        try {
            return TRACE_ID_FUN.get();
        } catch (Exception ignored) {
        }
        return "";
    }
}