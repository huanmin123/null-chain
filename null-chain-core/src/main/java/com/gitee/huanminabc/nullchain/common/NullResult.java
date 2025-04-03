package com.gitee.huanminabc.nullchain.common;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.NullExt;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.enums.ResponseStatusEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.ws.WebServiceException;
import java.io.Serializable;
import java.util.function.Supplier;

/**
 * 接口返回数据格式
 *
 * @author huanmin
 * @date 2024/11/26
 */
@Slf4j
@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
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
        r.setData(NullBuild.getValue(data));
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