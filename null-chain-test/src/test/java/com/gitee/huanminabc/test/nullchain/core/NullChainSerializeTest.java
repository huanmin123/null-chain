package com.gitee.huanminabc.test.nullchain.core;

import com.gitee.huanminabc.jcommon.base.SerializeUtil;
import com.gitee.huanminabc.jcommon.exception.CommonException;
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

        @SuppressWarnings("unchecked")
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

        @SuppressWarnings("unchecked")
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
        //异常包裹起来
        assertThrows(CommonException.class, () -> {
            NullChain<RoleEntity> chain = Null.of((UserEntity) null)
                    .map(UserEntity::getRoleData);
            SerializeUtil.serialize(chain);
        });
    }

    /**
     * 测试已执行任务链的序列化（利用lastResult缓存）
     * 
     * 验证：如果任务链已经执行过（调用了get()等方法），序列化时应该使用缓存结果，
     * 不会重复执行任务链，避免副作用。
     */
    @SneakyThrows
    @Test
    public void testSerializeWithExecutedChain() {
        // 创建链并执行，触发任务链执行，lastResult会被设置
        NullChain<RoleEntity> chain = Null.of(userEntity)
                .map(UserEntity::getRoleData);
        
        // 执行任务链，这会设置lastResult
        RoleEntity executedResult = chain.get();
        assertNotNull(executedResult);
        assertEquals("admin", executedResult.getRoleName());
        
        // 再次调用get()，验证使用缓存（不会重复执行）
        RoleEntity cachedResult = chain.get();
        assertNotNull(cachedResult);
        assertEquals("admin", cachedResult.getRoleName());
        
        // 序列化：应该使用lastResult缓存，不会重复执行任务
        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);
        assertTrue(serialize.length > 0);
        
        // 反序列化并验证
        @SuppressWarnings("unchecked")
        NullChain<RoleEntity> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertFalse(deserialize.is());
        
        RoleEntity deserializedResult = deserialize.get();
        assertNotNull(deserializedResult);
        assertEquals("admin", deserializedResult.getRoleName());
        assertEquals(221L, deserializedResult.getId());
    }

    /**
     * 测试未执行任务链的序列化
     * 
     * 验证：如果任务链未执行过（未调用get()等方法），序列化时会执行runTaskAll()获取结果。
     */
    @SneakyThrows
    @Test
    public void testSerializeWithUnexecutedChain() {
        // 创建链但不执行（不调用get()等方法）
        NullChain<RoleEntity> chain = Null.of(userEntity)
                .map(UserEntity::getRoleData);
        
        // 直接序列化：应该会执行runTaskAll()获取结果
        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);
        assertTrue(serialize.length > 0);
        
        // 反序列化并验证
        @SuppressWarnings("unchecked")
        NullChain<RoleEntity> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertFalse(deserialize.is());
        
        RoleEntity result = deserialize.get();
        assertNotNull(result);
        assertEquals("admin", result.getRoleName());
        assertEquals(221L, result.getId());
    }

    /**
     * 测试多次序列化同一对象
     * 
     * 验证：多次序列化应该使用缓存，不会重复执行任务链。
     */
    @SneakyThrows
    @Test
    public void testMultipleSerialization() {
        NullChain<UserEntity> chain = Null.of(userEntity);
        
        // 第一次序列化：会执行任务链
        byte[] serialize1 = SerializeUtil.serialize(chain);
        assertNotNull(serialize1);
        
        // 第二次序列化：应该使用lastResult缓存，不会重复执行
        byte[] serialize2 = SerializeUtil.serialize(chain);
        assertNotNull(serialize2);
        
        // 验证两次序列化的结果应该相同
        assertArrayEquals(serialize1, serialize2);
        
        // 反序列化验证
        @SuppressWarnings("unchecked")
        NullChain<UserEntity> deserialize1 = SerializeUtil.deserialize(serialize1, NullChain.class);
        @SuppressWarnings("unchecked")
        NullChain<UserEntity> deserialize2 = SerializeUtil.deserialize(serialize2, NullChain.class);
        
        assertEquals(deserialize1.get().getId(), deserialize2.get().getId());
        assertEquals(deserialize1.get().getName(), deserialize2.get().getName());
    }

    /**
     * 测试复杂链式操作的序列化
     * 
     * 验证：包含多个map操作的复杂链式操作可以正确序列化和反序列化。
     */
    @SneakyThrows
    @Test
    public void testSerializeComplexChain() {
        // 创建复杂的链式操作
        NullChain<String> chain = Null.of(userEntity)
                .map(UserEntity::getRoleData)
                .map(RoleEntity::getRoleName)
                .map(String::toUpperCase);
        
        // 执行链式操作
        String result = chain.get();
        assertNotNull(result);
        assertEquals("ADMIN", result);
        
        // 序列化
        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);
        assertTrue(serialize.length > 0);
        
        // 反序列化并验证
        @SuppressWarnings("unchecked")
        NullChain<String> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertFalse(deserialize.is());
        
        String deserializedResult = deserialize.get();
        assertNotNull(deserializedResult);
        assertEquals("ADMIN", deserializedResult);
    }

    /**
     * 测试空值序列化异常
     * 
     * 验证：如果链的值为null，序列化时应该抛出NullChainException。
     */
    @Test
    public void testSerializeNullValueException() {
        // 测试空链序列化
        assertThrows(CommonException.class, () -> {
            NullChain<UserEntity> emptyChain = Null.empty();
            SerializeUtil.serialize(emptyChain);
        });
        
        // 测试null值链序列化
        assertThrows(CommonException.class, () -> {
            NullChain<UserEntity> nullChain = Null.of((UserEntity) null);
            SerializeUtil.serialize(nullChain);
        });
        
        // 测试链式操作后结果为null的序列化
        assertThrows(CommonException.class, () -> {
            NullChain<RoleEntity> chain = Null.of((UserEntity) null)
                    .map(UserEntity::getRoleData);
            SerializeUtil.serialize(chain);
        });
    }

    /**
     * 测试反序列化后的功能
     * 
     * 验证：反序列化后的对象可以正常使用链式操作。
     */
    @SneakyThrows
    @Test
    public void testDeserializedChainFunctionality() {
        // 创建并序列化
        NullChain<UserEntity> chain = Null.of(userEntity);
        byte[] serialize = SerializeUtil.serialize(chain);
        
        // 反序列化
        @SuppressWarnings("unchecked")
        NullChain<UserEntity> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        
        // 验证反序列化后的对象可以正常使用
        assertFalse(deserialize.is());
        UserEntity result = deserialize.get();
        assertNotNull(result);
        assertEquals(1, result.getId().intValue());
        assertEquals("huanmin", result.getName());
        
        // 验证可以继续链式操作
        NullChain<String> nameChain = deserialize.map(UserEntity::getName);
        assertNotNull(nameChain);
        assertFalse(nameChain.is());
        assertEquals("huanmin", nameChain.get());
    }

    /**
     * 测试基本类型的序列化
     * 
     * 验证：基本类型的NullChain可以正确序列化和反序列化。
     */
    @SneakyThrows
    @Test
    public void testSerializePrimitiveType() {
        Integer value = 100;
        NullChain<Integer> chain = Null.of(value);
        
        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);
        
        @SuppressWarnings("unchecked")
        NullChain<Integer> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertFalse(deserialize.is());
        
        Integer result = deserialize.get();
        assertNotNull(result);
        assertEquals(100, result.intValue());
    }

    /**
     * 测试字符串类型的序列化
     * 
     * 验证：字符串类型的NullChain可以正确序列化和反序列化。
     */
    @SneakyThrows
    @Test
    public void testSerializeStringType() {
        String value = "测试字符串";
        NullChain<String> chain = Null.of(value);
        
        byte[] serialize = SerializeUtil.serialize(chain);
        assertNotNull(serialize);
        
        @SuppressWarnings("unchecked")
        NullChain<String> deserialize = SerializeUtil.deserialize(serialize, NullChain.class);
        assertNotNull(deserialize);
        assertFalse(deserialize.is());
        
        String result = deserialize.get();
        assertNotNull(result);
        assertEquals("测试字符串", result);
    }
}

