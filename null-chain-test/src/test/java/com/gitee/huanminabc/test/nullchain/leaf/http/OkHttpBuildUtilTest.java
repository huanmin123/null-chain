package com.gitee.huanminabc.test.nullchain.leaf.http;

import com.alibaba.fastjson.annotation.JSONField;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttpBuild;
import com.gitee.huanminabc.nullchain.leaf.http.dto.FileBinaryDTO;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OkHttpBuild 工具方法测试类
 * 
 * <p>测试 OkHttpBuild 中的共享工具方法：</p>
 * <ul>
 *   <li>getFieldName - 获取字段名（支持 @JSONField 映射，共享方法）</li>
 * </ul>
 * 
 * <p>注意：</p>
 * <ul>
 *   <li>extractAllFiles、getFormFields、collectFileValues 等方法已移到对应的策略类中，这些方法的测试请参考 {@link NullHttpStrategyTest} 中的策略测试</li>
 *   <li>请求头不再通过注解解析，请使用链式调用方式添加请求头（如 okHttp.header("key", "value")）</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class OkHttpBuildUtilTest {

    /**
     * 测试对象：包含文件字段（自动识别）
     */
    @Data
    public static class TestObjectWithFile {
        private String name;
        
        // 文件类型字段，自动识别
        private FileBinaryDTO file;
    }

    /**
     * 测试对象：包含多个文件字段（自动识别）
     */
    @Data
    public static class TestObjectWithMultipleFiles {
        private String description;
        
        // 文件类型字段，自动识别
        private FileBinaryDTO file1;
        
        // 文件类型字段，自动识别
        private List<FileBinaryDTO> files;
    }

    /**
     * 测试对象：包含 Map 字段
     */
    @Data
    public static class TestObjectWithHeaders {
        private String name;
        
        // Map 字段在 JSON 中会正常序列化
        private Map<String, String> headers;
    }

    /**
     * 测试对象：包含字段名映射
     */
    @Data
    public static class TestObjectWithFieldMapping {
        @JSONField(name = "mapped_name")
        private String name;
        
        @JSONField(name = "mapped_age")
        private int age;
    }

    /**
     * 测试对象：包含文件字段和 Map 字段
     */
    @Data
    public static class TestObjectWithAllSpecialFields {
        private String name;
        private String description;
        
        // Map 字段在 JSON 中会正常序列化
        private Map<String, String> headers;
        
        // 文件类型字段，自动识别
        private FileBinaryDTO file;
    }

    // ========== extractAllFiles 测试 ==========
    // 注意：extractAllFiles 方法已移到 MultipartRequestBodyStrategy 中，请参考 NullHttpStrategyTest 中的策略测试

    // ========== extractHeaders 测试 ==========
    // 注意：请求头不再通过注解解析，请使用链式调用方式添加请求头（如 okHttp.header("key", "value")）

    // ========== getFormFields 测试 ==========
    // 注意：getFormFields 方法已移到各个策略类中，请参考 NullHttpStrategyTest 中的策略测试

    // ========== getFieldName 测试 ==========

    /**
     * 测试获取字段名（使用 @JSONField 映射）
     */
    @Test
    public void testGetFieldNameWithJsonField() throws NoSuchFieldException {
        Field nameField = TestObjectWithFieldMapping.class.getDeclaredField("name");
        Field ageField = TestObjectWithFieldMapping.class.getDeclaredField("age");

        String nameFieldName = OkHttpBuild.getFieldName(nameField);
        String ageFieldName = OkHttpBuild.getFieldName(ageField);

        assertEquals("mapped_name", nameFieldName, "应该返回 @JSONField 映射的名称");
        assertEquals("mapped_age", ageFieldName, "应该返回 @JSONField 映射的名称");
    }

    /**
     * 测试获取字段名（没有 @JSONField 注解）
     */
    @Test
    public void testGetFieldNameWithoutJsonField() throws NoSuchFieldException {
        Field nameField = TestObjectWithFile.class.getDeclaredField("name");

        String fieldName = OkHttpBuild.getFieldName(nameField);

        assertEquals("name", fieldName, "应该返回原始字段名");
    }

    // ========== collectFileValues 测试 ==========
    // 注意：collectFileValues 方法已移到 MultipartRequestBodyStrategy 中，请参考 NullHttpStrategyTest 中的策略测试
}

