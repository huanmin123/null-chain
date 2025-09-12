package com.gitee.huanminabc.nullchain.common;

import com.alibaba.fastjson.JSONWriter;
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

import javax.xml.ws.WebServiceException;
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
    //可以自行修改,用于打印日志的链路id,便于追踪
    public static Supplier<String> TRACE_ID_FUN = () -> "";

    private String traceId;
    {
        traceId = getContextId();
    }


    /**
     * 成功标志
     */
    private boolean success = true;

    /**
     * 返回处理消息
     */
    private String message = "操作成功！";

    /**
     * 返回代码
     */
    private Integer code =  ResponseStatusEnum.SUCCESS.getCode();

    /**
     * 返回数据对象 data
     */
    private T data;

    /**
     * 如果是正常的返回,否则抛出异常
     */
    @JSONField(serialize = false)
    @JsonIgnore
    public NullChain<T> getBody() {
        if (this.isSuccess()) {
            return Null.of(this.data);
        }
        throw new WebServiceException(this.getMessage());
    }

    /**
     * 支持对象继承NULLExt无NullChain方式安全获取数据
     * 如果是正常的返回,否则抛出异常
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
                    throw new WebServiceException("data is not NullExt");
                }
            }else{
                throw new WebServiceException("data is null");
            }
        }
        throw new WebServiceException(this.getMessage());
    }

    @JSONField(serialize = false)
    @JsonIgnore
    public boolean isNull() {
        return this.getBody().is();
    }

    //不是空的,如果不是空的就返回true
    @JSONField(serialize = false)
    @JsonIgnore
    public boolean nonNull() {
        return this.getBody().non();
    }

    /**
     * 默认错误码是500
     *
     * @param msg
     * @param <T>
     * @return
     */
    public static <T> NullResult<T> error(String msg) {
        return error(ResponseStatusEnum.FAILED.getCode(), msg);
    }

    public static <T> NullResult<T> error(int code) {
        return error(code, null);
    }
    public static <T> NullResult<T> error(ResponseStatusEnum code) {
        return error(code.getCode(), code.getMessage());
    }
    public static <T> NullResult<T> error(ResponseStatusEnum code, String msg) {
        return error(code.getCode(), msg);
    }
    public static <T> NullResult<T> error(int code, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setCode(code);
        r.setMessage(msg);
        r.setSuccess(false);
        return r;
    }

    public static <T> NullResult<Void> success() {
        NullResult<Void> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(ResponseStatusEnum.SUCCESS.getCode());
        r.setMessage("成功");
        return r;
    }

    public static <T> NullResult<T> success(NullChain<T> data) {
        return success(data, "成功");
    }

    public static <T> NullResult<T> success(T data) {
        return success(data, "成功");
    }

    public static <T> NullResult<T> success(NullChain<T> data, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(ResponseStatusEnum.SUCCESS.getCode());
        r.setData(data.get());
        r.setMessage(msg);
        return r;
    }

    public static <T> NullResult<T> success(T data, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(ResponseStatusEnum.SUCCESS.getCode());
        r.setData(data);
        r.setMessage(msg);
        return r;
    }
    public static <T> NullResult<T> success(Integer code, String msg) {
        NullResult<T> r = new NullResult<>();
        r.setSuccess(true);
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }

    private static String getContextId() {
        try {
            return TRACE_ID_FUN.get();
        } catch (Exception ignored) {
        }
        return "";
    }
}