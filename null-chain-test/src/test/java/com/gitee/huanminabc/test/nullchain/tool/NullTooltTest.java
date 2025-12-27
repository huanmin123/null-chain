package com.gitee.huanminabc.test.nullchain.tool;

import com.gitee.huanminabc.jcommon.test.PathUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.tool.base64.Base64ByteDecodeTool;
import com.gitee.huanminabc.nullchain.tool.base64.Base64ByteEncodeTool;
import com.gitee.huanminabc.nullchain.tool.base64.Base64StrDecodeTool;
import com.gitee.huanminabc.nullchain.tool.base64.Base64StrEncodeTool;
import com.gitee.huanminabc.nullchain.tool.file.BytesToWriteFileTool;
import com.gitee.huanminabc.nullchain.tool.file.ReadFileToBytesTool;
import com.gitee.huanminabc.nullchain.tool.file.ReadFileToStrTool;
import com.gitee.huanminabc.nullchain.tool.file.StrToWriteFileTool;
import com.gitee.huanminabc.nullchain.tool.file.ZipToBytesByteTool;
import com.gitee.huanminabc.nullchain.tool.hash.MD5Tool;
import com.gitee.huanminabc.nullchain.tool.hash.Sha1Tool;
import com.gitee.huanminabc.nullchain.tool.hash.Sha256Tool;
import com.gitee.huanminabc.nullchain.tool.hash.Sha512Tool;
import com.gitee.huanminabc.nullchain.tool.object.DeserializeTool;
import com.gitee.huanminabc.nullchain.tool.object.SerializeTool;
import com.gitee.huanminabc.nullchain.tool.other.NumberToCnTool;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null 工具类测试
 * 
 * 测试各种工具类的使用，包括：
 * - Base64 编码/解码工具
 * - 文件读写工具
 * - 哈希工具
 * - 序列化工具
 * - 其他工具
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullTooltTest {
    UserEntity userEntity = new UserEntity();
    List<UserEntity> userEntityList = Arrays.asList(userEntity);

    @BeforeEach
    public void before() {
        userEntity.setId(1);
        userEntity.setName("huanmin");
        userEntity.setAge(33);
        userEntity.setDate(new Date());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("admin");
        roleEntity.setRoleDescription("123");
        roleEntity.setRoleCreationTime(new Date());
        userEntity.setRoleData(roleEntity);
    }

    // ========== Base64 工具测试 ==========

    @Test
    public void testBase64StrEncode() throws NullChainCheckException {
        String input = "test123";
        String encoded = Null.of(input)
                .tool(Base64StrEncodeTool.class)
                .getSafe();
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test
    public void testBase64StrDecode() throws NullChainCheckException {
        String input = "test123";
        String encoded = Null.of(input)
                .tool(Base64StrEncodeTool.class)
                .getSafe();
        
        String decoded = Null.of(encoded)
                .tool(Base64StrDecodeTool.class)
                .getSafe();
        assertEquals(input, decoded);
    }

    @Test
    public void testBase64ByteEncode() throws NullChainCheckException {
        byte[] input = "test123".getBytes();
        String encoded = Null.of(input)
                .tool(Base64ByteEncodeTool.class)
                .getSafe();
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test
    public void testBase64ByteDecode() throws NullChainCheckException {
        byte[] input = "test123".getBytes();
        String encoded = Null.of(input)
                .tool(Base64ByteEncodeTool.class)
                .getSafe();
        
        byte[] decoded = Null.of(encoded)
                .tool(Base64ByteDecodeTool.class)
                .getSafe();
        assertArrayEquals(input, decoded);
    }

    // ========== 哈希工具测试 ==========

    @Test
    public void testMD5() throws NullChainCheckException {
        String input = "test123";
        String hash = Null.of(input)
                .tool(MD5Tool.class)
                .getSafe();
        assertNotNull(hash);
        assertEquals(32, hash.length()); // MD5 哈希长度为 32
    }

    @Test
    public void testSha1() throws NullChainCheckException {
        String input = "test123";
        String hash = Null.of(input)
                .tool(Sha1Tool.class)
                .getSafe();
        assertNotNull(hash);
        assertEquals(40, hash.length()); // SHA1 哈希长度为 40
    }

    @Test
    public void testSha256() throws NullChainCheckException {
        String input = "test123";
        String hash = Null.of(input)
                .tool(Sha256Tool.class)
                .getSafe();
        assertNotNull(hash);
        assertEquals(64, hash.length()); // SHA256 哈希长度为 64
    }

    @Test
    public void testSha512() throws NullChainCheckException {
        String input = "test123";
        String hash = Null.of(input)
                .tool(Sha512Tool.class)
                .getSafe();
        assertNotNull(hash);
        assertEquals(128, hash.length()); // SHA512 哈希长度为 128
    }

    // ========== 文件工具测试 ==========

    @Test
    public void testStrToWriteFile() throws NullChainCheckException {
        String content = "test content";
        String tempFile = PathUtil.getCurrentProjectTestResourcesAbsolutePath("test_write.txt");
        
        Boolean result = Null.of(content)
                .tool(StrToWriteFileTool.class, tempFile, true)
                .type(Boolean.class)
                .getSafe();
        assertTrue(result);
        
        // 验证文件已创建
        File file = new File(tempFile);
        assertTrue(file.exists());
        
        // 清理
        file.delete();
    }

    @Test
    public void testReadFileToStr() throws NullChainCheckException {
        // 先创建一个测试文件
        String content = "test file content\nline 2";
        String tempFile = PathUtil.getCurrentProjectTestResourcesAbsolutePath("test_read.txt");
        
        Null.of(content)
                .tool(StrToWriteFileTool.class, tempFile, false)
                .type(Boolean.class)
                .getSafe();
        
        // 读取文件
        String readContent = Null.of(tempFile)
                .tool(ReadFileToStrTool.class)
                .getSafe();
        assertNotNull(readContent);
        assertTrue(readContent.contains("test file content"));
        
        // 清理
        new File(tempFile).delete();
    }

    @Test
    public void testReadFileToBytes() throws NullChainCheckException {
        // 先创建一个测试文件
        byte[] content = "test file content".getBytes();
        String tempFile = PathUtil.getCurrentProjectTestResourcesAbsolutePath("test_read_bytes.txt");
        
        Null.of(content)
                .tool(BytesToWriteFileTool.class, tempFile, false)
                .getSafe();
        
        // 读取文件
        byte[] readContent = Null.of(tempFile)
                .tool(ReadFileToBytesTool.class)
                .getSafe();
        assertNotNull(readContent);
        assertArrayEquals(content, readContent);
        
        // 清理
        new File(tempFile).delete();
    }

    @Test
    public void testBytesToWriteFile() throws NullChainCheckException {
        byte[] content = "test bytes content".getBytes();
        String tempFile = PathUtil.getCurrentProjectTestResourcesAbsolutePath("test_write_bytes.txt");
        
        Boolean result = Null.of(content)
                .tool(BytesToWriteFileTool.class, tempFile, false)
                .getSafe();
        assertTrue(result);
        
        // 验证文件已创建
        File file = new File(tempFile);
        assertTrue(file.exists());
        assertEquals(content.length, file.length());
        
        // 清理
        file.delete();
    }

    @Test
    public void testZipToBytesByte() throws NullChainCheckException {
        Map<String, byte[]> files = new HashMap<>();
        files.put("file1.txt", "content1".getBytes());
        files.put("file2.txt", "content2".getBytes());
        files.put("dir/", null); // 目录
        
        byte[] zipBytes = Null.of(files)
                .tool(ZipToBytesByteTool.class)
                .getSafe();
        assertNotNull(zipBytes);
        assertTrue(zipBytes.length > 0);
    }

    // ========== 序列化工具测试 ==========

    @Test
    public void testSerialize() throws NullChainCheckException {
        byte[] bytes = Null.of(userEntity)
                .type(Serializable.class)
                .tool(SerializeTool.class)
                .getSafe();
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    public void testDeserialize() throws NullChainCheckException {
        byte[] bytes = Null.of(userEntity)
                .type(Serializable.class)
                .tool(SerializeTool.class)
                .getSafe();
        
        UserEntity deserialized = Null.of(bytes)
                .tool(DeserializeTool.class)
                .type(UserEntity.class)
                .getSafe();
        assertNotNull(deserialized);
        assertEquals(userEntity.getId(), deserialized.getId());
        assertEquals(userEntity.getName(), deserialized.getName());
        assertEquals(userEntity.getAge(), deserialized.getAge());
    }

    // ========== 其他工具测试 ==========

    @Test
    public void testNumberToCn() throws NullChainCheckException {
        Integer number = 123;
        String result = Null.of(number)
                .tool(NumberToCnTool.class)
                .getSafe();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testNumberToCnWithZero() throws NullChainCheckException {
        Integer number = 0;
        String result = Null.of(number)
                .tool(NumberToCnTool.class)
                .getSafe();
        assertNotNull(result);
    }

    @Test
    public void testNumberToCnWithLargeNumber() throws NullChainCheckException {
        Integer number = 123456789;
        String result = Null.of(number)
                .tool(NumberToCnTool.class)
                .getSafe();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ========== 工具链式调用测试 ==========

    @Test
    public void testToolChain() throws NullChainCheckException {
        String input = "test123";
        
        // 编码 -> 哈希
        String encoded = Null.of(input)
                .tool(Base64StrEncodeTool.class)
                .getSafe();
        
        String hash = Null.of(encoded)
                .tool(MD5Tool.class)
                .getSafe();
        
        assertNotNull(encoded);
        assertNotNull(hash);
        assertEquals(32, hash.length());
    }

    // ========== 错误处理测试 ==========

    @Test
    public void testReadFileToStrWithNonExistentFile() {
        String nonExistentFile = PathUtil.getCurrentProjectTestResourcesAbsolutePath("non_existent.txt");
        assertThrows(NullChainCheckException.class, () -> {
            Null.of(nonExistentFile)
                    .tool(ReadFileToStrTool.class)
                    .getSafe();
        });
    }

    @Test
    public void testReadFileToBytesWithNonExistentFile() {
        String nonExistentFile = PathUtil.getCurrentProjectTestResourcesAbsolutePath("non_existent.txt");
        assertThrows(NullChainCheckException.class, () -> {
            Null.of(nonExistentFile)
                    .tool(ReadFileToBytesTool.class)
                    .getSafe();
        });
    }

    @Test
    public void testToolWithNullInput() {
        String result = Null.of((String) null)
                .tool(MD5Tool.class)
                .orElse("default");
        assertEquals("default", result);
    }
}
