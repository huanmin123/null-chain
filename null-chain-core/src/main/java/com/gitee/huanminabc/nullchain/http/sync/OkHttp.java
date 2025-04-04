package com.gitee.huanminabc.nullchain.http.sync;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullKernelAbstract;
import com.gitee.huanminabc.nullchain.http.NullHttp;
import com.gitee.huanminabc.nullchain.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
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
public class OkHttp<T> extends NullKernelAbstract<T> implements NullHttp, OkHttpChain {
    public static final String DEFAULT_THREAD_FACTORY_NAME = "$$$--NULL_DEFAULT_OKHTTP_SYNC--$$$";


    //创建一个空的OkHttpUtil
    public static <T> OkHttpChain empty(StringBuilder linkLog) {
        OkHttp<T> okHttp = new OkHttp<>();
        okHttp.setNull(true);
        okHttp.setLinkLog(linkLog);
        return okHttp;
    }

    private OkHttpClient okHttpClient;
    private Map<String, String> headerMap;
    private Request.Builder request;
    private String url;

    public void setUrl(String url) {
        //加工url,如果结尾是/或者?那么去掉
        if (url.endsWith("/") || url.endsWith("?")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
    }

    @Setter
    private T value;


    public OkHttp() {
        this.okHttpClient = OkHttpBuild.getOkHttp(OkHttp.DEFAULT_THREAD_FACTORY_NAME);
    }
    public OkHttp(String httpName) {
        this.okHttpClient = OkHttpBuild.getOkHttp(httpName);
    }


    //设置连接超时时间(建议比系统最大超时时间低一些,比如rpc设置的15s断开,那么这里设置14s)
    public OkHttpChain connectTimeout(long time, TimeUnit timeUnit) {
        if (isNull) {
            return this;
        }
        linkLog.append("connectTimeout->");
        okHttpClient = okHttpClient.newBuilder().connectTimeout(time, timeUnit).build();
        return this;
    }

    //设置写入超时时间(一般不调整)
    public OkHttpChain writeTimeout(long time, TimeUnit timeUnit) {
        if (isNull) {
            return this;
        }
        linkLog.append("writeTimeout->");
        okHttpClient = okHttpClient.newBuilder().writeTimeout(time, timeUnit).build();
        return this;
    }

    //设置读取超时时间(一般不调整)
    public OkHttpChain readTimeout(long time, TimeUnit timeUnit) {
        if (isNull) {
            return this;
        }
        linkLog.append("readTimeout->");
        okHttpClient = okHttpClient.newBuilder().readTimeout(time, timeUnit).build();
        return this;
    }

    //设置代理
    //设置代理方式
    //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
    public OkHttpChain proxy(Proxy proxy) {
        if (isNull) {
            return this;
        }
        linkLog.append("proxy->");
        okHttpClient = okHttpClient.newBuilder().proxy(proxy).build();
        return this;
    }

    //自定义线程池
    //new ConnectionPool(500, 10, TimeUnit.MINUTES)
    private void connectionPool(ConnectionPool connectionPool) {
        okHttpClient = okHttpClient.newBuilder().connectionPool(connectionPool).build();
    }


    /**
     * 添加请求头
     *
     * @param key   参数名
     * @param value 参数值
     * @return
     */
    public OkHttpChain addHeader(String key, String value) {
        if (isNull) {
            return this;
        }
        linkLog.append("addHeader->");
        if (headerMap == null) {
            headerMap = new LinkedHashMap<>(16);
        }
        headerMap.put(key, value);
        return this;
    }


    public OkHttpChain get() {
        if (isNull) {
            return this;
        }
        request = new Request.Builder().get();
        try {
            StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, value);
            request.url(valueToUrl.toString());
            linkLog.append("get->");
        } catch (Exception e) {
            linkLog.append("get? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }
        return this;
    }


    public OkHttpChain post(OkHttpPostEnum type) {
        if (isNull) {
            return this;
        }
        try {
            RequestBody requestBody = OkHttpBuild.requestBodyHandel(type, value);
            request = new Request.Builder().post(requestBody).url(url);
            linkLog.append("post->");
        } catch (Exception e) {
            linkLog.append("post? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }
        return this;
    }


    public OkHttpChain put(OkHttpPostEnum type) {
        if (isNull) {
            return this;
        }
        try {
            RequestBody requestBody = OkHttpBuild.requestBodyHandel(type, value);
            request = new Request.Builder().put(requestBody).url(url);
            linkLog.append("put->");
        } catch (Exception e) {
            linkLog.append("put? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }
        return this;
    }

    public OkHttpChain del() {
        if (isNull) {
            return this;
        }
        try {
            request = new Request.Builder().delete();
            StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, value);
            request.url(valueToUrl.toString());
            linkLog.append("del->");
        } catch (Exception e) {
            linkLog.append("del? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }
        return this;
    }


    /**
     * @param filePath 下载内容存储的路径
     */
    public NullChain<Boolean> downloadFile(String filePath) {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        if (Null.is(filePath)) {
            linkLog.append("downloadFile? ").append("本地文件路径不能为空");
            throw new NullChainException(linkLog.toString());
        }

        try {
            OkHttpBuild.setHeader(headerMap, request);
            boolean b = OkHttpBuild.downloadFile(url, filePath, okHttpClient, request);
            linkLog.append("downloadFile->");
            return NullBuild.noEmpty(b, linkLog, collect);
        } catch (Exception e) {
            linkLog.append("downloadFile? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }

    }

    //下载文件返回字节流
    public NullChain<byte[]> toBytes() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        try {
            OkHttpBuild.setHeader(headerMap, request);
            byte[] bytes = OkHttpBuild.toBytes(url, okHttpClient, request);
            if (bytes == null) {
                linkLog.append("toBytes? ").append("返回值为空");
                return NullBuild.empty(linkLog, collect);
            }
            linkLog.append("toBytes->");
            return NullBuild.noEmpty(bytes, linkLog, collect);
        } catch (Exception e) {
            linkLog.append("toBytes? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }
    }

    //下载文件返回inputStream
    public NullChain<InputStream> toInputStream() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        try {
            OkHttpBuild.setHeader(headerMap, request);
            InputStream inputStream = OkHttpBuild.toInputStream(url, okHttpClient, request);
            if (inputStream == null) {
                linkLog.append("toInputStream? ").append("返回值为空");
                return NullBuild.empty(linkLog, collect);
            }
            linkLog.append("toInputStream->");
            return NullBuild.noEmpty(inputStream, linkLog, collect);
        } catch (Exception e) {
            linkLog.append("toInputStream? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }
    }

    /**
     * 同步请求
     *
     * @return
     */
    public NullChain<String> toStr() {
        if (isNull) {
            return NullBuild.empty(linkLog, collect);
        }
        try {
            OkHttpBuild.setHeader(headerMap, request);
            String str = OkHttpBuild.toStr(url, okHttpClient, request);
            if (Null.is(str)) {
                linkLog.append("toStr? ").append("返回值为空");
                return NullBuild.empty(linkLog, collect);
            }
            linkLog.append("toStr->");
            return NullBuild.noEmpty(str, linkLog, collect);
        } catch (Exception e) {
            linkLog.append("toStr? ").append(e.getMessage());
            throw new NullChainException(linkLog.toString());
        }

    }


}

