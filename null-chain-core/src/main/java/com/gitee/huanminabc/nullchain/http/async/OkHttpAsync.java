package com.gitee.huanminabc.nullchain.http.async;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.NullKernelAsyncAbstract;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.http.NullHttp;
import com.gitee.huanminabc.nullchain.http.OkHttpBuild;
import lombok.Setter;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.InputStream;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class OkHttpAsync<T> extends NullKernelAsyncAbstract<T> implements NullHttp,OkHttpAsyncChain{
    public static final String DEFAULT_THREAD_FACTORY_NAME = "$$$--NULL_DEFAULT_OKHTTP_ASYNC--$$$";

    //创建一个空的OkHttpUtil
    public  static <T> OkHttpAsyncChain empty( StringBuilder linkLog, NullCollect collect) {
        OkHttpAsync<T> okHttp = new OkHttpAsync<>();
        okHttp.setNull(true);
        okHttp.setLinkLog(linkLog);
        okHttp.setCollect(collect);
        return okHttp;
    }

    private OkHttpClient okHttpClient;
    private Map<String, String> headerMap;
    private Request.Builder request;
    private String url;
    @Setter
    private T value;

    public void setUrl(String url) {
        //加工url,如果结尾是/或者?那么去掉
        if (url.endsWith("/") || url.endsWith("?")) {
            url = url.substring(0, url.length() - 1);
        }
        this.url = url;
    }

    @Setter
    protected CompletableFuture<Object> completableFuture;

    public OkHttpAsync() {
        this.okHttpClient = OkHttpBuild.getOkHttp(DEFAULT_THREAD_FACTORY_NAME);
    }
    public OkHttpAsync(String httpName) {
        this.okHttpClient = OkHttpBuild.getOkHttp(httpName);
    }



    //设置连接超时时间(建议比系统最大超时时间低一些,比如rpc设置的15s断开,那么这里设置14s)
    public OkHttpAsync connectTimeout(long time, TimeUnit timeUnit) {
        if (isNull) {
            return this;
        }
        linkLog.append("connectTimeout->");
        okHttpClient = okHttpClient.newBuilder().connectTimeout(time, timeUnit).build();
        return this;
    }

    //设置写入超时时间(一般不调整)
    public OkHttpAsync writeTimeout(long time, TimeUnit timeUnit) {
        if (isNull) {
            return this;
        }
        linkLog.append("writeTimeout->");
        okHttpClient = okHttpClient.newBuilder().writeTimeout(time, timeUnit).build();
        return this;
    }

    //设置读取超时时间(一般不调整)
    public OkHttpAsync readTimeout(long time, TimeUnit timeUnit) {
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
    public OkHttpAsync proxy(Proxy proxy) {
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
    public OkHttpAsync addHeader(String key, String value) {
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


    public OkHttpAsync get() {
        if (isNull) {
            return this;
        }
        completableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            request = new Request.Builder().get();
            try {
                StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, value);
                request.url(valueToUrl.toString());
                linkLog.append("get->");
                return value;
            } catch (Exception e) {
                linkLog.append("get? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
         },getCT());
        return this;
    }



    public OkHttpAsync post(OkHttpPostEnum type) {
        if (isNull) {
            return this;
        }
        completableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                RequestBody requestBody = OkHttpBuild.requestBodyHandel(type,value);
                request = new Request.Builder().post(requestBody).url(url);
                linkLog.append("post->");
                return value;
            } catch (Exception e) {
                linkLog.append("post? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        },getCT());
        return this;
    }


    public OkHttpAsync put(OkHttpPostEnum type) {
        if (isNull) {
            return this;
        }
        completableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                RequestBody requestBody = OkHttpBuild.requestBodyHandel(type,value);
                request = new Request.Builder().put(requestBody).url(url);
                linkLog.append("put->");
                return value;
            } catch (Exception e) {
                linkLog.append("put? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        },getCT());

        return this;
    }

    public OkHttpAsync del() {
        if (isNull) {
            return this;
        }
        completableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                request = new Request.Builder().delete();
                StringBuilder valueToUrl = OkHttpBuild.valueToUrl(url, value);
                request.url(valueToUrl.toString());
                linkLog.append("del->");
                return value;
            } catch (Exception e) {
                linkLog.append("del? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        },getCT());
        return this;
    }



    //下载文件,到指定路径  ,注意这种方式是异步的,不用等待下载完成就可以执行后面的代码
    public NullChainAsync<Boolean> downloadFile(String filePath) {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        if (Null.is(filePath)) {
            linkLog.append("downloadFile? ").append("本地文件路径不能为空");
            throw new NullChainException(linkLog.toString());
        }
        CompletableFuture<Boolean> booleanCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            try {
                OkHttpBuild.setHeader(headerMap,request);
                boolean b = OkHttpBuild.downloadFile(url,filePath, okHttpClient, request);
                linkLog.append("downloadFile->");
                return b;
            } catch (Exception e) {
                throw new NullChainException(linkLog.append("downloadFileAsync? ").append(e.getMessage()).toString());
            }
        },getCT());
        return NullBuild.noEmptyAsync(booleanCompletableFuture, linkLog, currentThreadFactoryName,collect);
    }

    /**
     * 异步请求，有返回值
     */
    public NullChainAsync<String> toStr() {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<String> stringCompletableFuture = completableFuture.thenApplyAsync((value) -> {
            if (Null.is(value)) {
                return null;
            }
            String str = null;
            try {
                OkHttpBuild.setHeader(headerMap,request);
                str = OkHttpBuild.toStr(url, okHttpClient, request);
                if (Null.is(str)) {
                    linkLog.append("toStr? ").append("获取到的数据为空");
                    return null;
                }
            } catch (Exception e) {
                linkLog.append("toStr? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("toStr->");
            return str;
        },getCT());

        return NullBuild.noEmptyAsync(stringCompletableFuture, linkLog, currentThreadFactoryName,collect);
    }

    @Override
    public NullChainAsync<InputStream> toInputStream() {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<InputStream> inputStreamCompletableFuture = completableFuture.thenApplyAsync((a)->{
            if (Null.is(value)) {
                return null;
            }
            try {
                InputStream inputStream = OkHttpBuild.toInputStream(url,okHttpClient, request);
                if (Null.is(inputStream)) {
                    linkLog.append("toInputStream? ").append("获取输入流失败");
                     return null;
                 }
                linkLog.append("toInputStream->");
                return inputStream;
            } catch (Exception e) {
                linkLog.append("toInputStream? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.noEmptyAsync(inputStreamCompletableFuture, linkLog, currentThreadFactoryName,collect);
    }

    @Override
    public NullChainAsync<byte[]> toBytes() {
        if (isNull) {
            return NullBuild.emptyAsync(linkLog,collect);
        }
        CompletableFuture<byte[]> byteCompletableFuture = completableFuture.thenApplyAsync((a)->{
            if (Null.is(value)) {
                return null;
            }
            try {
                byte[] bytes = OkHttpBuild.toBytes(url,okHttpClient, request);
                if (Null.is(bytes)) {
                    linkLog.append("toBytes? ").append("获取到的数据为空");
                    return null;
                }
                linkLog.append("toBytes->");
                return bytes;
            } catch (Exception e) {
                linkLog.append("toBytes? ").append(e.getMessage());
                throw new NullChainException(linkLog.toString());
            }
        });
        return NullBuild.noEmptyAsync(byteCompletableFuture, linkLog, currentThreadFactoryName,collect);
    }


}
