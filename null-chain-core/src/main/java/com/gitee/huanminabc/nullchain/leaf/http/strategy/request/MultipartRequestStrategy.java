package com.gitee.huanminabc.nullchain.leaf.http.strategy.request;

import com.alibaba.fastjson.JSON;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.leaf.http.bo.FileBinary;
import com.gitee.huanminabc.nullchain.leaf.http.strategy.RequestStrategy;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.File;
import java.lang.reflect.Field;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 文件上传请求体构建策略
 * 
 * <p>实现 {@code multipart/form-data} 格式的请求体构建，支持：</p>
 * <ul>
 *   <li>自动识别文件类型字段（FileBinaryDTO、File、File[]、Collection&lt;File&gt; 等）</li>
 *   <li>文件类型字段自动作为文件上传，其他字段作为普通表单字段</li>
 *   <li>注意：字节类型（byte[]、byte[][]）必须使用 FileBinaryDTO</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class MultipartRequestStrategy implements RequestStrategy {
    
    @Override
    public RequestBody build(Object requestData, Request.Builder requestBuilder) throws Exception {
        // 构建 multipart 请求体
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        try {
            // 如果对象是 Map 类型，需要特殊处理
            if (requestData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapData = (Map<String, Object>) requestData;
                processMapData(mapData, bodyBuilder);
            } else {
                // 对象类型，使用反射和注解处理
                processObjectData(requestData, bodyBuilder);
            }
        } catch (Exception e) {
            throw new RuntimeException("构建请求体失败", e);
        }

        return bodyBuilder.build();
    }
    
    /**
     * 处理 Map 类型的数据
     * 
     * @param mapData Map 数据
     * @param bodyBuilder 请求体构建器
     */
    private void processMapData(Map<String, Object> mapData, MultipartBody.Builder bodyBuilder) {
        for (Map.Entry<String, Object> entry : mapData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value == null) {
                continue;
            }
            
            // 判断 value 是否是支持的文件类型
            if (OkHttpBuild.isFileType(value)) {
                // 是文件类型，使用 key 作为 addFormDataPart 的 name
                addFilesToBuilder(key, collectFileValues(value, key), bodyBuilder);
            } else {
                // 不是文件类型，作为普通表单字段处理
                addFormFieldToBuilder(key, value, bodyBuilder);
            }
        }
    }
    
    /**
     * 处理对象类型的数据
     * 
     * @param requestData 请求数据对象
     * @param bodyBuilder 请求体构建器
     */
    private void processObjectData(Object requestData, MultipartBody.Builder bodyBuilder) {
        // 添加普通表单字段
        List<FieldInfo> fieldInfos = getFormFields(requestData);
        for (FieldInfo fieldInfo : fieldInfos) {
            addFormFieldToBuilder(fieldInfo.name, fieldInfo.value, bodyBuilder);
        }

        // 自动识别并添加文件字段
        Map<String, List<FileBinary>> allFiles = extractAllFiles(requestData);
        if (!allFiles.isEmpty()) {
            for (Map.Entry<String, List<FileBinary>> entry : allFiles.entrySet()) {
                String fieldName = entry.getKey(); // 字段名（优先使用 @JSONField 的 name，否则使用字段名）
                List<FileBinary> files = entry.getValue();
                addFilesToBuilder(fieldName, files, bodyBuilder);
            }
        }
    }
    
    /**
     * 添加文件到请求体构建器
     * 
     * @param fieldName 字段名（作为 addFormDataPart 的 name）
     * @param files 文件列表
     * @param bodyBuilder 请求体构建器
     */
    private void addFilesToBuilder(String fieldName, List<FileBinary> files, MultipartBody.Builder bodyBuilder) {
        for (FileBinary fb : files) {
            if (fb == null || fb.content == null || fb.content.length == 0) {
                continue;
            }

            String contentType = getContentType(fb);
            MediaType mediaType = MediaType.parse(contentType);
            if (mediaType == null) {
                mediaType = MediaType.parse("application/octet-stream");
            }

            RequestBody rb = RequestBody.create(Objects.requireNonNull(mediaType), fb.content);
            bodyBuilder.addFormDataPart(fieldName, fb.fileName, rb);
        }
    }
    
    /**
     * 添加普通表单字段到请求体构建器
     * 
     * @param fieldName 字段名
     * @param value 字段值
     * @param bodyBuilder 请求体构建器
     */
    private void addFormFieldToBuilder(String fieldName, Object value, MultipartBody.Builder bodyBuilder) {
        String str;
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            str = String.valueOf(value);
        } else {
            str = JSON.toJSONString(value);
        }
        bodyBuilder.addFormDataPart(fieldName, str);
    }
    
    /**
     * 获取文件的 Content-Type
     * 
     * @param fileDTO 文件 DTO
     * @return Content-Type，如果无法确定则返回 "application/octet-stream"
     */
    private String getContentType(FileBinary fileDTO) {
        if (fileDTO.contentType != null && !fileDTO.contentType.isEmpty()) {
            return fileDTO.contentType;
        }
        
        try {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            String contentType = fileNameMap.getContentTypeFor(fileDTO.fileName);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
    
    @Override
    public boolean supports(OkHttpPostEnum type) {
        return type == OkHttpPostEnum.FILE;
    }
    
    
    /**
     * 字段信息封装类
     */
    private static class FieldInfo {
        public final String name;
        public final Object value;

        public FieldInfo(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
    
    /**
     * 从对象中提取所有文件字段
     * 自动识别文件类型字段（FileBinaryDTO、File、File[]、Collection<File> 等）
     *
     * @param obj 要处理的对象
     * @return 所有文件的列表（字段名 -> 文件列表的映射）
     */
    private Map<String, List<FileBinary>> extractAllFiles(Object obj) {
        Map<String, List<FileBinary>> allFiles = new HashMap<>();

        if (obj == null) {
            return allFiles;
        }

        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(obj);

                if (value == null) {
                    continue;
                }

                // 自动识别文件类型字段
                if (OkHttpBuild.isFileType(value)) {
                    // 获取字段名（优先使用 @JSONField 的 name，否则使用字段名）
                    String fieldName = OkHttpBuild.getFieldName(field);
                    List<FileBinary> files = collectFileValues(value, field.getName());
                    if (!files.isEmpty()) {
                        allFiles.put(fieldName, files);
                    }
                }
            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }

        return allFiles;
    }
    
    /**
     * 获取对象中需要包含在表单中的字段
     * 排除文件类型字段和 null 值字段
     *
     * @param obj 要处理的对象
     * @return 字段名和值的列表
     */
    private List<FieldInfo> getFormFields(Object obj) {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        Class<?> clazz = obj.getClass();

        // 获取所有声明的字段（包括私有字段）
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            try {
                // 设置可访问私有字段
                field.setAccessible(true);
                Object value = field.get(obj);

                // 跳过 null 值
                if (value == null) {
                    continue;
                }

                // 检查是否是文件类型：如果是文件类型则排除（文件类型会单独处理）
                if (OkHttpBuild.isFileType(value)) {
                    continue;
                }

                // 确定表单字段名：优先使用 @JSONField 的 name，否则使用字段名
                String fieldName = OkHttpBuild.getFieldName(field);

                fieldInfos.add(new FieldInfo(fieldName, value));

            } catch (IllegalAccessException e) {
                // 忽略无法访问的字段
            }
        }

        return fieldInfos;
    }
    
    /**
     * 收集文件值（支持 FileBinaryDTO、File、File[]、Collection<File> 等）
     * 
     * <p>注意：字节类型（byte[]、byte[][]）必须使用 FileBinaryDTO，不支持直接使用字节数组</p>
     *
     * @param value 字段值
     * @param fieldName 字段名
     * @return 文件列表
     */
    private List<FileBinary> collectFileValues(Object value, String fieldName) {
        List<FileBinary> files = new ArrayList<>();
        if (value == null) {
            return files;
        }
        
        // 支持 FileBinaryDTO
        if (value instanceof FileBinary) {
            files.add((FileBinary) value);
            return files;
        }
        
        // 支持 File
        if (value instanceof File) {
            File file = (File) value;
            if (!file.exists()) {
                return files;
            }
            try {
                byte[] content = Files.readAllBytes(file.toPath());
                String contentType = getContentTypeForFile(file);
                files.add(new FileBinary(file.getName(), content, contentType));
            } catch (Exception e) {
                // 忽略读取失败的文件
            }
            return files;
        }
        
        // 支持 File[]
        if (value instanceof File[]) {
            File[] fileArray = (File[]) value;
            for (File file : fileArray) {
                if (file == null || !file.exists()) {
                    continue;
                }
                try {
                    byte[] content = Files.readAllBytes(file.toPath());
                    String contentType = getContentTypeForFile(file);
                    files.add(new FileBinary(file.getName(), content, contentType));
                } catch (Exception e) {
                    // 忽略读取失败的文件
                }
            }
            return files;
        }
        
        // 支持 Collection<File> 或 Collection<FileBinaryDTO>
        if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            for (Object item : collection) {
                if (item == null) {
                    continue;
                }
                if (item instanceof FileBinary) {
                    files.add((FileBinary) item);
                } else if (item instanceof File) {
                    File file = (File) item;
                    if (!file.exists()) {
                        continue;
                    }
                    try {
                        byte[] content = Files.readAllBytes(file.toPath());
                        String contentType = getContentTypeForFile(file);
                        files.add(new FileBinary(file.getName(), content, contentType));
                    } catch (Exception e) {
                        // 忽略读取失败的文件
                    }
                }
            }
            return files;
        }
        
        return files;
    }
    
    /**
     * 获取文件的 Content-Type
     * 
     * @param file 文件对象
     * @return Content-Type，如果无法确定则返回 "application/octet-stream"
     */
    private String getContentTypeForFile(File file) {
        try {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            String contentType = fileNameMap.getContentTypeFor(file.getName());
            return contentType != null ? contentType : "application/octet-stream";
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }
}

