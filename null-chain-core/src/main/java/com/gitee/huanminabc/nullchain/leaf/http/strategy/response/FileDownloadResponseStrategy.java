package com.gitee.huanminabc.nullchain.leaf.http.strategy.response;

import com.gitee.huanminabc.nullchain.enums.OkHttpResponseEnum;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.ResponseStrategy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 文件下载响应处理策略
 * 
 * <p>处理HTTP响应为文件下载，将响应内容保存到指定文件路径。
 * 适用于下载文件、图片、文档等二进制内容。</p>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class FileDownloadResponseStrategy implements ResponseStrategy {
    
    @Override
    public Object handle(String url, OkHttpClient okHttpClient, Request.Builder request, int retryCount, long retryInterval, Object... params) {
        // 参数已在调用方校验，直接使用
        String filePath = String.valueOf(params[0]);
        
        return OkHttpBuild.retry(url, retryCount, retryInterval, () -> {
            try (Response response = okHttpClient.newCall(request.build()).execute()) {
                if (response.body() == null) {
                    return false;
                }
                FileOutputStream fileOutputStream = null;
                try (InputStream inputStream = response.body().byteStream()) {
                    File file = new File(filePath);
                    // 判断文件目录
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[2048];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    return true;
                } finally {
                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                }
            }
        });
    }
    
    @Override
    public boolean supports(OkHttpResponseEnum type) {
        return type == OkHttpResponseEnum.FILE_DOWNLOAD;
    }
}

