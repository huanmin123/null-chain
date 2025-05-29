package com.gitee.huanminabc.nullchain.leaf.http;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.InputStream;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author huanmin
 * @date 2024/11/30
 */
@Slf4j
public class OkHttpBase<T> extends NullKernelAbstract<T> implements OkHttp {
    public static final String DEFAULT_THREAD_FACTORY_NAME = "$$$--NULL_DEFAULT_OKHTTP_SYNC--$$$";
    private OkHttpClient okHttpClient;
    private Map<String, String> headerMap;
    private Request.Builder request;
    private String url;

    private void setUrl(String url) {
        //加工url,如果结尾是/或者?那么去掉
        if (url.endsWith("/") || url.endsWith("?")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
    }

    public OkHttpBase(String url, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        this(DEFAULT_THREAD_FACTORY_NAME,url,linkLog, collect, taskList);
    }

    public OkHttpBase(String httpName, String url, StringBuilder linkLog, NullCollect nullChainCollect, NullTaskList taskList) {
        super(linkLog, nullChainCollect, taskList);
        this.okHttpClient = OkHttpBuild.getOkHttp(httpName);
        setUrl(url);
    }


    //设置连接超时时间(建议比系统最大超时时间低一些,比如rpc设置的15s断开,那么这里设置14s)
    public OkHttp connectTimeout(long time, TimeUnit timeUnit) {
        this.taskList.add((value)->{
            linkLog.append("connectTimeout->");
            okHttpClient = okHttpClient.newBuilder().connectTimeout(time, timeUnit).build();
            return  NullBuild.noEmpty(value);
        });
        return  this;
    }

    //设置写入超时时间(一般不调整)
    public OkHttp writeTimeout(long time, TimeUnit timeUnit) {
        this.taskList.add((value)->{
            linkLog.append("writeTimeout->");
            okHttpClient = okHttpClient.newBuilder().writeTimeout(time, timeUnit).build();
            return  NullBuild.noEmpty(value);
        });
        return  this;
    }

    //设置读取超时时间(一般不调整)
    public OkHttp readTimeout(long time, TimeUnit timeUnit) {
        this.taskList.add((value)->{
            linkLog.append("readTimeout->");
            okHttpClient = okHttpClient.newBuilder().readTimeout(time, timeUnit).build();
            return  NullBuild.noEmpty(value);
        });
        return this;
    }

    //设置代理
    //设置代理方式
    //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
    public OkHttp proxy(Proxy proxy) {
        this.taskList.add((value)->{
            linkLog.append("proxy->");
            okHttpClient = okHttpClient.newBuilder().proxy(proxy).build();
            return  NullBuild.noEmpty(value);
        });
        return this;
    }

    //自定义线程池
    //new ConnectionPool(500, 10, TimeUnit.MINUTES)
    private OkHttp connectionPool(ConnectionPool connectionPool) {
        this.taskList.add((value)->{
            linkLog.append("connectionPool->");
            okHttpClient = okHttpClient.newBuilder().connectionPool(connectionPool).build();
            return  NullBuild.noEmpty(value);
        });
        return this;
    }


    /**
     * 添加请求头
     *
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttp addHeader(String key, String value) {
        this.taskList.add((preValue)->{
            linkLog.append("addHeader->");
            if (headerMap == null) {
                headerMap = new LinkedHashMap<>(16);
            }
            headerMap.put(key, value);
            return  NullBuild.noEmpty(preValue);
        });
        return this;
    }


    public OkHttp get() {
        this.taskList.add((preValue)->{
            request = new Request.Builder().get();
            try {
                StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, preValue);
                request.url(valueToUrl.toString());
                linkLog.append("get->");
            } catch (Exception e) {
                linkLog.append("get? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return  NullBuild.noEmpty(preValue);
        });
        return this;
    }


    public OkHttp post(OkHttpPostEnum type) {
        this.taskList.add((preValue)->{
            try {
                RequestBody requestBody = OkHttpBuild.requestBodyHandel(type, preValue);
                request = new Request.Builder().post(requestBody).url(url);
                linkLog.append("post->");
            } catch (Exception e) {
                linkLog.append("post? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return  NullBuild.noEmpty(preValue);
        });
        return this;
    }


    public OkHttp put(OkHttpPostEnum type) {
        this.taskList.add((preValue)->{
            try {
                RequestBody requestBody = OkHttpBuild.requestBodyHandel(type, preValue);
                request = new Request.Builder().put(requestBody).url(url);
                linkLog.append("put->");
            } catch (Exception e) {
                linkLog.append("put? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return  NullBuild.noEmpty(preValue);
        });
        return this;
    }

    public OkHttp del() {
        this.taskList.add((preValue)->{
            try {
                request = new Request.Builder().delete();
                StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, preValue);
                request.url(valueToUrl.toString());
                linkLog.append("del->");
            } catch (Exception e) {
                linkLog.append("del? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            return  NullBuild.noEmpty(preValue);
        });
        return this;
    }


    /**
     * @param filePath 下载内容存储的路径
     */
    public NullChain<Boolean> downloadFile(String filePath) {
        this.taskList.add((__)->{
            if (Null.is(filePath)) {
                linkLog.append("downloadFile? ").append("本地文件路径不能为空");
                throw new NullChainException(linkLog.toString());
            }

            try {
                OkHttpBuild.setHeader(headerMap, request);
                boolean b = OkHttpBuild.downloadFile(url, filePath, okHttpClient, request);
                linkLog.append("downloadFile->");
                return NullBuild.noEmpty(b);
            } catch (Exception e) {
                linkLog.append("downloadFile? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy( linkLog, collect, taskList);
    }

    //下载文件返回字节流
    public NullChain<byte[]> toBytes() {
        this.taskList.add((__)->{
            try {
                OkHttpBuild.setHeader(headerMap, request);
                byte[] bytes = OkHttpBuild.toBytes(url, okHttpClient, request);
                if (bytes == null) {
                    linkLog.append("toBytes? ").append("返回值为空");
                    return NullBuild.empty();
                }
                linkLog.append("toBytes->");
                return NullBuild.noEmpty(bytes);
            } catch (Exception e) {
                linkLog.append("toBytes? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy( linkLog, collect, taskList);
    }

    //下载文件返回inputStream
    public NullChain<InputStream> toInputStream() {
        this.taskList.add((__)->{
            try {
                OkHttpBuild.setHeader(headerMap, request);
                InputStream inputStream = OkHttpBuild.toInputStream(url, okHttpClient, request);
                if (inputStream == null) {
                    linkLog.append("toInputStream? ").append("返回值为空");
                    return NullBuild.empty();
                }
                linkLog.append("toInputStream->");
                return NullBuild.noEmpty(inputStream);
            } catch (Exception e) {
                linkLog.append("toInputStream? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy( linkLog, collect, taskList);
    }

    /**
     * 同步请求
     *
     * @return
     */
    public NullChain<String> toStr() {
        this.taskList.add((__)->{
            try {
                OkHttpBuild.setHeader(headerMap, request);
                String str = OkHttpBuild.toStr(url, okHttpClient, request);
                if (Null.is(str)) {
                    linkLog.append("toStr? ").append("返回值为空");
                    return NullBuild.empty();
                }
                linkLog.append("toStr->");
                return NullBuild.noEmpty(str);
            } catch (Exception e) {
                linkLog.append("toStr? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.busy( linkLog, collect, taskList);
    }


}

