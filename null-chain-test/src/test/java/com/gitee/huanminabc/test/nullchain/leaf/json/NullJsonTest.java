package com.gitee.huanminabc.test.nullchain.leaf.json;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Null JSON 操作测试
 * 
 * 测试 JSON 相关的功能
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullJsonTest {

    private UserEntity userEntity;

    @BeforeEach
    public void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setName("huanmin");
        userEntity.setAge(25);
    }

    @Test
    public void testJson() {
        NullChain<UserEntity> userEntityChain = Null.of(userEntity);
        HashMap<String, Object> result = Null.of(userEntityChain)
                .json()
                .json(new HashMap<String, Object>())
                .get();
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        // 验证 JSON 转换后的内容
        assertTrue(result.containsKey("id") || result.containsKey("name"));
    }

    @Test
    public void testJsonWithNull() {
        HashMap<String, Object> result = Null.of((UserEntity) null)
                .json()
                .json(new HashMap<String, Object>())
                .orElse(new HashMap<>());
        
        assertNotNull(result);
    }
}

