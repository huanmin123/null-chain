package com.gitee.huanminabc.test.nullchain.core;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullChainException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserExtEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullChain核心功能测试类
 * 
 * @author huanmin
 * @since 1.1.2
 */
public class NullChainTest {

    private UserEntity userEntity;
    private RoleEntity roleEntity;

    @BeforeEach
    public void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setName("huanmin");
        userEntity.setAge(25);
        userEntity.setDate(new Date());

        roleEntity = new RoleEntity();
        roleEntity.setId(221);
        roleEntity.setRoleName("admin");
        roleEntity.setRoleDescription("管理员");
        roleEntity.setRoleCreationTime(new Date());
        userEntity.setRoleData(roleEntity);
    }

    // ========== of() 方法测试 ==========

    @Test
    public void testOfWithNull() {
        NullChain<String> chain = Null.of((String) null);
        assertTrue(chain.is());
        assertNull(chain.orElseNull());
    }

    @Test
    public void testOfWithValue() {
        NullChain<String> chain = Null.of("test");
        assertFalse(chain.is());
        assertEquals("test", chain.get());
    }

    @Test
    public void testOfWithCollection() {
        List<String> list = Arrays.asList("a", "b", "c");
        NullChain<List<String>> chain = Null.of(list);
        assertFalse(chain.is());
        assertEquals(list, chain.get());
    }

    @Test
    public void testOfWithEmptyCollection() {
        List<String> list = new ArrayList<>();
        NullChain<List<String>> chain = Null.of(list);
        assertTrue(chain.is());
    }

    @Test
    public void testOfWithArray() {
        String[] array = {"a", "b", "c"};
        NullChain<String[]> chain = Null.of(array);
        assertFalse(chain.is());
        assertArrayEquals(array, chain.get());
    }

    @Test
    public void testOfWithMap() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        NullChain<Map<String, String>> chain = Null.of(map);
        assertFalse(chain.is());
        assertEquals(map, chain.get());
    }

    @Test
    public void testOfWithOptional() {
        Optional<String> optional = Optional.of("test");
        NullChain<String> chain = Null.of(optional);
        assertFalse(chain.is());
        assertEquals("test", chain.get());
    }

    @Test
    public void testOfWithEmptyOptional() {
        Optional<String> optional = Optional.empty();
        NullChain<String> chain = Null.of(optional);
        assertTrue(chain.is());
    }

    // ========== empty() 方法测试 ==========

    @Test
    public void testEmpty() {
        NullChain<String> chain = Null.empty();
        assertTrue(chain.is());
        assertNull(chain.orElseNull());
    }

    // ========== map() 方法测试 ==========

    @Test
    public void testMap() {
        String name = Null.of(userEntity)
                .map(UserEntity::getName)
                .get();
        assertEquals("huanmin", name);
    }

    @Test
    public void testMapWithNull() {
        userEntity.setName(null);
        NullChain<String> chain = Null.of(userEntity)
                .map(UserEntity::getName);
        assertTrue(chain.is());
    }

    @Test
    public void testMapWithBiFunction() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        String value = Null.of(map)
                .map(Map::get, "key")
                .get();
        assertEquals("value", value);
    }

    @Test
    public void testMapChain() {
        String roleName = Null.of(userEntity)
                .map(UserEntity::getRoleData)
                .map(RoleEntity::getRoleName)
                .get();
        assertEquals("admin", roleName);
    }

    // ========== of() 条件判断测试 ==========

    @Test
    public void testOfCondition() {
        boolean result = Null.of(userEntity)
                .of(UserEntity::getName)
                .is();
        assertFalse(result);
    }

    @Test
    public void testOfConditionWithNull() {
        userEntity.setName(null);
        boolean result = Null.of(userEntity)
                .of(UserEntity::getName)
                .is();
        assertTrue(result);
    }

    // ========== ifGo() 方法测试 ==========

    @Test
    public void testIfGo() {
        String name = Null.of(userEntity)
                .ifGo(u -> u.getAge() > 18)
                .map(UserEntity::getName)
                .orElse("未成年");
        assertEquals("huanmin", name);
    }

    @Test
    public void testIfGoWithFalse() {
        String name = Null.of(userEntity)
                .ifGo(u -> u.getAge() < 18)
                .map(UserEntity::getName)
                .orElse("未成年");
        assertEquals("未成年", name);
    }

    // ========== ifNeGo() 方法测试 ==========

    @Test
    public void testIfNeGo() {
        String name = Null.of(userEntity)
                .ifNeGo(u -> u.getAge() < 18)
                .map(UserEntity::getName)
                .orElse("未成年");
        assertEquals("huanmin", name);
    }

    @Test
    public void testIfNeGoWithTrue() {
        String name = Null.of(userEntity)
                .ifNeGo(u -> u.getAge() > 18)
                .map(UserEntity::getName)
                .orElse("未成年");
        assertEquals("未成年", name);
    }

    // ========== isNull() 方法测试 ==========

    @Test
    public void testIsNull() {
        userEntity.setName(null);
        boolean result = Null.of(userEntity)
                .isNull(UserEntity::getName)
                .is();
        assertFalse(result);
    }

    @Test
    public void testIsNullWithNotNull() {
        boolean result = Null.of(userEntity)
                .isNull(UserEntity::getName)
                .is();
        assertTrue(result);
    }

    // ========== then() 方法测试 ==========

    @Test
    public void testThen() {
        final boolean[] executed = {false};
        String name = Null.of(userEntity)
                .then(() -> executed[0] = true)
                .map(UserEntity::getName)
                .get();
        assertTrue(executed[0]);
        assertEquals("huanmin", name);
    }

    // ========== peek() 方法测试 ==========

    @Test
    public void testPeek() {
        final String[] captured = {null};
        String name = Null.of(userEntity)
                .peek(u -> captured[0] = u.getName())
                .map(UserEntity::getName)
                .get();
        assertEquals("huanmin", captured[0]);
        assertEquals("huanmin", name);
    }

    // ========== flatChain() 方法测试 ==========

    @Test
    public void testFlatChain() {
        String roleName = Null.of(userEntity)
                .map(UserEntity::getId)
                .flatChain(id -> Null.of(roleEntity).map(RoleEntity::getRoleName))
                .get();
        assertEquals("admin", roleName);
    }

    // ========== flatOptional() 方法测试 ==========

    @Test
    public void testFlatOptional() {
        String name = Null.of(userEntity)
                .map(UserEntity::getId)
                .flatOptional(id -> Optional.of("id:" + id))
                .get();
        assertEquals("id:1", name);
    }

    @Test
    public void testFlatOptionalWithEmpty() {
        boolean result = Null.of(userEntity)
                .map(UserEntity::getId)
                .flatOptional(id -> Optional.empty())
                .is();
        assertTrue(result);
    }

    // ========== or() 方法测试 ==========

    @Test
    public void testOrWithValue() {
        String result = Null.of("test")
                .or("default")
                .get();
        assertEquals("test", result);
    }

    @Test
    public void testOrWithNull() {
        String result = Null.of((String) null)
                .or("default")
                .get();
        assertEquals("default", result);
    }

    @Test
    public void testOrWithSupplier() {
        String result = Null.of((String) null)
                .or(() -> "default")
                .get();
        assertEquals("default", result);
    }

    // ========== orElse() 方法测试 ==========

    @Test
    public void testOrElse() {
        String result = Null.of("test").orElse("default");
        assertEquals("test", result);
    }

    @Test
    public void testOrElseWithNull() {
        String result = Null.of((String) null).orElse("default");
        assertEquals("default", result);
    }

    // ========== orElseNull() 方法测试 ==========

    @Test
    public void testOrElseNull() {
        String result = Null.of("test").orElseNull();
        assertEquals("test", result);
    }

    @Test
    public void testOrElseNullWithNull() {
        String result = Null.of((String) null).orElseNull();
        assertNull(result);
    }

    // ========== get() 方法测试 ==========

    @Test
    public void testGet() {
        String name = Null.of("test").get();
        assertEquals("test", name);
    }

    @Test
    public void testGetWithNull() {
        assertThrows(NullChainException.class, () -> {
            Null.of((String) null).get();
        });
    }

    // ========== getSafe() 方法测试 ==========

    @Test
    public void testGetSafe() throws NullChainCheckException {
        String name = Null.of("test").getSafe();
        assertEquals("test", name);
    }

    @Test
    public void testGetSafeWithNull() {
        assertThrows(NullChainCheckException.class, () -> {
            Null.of((String) null).getSafe();
        });
    }

    // ========== is() 方法测试 ==========

    @Test
    public void testIs() {
        assertTrue(Null.of((String) null).is());
        assertFalse(Null.of("test").is());
    }

    // ========== non() 方法测试 ==========

    @Test
    public void testNon() {
        assertTrue(Null.of("test").non());
        assertFalse(Null.of((String) null).non());
    }

    // ========== ifPresent() 方法测试 ==========

    @Test
    public void testIfPresent() {
        final boolean[] executed = {false};
        Null.of("test").ifPresent(s -> executed[0] = true);
        assertTrue(executed[0]);
    }

    @Test
    public void testIfPresentWithNull() {
        final boolean[] executed = {false};
        Null.of((String) null).ifPresent(s -> executed[0] = true);
        assertFalse(executed[0]);
    }

    // ========== 异常处理测试 ==========

    @Test
    public void testMapWithException() {
        assertThrows(Exception.class, () -> {
            Null.of(userEntity)
                    .map(u -> {
                        throw new RuntimeException("test exception");
                    })
                    .get();
        });
    }

    @Test
    public void testPeekWithException() {
        // peek 中抛出异常应该传播
        assertThrows(RuntimeException.class, () -> {
            Null.of(userEntity)
                    .map(UserEntity::getRoleData)
                    .peek((data) -> {
                        data.setRoleName("default");
                        throw new RuntimeException("测试主动异常");
                    })
                    .get();
        });
    }

    @Test
    public void testGetWithNullValue() {
        // 当链中值为 null 时，get() 应该抛异常
        userEntity.setRoleData(null);
        assertThrows(NullChainException.class, () -> {
            Null.of(userEntity)
                    .map(UserEntity::getRoleData)
                    .map(RoleEntity::getRoleName)
                    .get();
        });
    }

    @Test
    public void testGetWithCustomException() {
        // get() 方法支持自定义异常构造器
        userEntity.getRoleData().setRoleName(null);
        assertThrows(RuntimeException.class, () -> {
            Null.of(userEntity)
                    .map(UserEntity::getRoleData)
                    .map(RoleEntity::getRoleName)
                    .get(RuntimeException::new);
        });
    }

    @Test
    public void testComplexChainWithNullField() throws NullChainCheckException {
        // 测试复杂链式调用中字段为 null 的情况
        userEntity.getRoleData().setRoleName(null);
        
        // 使用 of() 检查字段，如果为 null 则返回默认值
        UserEntity result = Null.of(userEntity)
                .map((data) -> {
                    data.getRoleData().setRoleName(null);
                    return data;
                })
                .of(UserEntity::getRoleData)
                .orElse(new UserEntity());
        assertNotNull(result);
    }

    @Test
    public void testComplexChainWithNullFieldGetSafe() {
        // 测试复杂链式调用中使用 getSafe() 的情况
        userEntity.getRoleData().setRoleName(null);
        
        assertThrows(NullChainCheckException.class, () -> {
            Null.of(userEntity)
                    .map((data) -> {
                        data.getRoleData().setRoleName(null);
                        return data;
                    })
                    .of(UserEntity::getRoleData)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .map(RoleEntity::getRoleName)
                    .getSafe();
        });
    }

    @Test
    public void testOfWithNullFunction() {
        assertThrows(NullChainException.class, () -> {
            Null.of(userEntity)
                    .of(null)
                    .get();
        });
    }

    @Test
    public void testMapWithNullFunction() {
        assertThrows(NullChainException.class, () -> {
            Null.of(userEntity)
                    .map(null)
                    .get();
        });
    }

    // ========== 复杂链式调用测试 ==========

    @Test
    public void testComplexChain() {
        String result = Null.of(userEntity)
                .ifGo(u -> u.getAge() > 18)
                .map(UserEntity::getRoleData)
                .ifGo(r -> r.getRoleName() != null)
                .map(RoleEntity::getRoleName)
                .map(String::toUpperCase)
                .orElse("UNKNOWN");
        assertEquals("ADMIN", result);
    }

    @Test
    public void testComplexChainWithNull() {
        userEntity.setRoleData(null);
        String result = Null.of(userEntity)
                .map(UserEntity::getRoleData)
                .map(RoleEntity::getRoleName)
                .orElse("UNKNOWN");
        assertEquals("UNKNOWN", result);
    }

    // ========== pick() 方法测试 ==========

    @Test
    public void testPick() {
        // 提取部分内容返回新的对象
        UserEntity picked = Null.of(userEntity)
                .pick(UserEntity::getName, UserEntity::getAge)
                .get();
        assertNotNull(picked);
        assertEquals("huanmin", picked.getName());
        assertEquals(Integer.valueOf(25), picked.getAge());
        // 其他字段应该为 null
        assertNull(picked.getId());
        assertNull(picked.getRoleData());
    }

    @Test
    public void testPickWithNull() {
        userEntity.setName(null);
        userEntity.setAge(null);
        // pick 会跳过 null 值
        UserEntity picked = Null.of(userEntity)
                .pick(UserEntity::getName, UserEntity::getAge)
                .get();
        assertNotNull(picked);
        assertNull(picked.getName());
        assertNull(picked.getAge());
    }

    // ========== collect() 方法测试 ==========

    @Test
    public void testCollect() {
        NullCollect collect = Null.of(userEntity)
                .map(UserEntity::getRoleData)
                .collect();
        
        NullChain<RoleEntity> roleEntityChain = collect.get(RoleEntity.class);
        assertFalse(roleEntityChain.is());
        assertEquals("admin", roleEntityChain.get().getRoleName());
        
        NullChain<UserEntity> userEntityChain = collect.get(UserEntity.class);
        assertFalse(userEntityChain.is());
        assertEquals("huanmin", userEntityChain.get().getName());

        NullCollect collect1 = Null.of(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getId)
                .of(RoleEntity::getRoleName)
                .of(RoleEntity::getRoleDescription)
                .collect();
        assertFalse(collect1.get(RoleEntity.class).is());


    }

    @Test
    public void testCollectWithNull() {
        userEntity.setRoleData(null);
        NullCollect collect = Null.of(userEntity)
                .map(UserEntity::getRoleData)
                .collect();
        
        NullChain<RoleEntity> roleEntityChain = collect.get(RoleEntity.class);
        assertTrue(roleEntityChain.is());
        
        // 使用 get 方法带消息参数
        assertThrows(NullChainException.class, () -> {
            collect.get(RoleEntity.class).get("roleEntity is null");
        });
    }

    // ========== orEmpty() 方法测试 ==========

    @Test
    public void testOrEmpty() {
        UserExtEntity empty = Null.orEmpty(null, UserExtEntity.class);
        assertNotNull(empty);
        assertTrue(empty.is());
    }

    @Test
    public void testOrEmptyWithValue() {
        UserExtEntity entity = new UserExtEntity();
        entity.setId(1);
        entity.setName("test");
        
        UserExtEntity result = Null.orEmpty(entity, UserExtEntity.class);
        assertNotNull(result);
        assertFalse(result.is());
        assertEquals(Integer.valueOf(1), result.getId());
        assertEquals("test", result.getName());
    }

    // ========== length() 方法测试 ==========

    @Test
    public void testLength() {
        // 对象返回 0
        int length = Null.of(userEntity).length();
        assertEquals(0, length);
        
        // 数字返回数字的字符串长度
        length = Null.of(123).length();
        assertEquals(3, length);
        
        length = Null.of(12345).length();
        assertEquals(5, length);
        
        // 字符串返回字符串长度
        length = Null.of("3333").length();
        assertEquals(4, length);
        
        length = Null.of("hello").length();
        assertEquals(5, length);
        
        // Date 返回 0
        length = Null.of(new Date()).length();
        assertEquals(0, length);
        
        // 集合返回集合大小
        List<String> list = Arrays.asList("a", "b", "c");
        length = Null.of(list).length();
        assertEquals(3, length);
        
        List<UserEntity> userList = new ArrayList<>();
        userList.add(userEntity);
        userList.add(userEntity);
        userList.add(null);
        length = Null.of(userList).length();
        assertEquals(3, length);
    }

    @Test
    public void testLengthWithNull() {
        int length = Null.of((String) null).length();
        assertEquals(0, length);
        
        length = Null.of((List<String>) null).length();
        assertEquals(0, length);
    }

    // ========== async() 方法测试 ==========

    @Test
    public void testAsync() throws InterruptedException {
        final boolean[] executed = {false};
        final String[] result = {null};
        
        NullChain<RoleEntity> chain = Null.of(userEntity)
                .async()
                .then(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    executed[0] = true;
                })
                .map(UserEntity::getRoleData);
        
        // 异步执行，立即返回
        assertFalse(executed[0]);
        //这个方法才是真的执行了
        chain.ifPresent(r -> result[0] = r.getRoleName());
        // 等待异步执行完成
        Thread.sleep(300);
        assertTrue(executed[0]);
        assertEquals("admin", result[0]);
    }

    @Test
    public void testAsyncChain() throws InterruptedException {
        final String[] result = {null};
        
        NullChain<String> chain = Null.of(userEntity)
                .async()
                .map(UserEntity::getRoleData)
                .map(RoleEntity::getRoleName);
        chain.ifPresent(r -> result[0] = r);
        // 等待异步执行
        Thread.sleep(100);
        assertEquals("admin", result[0]);
    }
}

