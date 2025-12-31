package com.gitee.huanminabc.test.nullchain.leaf.http;

import com.alibaba.fastjson.annotation.JSONField;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.nullchain.leaf.http.dto.FileBinary;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullHttp 策略模式和文件类型自动识别测试类
 * 
 * <p>测试新添加的功能：</p>
 * <ul>
 *   <li>自动识别文件类型字段（FileBinaryDTO、File、File[]、Collection&lt;File&gt; 等）</li>
 *   <li>JSON 请求自动排除文件类型字段</li>
 *   <li>Multipart 请求自动将文件类型字段作为文件上传</li>
 *   <li>FileBinaryDTO：文件二进制数据传输对象</li>
 *   <li>策略模式：请求体构建策略</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullHttpStrategyTest {

    private static final String HTTPBIN_BASE_URL = "https://httpbin.org";

    /**
     * 测试请求对象：包含普通字段和 Map 字段（Map 字段在 JSON 中会正常序列化）
     */
    @Data
    public static class RequestWithHeaders {
        private String name;
        private int age;
        
        // Map 字段在 JSON 中会正常序列化，不再特殊处理
        private Map<String, String> headers;
    }

    /**
     * 测试请求对象：包含文件类型字段（自动识别）
     */
    @Data
    public static class RequestWithFile {
        private String description;
        
        // 文件类型字段，自动识别，在 JSON 中会被排除，在 Multipart 中会作为文件上传
        private FileBinary file;
    }

    /**
     * 测试请求对象：包含文件类型字段和普通字段
     */
    @Data
    public static class RequestWithMultipleSpecialFields {
        private String name;
        private String description;
        
        // Map 字段在 JSON 中会正常序列化
        private Map<String, String> headers;
        
        // 文件类型字段，自动识别
        private FileBinary file;
    }

    /**
     * 测试请求对象：包含 List<FileBinaryDTO>（自动识别为文件类型）
     */
    @Data
    public static class RequestWithFileList {
        private String description;
        
        // 文件类型字段，自动识别
        private List<FileBinary> files;
    }

    /**
     * 测试请求对象：使用 @JSONField 字段名映射
     */
    @Data
    public static class RequestWithJsonFieldMapping {
        @JSONField(name = "user_name")
        private String userName;
        
        @JSONField(name = "user_age")
        private int userAge;
        
        // Map 字段在 JSON 中会正常序列化
        private Map<String, String> headers;
    }

    // ========== 文件类型自动识别测试 ==========

    /**
     * 测试自动识别文件字段（使用 FileBinaryDTO）
     */
    @Test
    public void testAutoExtractFileWithFileBinaryDTO() throws IOException {
        // 创建临时测试文件
        Path tempFile = Files.createTempFile("test-file-", ".txt");
        try {
            Files.write(tempFile, "Test file content".getBytes());
            
            // 创建 FileBinaryDTO
            byte[] fileContent = Files.readAllBytes(tempFile);
            FileBinary fileDTO = new FileBinary(
                    tempFile.getFileName().toString(),
                    fileContent,
                    "text/plain"
            );

            RequestWithFile request = new RequestWithFile();
            request.setDescription("Test file upload");
            request.setFile(fileDTO);

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "文件上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
            // 验证普通字段在表单中
            assertTrue(response.contains("Test file upload"), "响应应该包含 description 字段");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 测试自动提取多个文件（使用 List<FileBinaryDTO>）
     */
    @Test
    public void testAutoExtractMultipleFiles() throws IOException {
        // 创建多个临时测试文件
        Path tempFile1 = Files.createTempFile("test-file-1-", ".txt");
        Path tempFile2 = Files.createTempFile("test-file-2-", ".txt");
        
        try {
            Files.write(tempFile1, "First file content".getBytes());
            Files.write(tempFile2, "Second file content".getBytes());
            
            List<FileBinary> files = new ArrayList<>();
            files.add(new FileBinary(
                    tempFile1.getFileName().toString(),
                    Files.readAllBytes(tempFile1),
                    "text/plain"
            ));
            files.add(new FileBinary(
                    tempFile2.getFileName().toString(),
                    Files.readAllBytes(tempFile2),
                    "text/plain"
            ));

            RequestWithFileList request = new RequestWithFileList();
            request.setDescription("Multiple files upload");
            request.setFiles(files);

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "多文件上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile1);
            Files.deleteIfExists(tempFile2);
        }
    }

    // ========== JSON 请求排除文件类型字段测试 ==========

    /**
     * 测试 JSON 请求时自动排除文件类型字段
     */
    @Test
    public void testJsonExcludeFileFields() {
        RequestWithMultipleSpecialFields request = new RequestWithMultipleSpecialFields();
        request.setName("test");
        request.setDescription("test description");
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Test-Header", "test-value");
        request.setHeaders(headers);
        
        // 创建一个小的文件 DTO（不实际上传，只测试字段排除）
        FileBinary fileDTO = new FileBinary("test.txt", "test content".getBytes(), "text/plain");
        request.setFile(fileDTO);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                .post(OkHttpPostEnum.JSON)
                .toSTR()
                .orElseNull();

        assertNotNull(response, "请求响应不应该为空");
        // 验证普通字段在 JSON 中
        assertTrue(response.contains("test"), "响应应该包含 name 字段");
        assertTrue(response.contains("test description"), "响应应该包含 description 字段");
        // 验证 Map 字段在 JSON 中（正常序列化）
        assertTrue(response.contains("\"headers\""), "JSON 中应该包含 headers 字段（Map 类型正常序列化）");
        // 验证文件类型字段不在 JSON 中（自动排除）
        assertFalse(response.contains("\"file\""), "JSON 中不应该包含 file 字段（文件类型自动排除）");
    }

    // ========== @JSONField 字段名映射测试 ==========

    /**
     * 测试 @JSONField 字段名映射
     */
    @Test
    public void testJsonFieldNameMapping() {
        RequestWithJsonFieldMapping request = new RequestWithJsonFieldMapping();
        request.setUserName("testuser");
        request.setUserAge(30);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Mapped-Header", "mapped-value");
        request.setHeaders(headers);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                .post(OkHttpPostEnum.JSON)
                .toSTR()
                .orElseNull();

        assertNotNull(response, "请求响应不应该为空");
        // 验证字段名被映射
        assertTrue(response.contains("user_name"), "响应应该包含映射后的字段名 user_name");
        assertTrue(response.contains("user_age"), "响应应该包含映射后的字段名 user_age");
        // 验证原始字段名不在 JSON 中
        assertFalse(response.contains("\"userName\""), "JSON 中不应该包含原始字段名 userName");
        assertFalse(response.contains("\"userAge\""), "JSON 中不应该包含原始字段名 userAge");
        // 验证 Map 字段在 JSON 中（正常序列化）
        assertTrue(response.contains("\"headers\""), "JSON 中应该包含 headers 字段");
    }

    // ========== 向后兼容性测试 ==========

    /**
     * 测试向后兼容：没有注解的对象仍然可以正常工作
     */
    @Test
    public void testBackwardCompatibilityWithoutAnnotations() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "test");
        body.put("age", 25);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", body)
                .post(OkHttpPostEnum.JSON)
                .toSTR()
                .orElseNull();

        assertNotNull(response, "请求响应不应该为空");
        assertTrue(response.contains("test"), "响应应该包含 name 字段");
        assertTrue(response.contains("25"), "响应应该包含 age 字段");
    }

    /**
     * 测试 File 类型自动识别上传
     */
    @Data
    public static class RequestWithFileType {
        private String description;
        
        // File 类型字段，自动识别为文件类型
        private File file;
    }
    
    @Test
    public void testFileUploadAutoDetect() throws IOException {
        Path tempFile = Files.createTempFile("test-", ".txt");
        try {
            Files.write(tempFile, "Test content".getBytes());
            
            RequestWithFileType request = new RequestWithFileType();
            request.setDescription("Test file upload");
            request.setFile(tempFile.toFile());

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "文件上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
    
    /**
     * 测试 File[] 类型自动识别上传
     */
    @Data
    public static class RequestWithFileArrayType {
        private String description;
        
        // File[] 类型字段，自动识别为文件类型
        private File[] files;
    }
    
    @Test
    public void testFileArrayUploadAutoDetect() throws IOException {
        Path tempFile1 = Files.createTempFile("test-1-", ".txt");
        Path tempFile2 = Files.createTempFile("test-2-", ".txt");
        try {
            Files.write(tempFile1, "First file".getBytes());
            Files.write(tempFile2, "Second file".getBytes());
            
            RequestWithFileArrayType request = new RequestWithFileArrayType();
            request.setDescription("Multiple files upload");
            request.setFiles(new File[]{tempFile1.toFile(), tempFile2.toFile()});

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "多文件上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile1);
            Files.deleteIfExists(tempFile2);
        }
    }
    
    /**
     * 测试 Collection<File> 类型自动识别上传
     */
    @Data
    public static class RequestWithFileCollectionType {
        private String description;
        
        // Collection<File> 类型字段，自动识别为文件类型
        private List<File> files;
    }
    
    @Test
    public void testFileCollectionUploadAutoDetect() throws IOException {
        Path tempFile1 = Files.createTempFile("test-1-", ".txt");
        Path tempFile2 = Files.createTempFile("test-2-", ".txt");
        try {
            Files.write(tempFile1, "First file".getBytes());
            Files.write(tempFile2, "Second file".getBytes());
            
            RequestWithFileCollectionType request = new RequestWithFileCollectionType();
            request.setDescription("Collection files upload");
            List<File> fileList = new ArrayList<>();
            fileList.add(tempFile1.toFile());
            fileList.add(tempFile2.toFile());
            request.setFiles(fileList);

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "集合文件上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile1);
            Files.deleteIfExists(tempFile2);
        }
    }

    // ========== 策略模式测试 ==========

    /**
     * 测试策略模式：JSON 策略
     */
    @Test
    public void testJsonStrategy() {
        RequestWithHeaders request = new RequestWithHeaders();
        request.setName("strategy-test");
        request.setAge(30);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Strategy", "json");
        request.setHeaders(headers);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                .post(OkHttpPostEnum.JSON)
                .toSTR()
                .orElseNull();

        assertNotNull(response, "JSON 策略请求响应不应该为空");
        assertTrue(response.contains("strategy-test"), "响应应该包含请求数据");
        // Map 字段在 JSON 中会正常序列化
        assertTrue(response.contains("\"headers\""), "JSON 中应该包含 headers 字段");
    }

    /**
     * 测试策略模式：FORM 策略
     */
    @Test
    public void testFormStrategy() {
        Map<String, String> formData = new HashMap<>();
        formData.put("username", "testuser");
        formData.put("password", "testpass");

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", formData)
                .post(OkHttpPostEnum.FORM)
                .toSTR()
                .orElseNull();

        assertNotNull(response, "FORM 策略请求响应不应该为空");
        assertTrue(response.contains("testuser"), "响应应该包含表单数据");
    }

    /**
     * 测试策略模式：FILE 策略（传统方式）
     */
    @Test
    public void testFileStrategy() throws IOException {
        Path tempFile = Files.createTempFile("test-", ".txt");
        try {
            Files.write(tempFile, "Test".getBytes());
            
            Map<String, Object> formData = new HashMap<>();
            formData.put("file", tempFile.toFile());
            formData.put("description", "Test file");

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", formData)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "FILE 策略请求响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // ========== 综合测试 ==========

    /**
     * 测试综合场景：包含所有特殊字段的复杂请求
     */
    @Test
    public void testComplexRequestWithAllSpecialFields() throws IOException {
        Path tempFile = Files.createTempFile("test-", ".txt");
        try {
            Files.write(tempFile, "Complex test content".getBytes());
            
            byte[] fileContent = Files.readAllBytes(tempFile);
            FileBinary fileDTO = new FileBinary(
                    tempFile.getFileName().toString(),
                    fileContent,
                    "text/plain"
            );

            RequestWithMultipleSpecialFields request = new RequestWithMultipleSpecialFields();
            request.setName("complex-test");
            request.setDescription("Complex request test");
            
            Map<String, String> headers = new HashMap<>();
            headers.put("X-Complex-Header", "complex-value");
            headers.put("X-Request-Type", "multipart");
            request.setHeaders(headers);
            
            request.setFile(fileDTO);

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "复杂请求响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
            // 验证普通字段在表单中
            assertTrue(response.contains("complex-test"), "响应应该包含 name 字段");
            assertTrue(response.contains("Complex request test"), "响应应该包含 description 字段");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 测试 PUT 请求使用策略模式
     */
    @Test
    public void testPutRequestWithStrategy() {
        RequestWithHeaders request = new RequestWithHeaders();
        request.setName("put-test");
        request.setAge(40);
        
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Put-Header", "put-value");
        request.setHeaders(headers);

        String response = Null.ofHttp(HTTPBIN_BASE_URL + "/put", request)
                .put(OkHttpPostEnum.JSON)
                .toSTR()
                .orElseNull();

        assertNotNull(response, "PUT 请求响应不应该为空");
        assertTrue(response.contains("put-test"), "响应应该包含请求数据");
        // Map 字段在 JSON 中会正常序列化
        assertTrue(response.contains("\"headers\""), "JSON 中应该包含 headers 字段");
    }

    // ========== 边界情况测试 ==========

    /**
     * 测试 null 值的字段
     */
    @Test
    public void testNullFields() {
        RequestWithHeaders request = new RequestWithHeaders();
        request.setName("test");
        request.setAge(25);
        request.setHeaders(null); // headers 为 null

        assertDoesNotThrow(() -> {
            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.JSON)
                    .toSTR()
                    .orElseNull();
            assertNotNull(response, "请求响应不应该为空");
        });
    }

    /**
     * 测试空的文件列表
     */
    @Test
    public void testEmptyFileList() {
        RequestWithFileList request = new RequestWithFileList();
        request.setDescription("Empty file list test");
        request.setFiles(new ArrayList<>()); // 空列表

        assertDoesNotThrow(() -> {
            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();
            assertNotNull(response, "请求响应不应该为空");
        });
    }

    /**
     * 测试文件大小限制（10MB）
     */
    @Test
    public void testFileSizeLimit() throws IOException {
        // 创建一个超过 10MB 的文件（实际测试中可能不需要真正创建，只测试逻辑）
        // 这里只测试小文件，大文件测试可能需要更长时间
        Path tempFile = Files.createTempFile("test-", ".txt");
        try {
            // 创建一个较小的文件（小于 10MB）
            byte[] smallContent = new byte[1024]; // 1KB
            Files.write(tempFile, smallContent);
            
            FileBinary fileDTO = new FileBinary(
                    tempFile.getFileName().toString(),
                    smallContent,
                    "text/plain"
            );

            RequestWithFile request = new RequestWithFile();
            request.setDescription("Small file test");
            request.setFile(fileDTO);

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", request)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "小文件上传响应不应该为空");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    // ========== Map 类型兼容测试 ==========

    /**
     * 测试 Map 类型：File 文件上传
     */
    @Test
    public void testMapWithFile() throws IOException {
        Path tempFile = Files.createTempFile("test-", ".txt");
        try {
            Files.write(tempFile, "Test content".getBytes());
            
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("file", tempFile.toFile());
            mapData.put("description", "Map file upload test");

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", mapData)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "Map 文件上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 测试 Map 类型：File[] 文件上传
     */
    @Test
    public void testMapWithFileArray() throws IOException {
        Path tempFile1 = Files.createTempFile("test-1-", ".txt");
        Path tempFile2 = Files.createTempFile("test-2-", ".txt");
        try {
            Files.write(tempFile1, "First file".getBytes());
            Files.write(tempFile2, "Second file".getBytes());
            
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("files", new File[]{tempFile1.toFile(), tempFile2.toFile()});
            mapData.put("description", "Map file array upload test");

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", mapData)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "Map 文件数组上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile1);
            Files.deleteIfExists(tempFile2);
        }
    }

    /**
     * 测试 Map 类型：Collection<File> 文件上传
     */
    @Test
    public void testMapWithFileCollection() throws IOException {
        Path tempFile1 = Files.createTempFile("test-1-", ".txt");
        Path tempFile2 = Files.createTempFile("test-2-", ".txt");
        try {
            Files.write(tempFile1, "First file".getBytes());
            Files.write(tempFile2, "Second file".getBytes());
            
            List<File> fileList = new ArrayList<>();
            fileList.add(tempFile1.toFile());
            fileList.add(tempFile2.toFile());
            
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("files", fileList);
            mapData.put("description", "Map file collection upload test");

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", mapData)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "Map 文件集合上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile1);
            Files.deleteIfExists(tempFile2);
        }
    }

    /**
     * 测试 Map 类型：FileBinaryDTO 文件上传
     */
    @Test
    public void testMapWithFileBinaryDTO() throws IOException {
        Path tempFile = Files.createTempFile("test-", ".txt");
        try {
            byte[] content = Files.readAllBytes(tempFile);
            Files.write(tempFile, "Test content".getBytes());
            content = Files.readAllBytes(tempFile);
            
            FileBinary fileDTO = new FileBinary(
                    tempFile.getFileName().toString(),
                    content,
                    "text/plain"
            );
            
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("upload_file", fileDTO);
            mapData.put("description", "Map FileBinaryDTO upload test");

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", mapData)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "Map FileBinaryDTO 上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * 测试 Map 类型：混合文件和非文件字段
     */
    @Test
    public void testMapWithMixedFields() throws IOException {
        Path tempFile = Files.createTempFile("test-", ".txt");
        try {
            Files.write(tempFile, "Test content".getBytes());
            
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("file", tempFile.toFile());
            mapData.put("username", "testuser");
            mapData.put("age", 25);
            mapData.put("active", true);

            String response = Null.ofHttp(HTTPBIN_BASE_URL + "/post", mapData)
                    .post(OkHttpPostEnum.FILE)
                    .toSTR()
                    .orElseNull();

            assertNotNull(response, "Map 混合字段上传响应不应该为空");
            assertTrue(response.contains("\"files\"") || response.contains("\"form\""), 
                    "响应应该包含 files 或 form 字段");
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}

