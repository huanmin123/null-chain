package com.gitee.huanminabc.test.nullchain.core;

import com.gitee.huanminabc.jcommon.base.SerializeUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullChain 序列化测试
 * 
 * 测试 NullChain 的序列化和反序列化功能
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullChainSerializeTest {

    private UserEntity userEntity;
    private RoleEntity roleEntity;

    @BeforeEach
    public void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setName("huanmin");
        userEntity.setAge(25);

        roleEntity = new RoleEntity();
        roleEntity.setId(221);
        roleEntity.setRoleName("admin");
        roleEntity.setRoleDescription("管理员");
        userEntity.setRoleData(roleEntity);
    }

    @SneakyThrows
    @Test
    public void testSerializeAndDeserialize() {
        NullChain<RoleEntity> chain = Null.of(userEntity).map(UserEntity::getRoleData);
        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);
        assertTrue(serialize.length > 0);

        NullChain<RoleEntity> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertFalse(deserialize.is());
        
        RoleEntity result = deserialize.get();
        assertNotNull(result);
        assertEquals("admin", result.getRoleName());
        assertEquals(221L, result.getId());
    }

    @SneakyThrows
    @Test
    public void testSerializeUserEntity() {
        UserEntity build = UserEntity.builder()
                .id(1)
                .age(2)
                .name("测试用户")
                .build();
        NullChain<UserEntity> chain = Null.of(build);

        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);
        assertTrue(serialize.length > 0);

        NullChain<UserEntity> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertFalse(deserialize.is());
        
        UserEntity result = deserialize.get();
        assertNotNull(result);
        assertEquals(1, result.getId().intValue());
        assertEquals(2, result.getAge().intValue());
        assertEquals("测试用户", result.getName());
    }

    @SneakyThrows
    @Test
    public void testSerializeWithNull() {
        NullChain<RoleEntity> chain = Null.of((UserEntity) null)
                .map(UserEntity::getRoleData);
        
        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);

        NullChain<RoleEntity> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertTrue(deserialize.is());
    }
}

