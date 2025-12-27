package com.gitee.huanminabc.test.nullchain.leaf.check;

import com.gitee.huanminabc.jcommon.exception.BizException;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NullCheck 多级判空工具测试类
 * 
 * @author huanmin
 * @since 1.1.2
 */
@Slf4j
public class NullCheckTest {

    private UserEntity userEntity;

    @BeforeEach
    public void setUp() {
        userEntity = new UserEntity();
        userEntity.setId(1);
        userEntity.setName("huanmin");
        userEntity.setAge(25);
        userEntity.setDate(new Date());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(221);
        roleEntity.setRoleName("admin");
        roleEntity.setRoleDescription("管理员");
        roleEntity.setRoleCreationTime(new Date());
        userEntity.setRoleData(roleEntity);
    }

    /**
     * 测试 is 方法：有字段为空
     */
    @Test
    public void testIsMethodHasNull() {
        userEntity.setName(null);
        boolean hasNull = Null.ofCheck(userEntity)
                .of(UserEntity::getId)
                .of(UserEntity::getName)
                .of(UserEntity::getAge)
                .is();
        assertTrue(hasNull, "当存在空字段时，is() 方法应该返回 true");
    }

    /**
     * 测试 is 方法：无字段为空
     */
    @Test
    public void testIsMethodNoNull() {
        boolean hasNull = Null.ofCheck(userEntity)
                .of(UserEntity::getId)
                .of(UserEntity::getName)
                .of(UserEntity::getAge)
                .is();
        assertFalse(hasNull, "当所有字段都不为空时，is() 方法应该返回 false");
    }

    /**
     * 测试自定义异常类型：有空值时抛出异常
     */
    @Test
    public void testCustomExceptionType() {
        userEntity.setName(null);
        assertThrows(BizException.class, () -> {
            Null.ofCheck(userEntity)
                    .of(UserEntity::getName)
                    .doThrow(BizException.class);
        }, "当存在空字段时，doThrow 应该抛出 BizException");
    }

    /**
     * 测试自定义异常类型：带前缀消息的异常
     */
    @Test
    public void testCustomExceptionType1() {
        userEntity.setName(null);
        BizException exception = assertThrows(BizException.class, () -> {
            Null.ofCheck(userEntity)
                    .of(UserEntity::getName)
                    .doThrow(BizException.class, "xxx缺少参数");
        }, "当存在空字段时，doThrow 应该抛出带前缀消息的 BizException");
        assertNotNull(exception.getMessage(), "异常消息不应该为空");
        assertTrue(exception.getMessage().contains("xxx缺少参数"), "异常消息应该包含前缀");
    }

    /**
     * 测试多个空值：应该抛出异常
     */
    @Test
    public void testMultipleNullMessages() {
        userEntity.setName(null);
        userEntity.setRoleData(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Null.ofCheck(userEntity)
                    .of(UserEntity::getId)
                    .of(UserEntity::getName)
                    .of(UserEntity::getAge)
                    .of(UserEntity::getRoleData)
                    .doThrow(RuntimeException.class);
        }, "当存在多个空字段时，doThrow 应该抛出 RuntimeException");
        assertNotNull(exception.getMessage(), "异常消息不应该为空");
    }

    /**
     * 测试无空值时不会抛出异常
     */
    @Test
    public void testDoThrowWithNoNull() {
        assertDoesNotThrow(() -> {
            Null.ofCheck(userEntity)
                    .of(UserEntity::getId)
                    .of(UserEntity::getName)
                    .of(UserEntity::getAge)
                    .doThrow(RuntimeException.class);
        }, "当所有字段都不为空时，doThrow 不应该抛出异常");
    }

    /**
     * 测试 map 方法：进入内部对象继续判空，所有字段都不为空
     */
    @Test
    public void testMapMethod() {
        boolean hasNull = Null.ofCheck(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getRoleName)
                .of(RoleEntity::getRoleDescription)
                .is();
        assertFalse(hasNull, "当所有字段都不为空时，is() 方法应该返回 false");
    }

    /**
     * 测试 map 方法：内部对象字段为空
     */
    @Test
    public void testMapMethodWithNullField() {
        userEntity.getRoleData().setRoleName(null);
        boolean hasNull = Null.ofCheck(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getRoleName)
                .of(RoleEntity::getRoleDescription)
                .is();
        assertTrue(hasNull, "当内部对象字段为空时，is() 方法应该返回 true");
    }

    /**
     * 测试 map 方法：内部对象本身为 null，应该抛出异常
     */
    @Test
    public void testMapMethodWithNullObject() {
        userEntity.setRoleData(null);
        assertThrows(RuntimeException.class, () -> {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .doThrow();
        }, "当内部对象为 null 时，doThrow 应该抛出异常");
    }

    /**
     * 测试 map 方法：内部对象本身为 null，带前缀消息的异常
     */
    @Test
    public void testMapMethodWithNullObject1() {
        userEntity.setRoleData(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .doThrow("参数错误");
        }, "当内部对象为 null 时，doThrow 应该抛出带前缀消息的异常");
        assertNotNull(exception.getMessage(), "异常消息不应该为空");
        assertTrue(exception.getMessage().contains("参数错误"), "异常消息应该包含前缀");
    }

    /**
     * 测试 map 方法：抛出异常
     */
    @Test
    public void testMapMethodDoThrow() {
        userEntity.getRoleData().setRoleName(null);
        BizException exception = assertThrows(BizException.class, () -> {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .doThrow(BizException.class);
        }, "当内部对象字段为空时，doThrow 应该抛出 BizException");
        assertNotNull(exception.getMessage(), "异常消息不应该为空");
    }

    /**
     * 测试 map 方法：带前缀消息的异常
     */
    @Test
    public void testMapMethodDoThrowWithPrefix() {
        userEntity.getRoleData().setRoleName(null);
        BizException exception = assertThrows(BizException.class, () -> {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .doThrow(BizException.class, "角色信息验证失败");
        }, "当内部对象字段为空时，doThrow 应该抛出带前缀消息的 BizException");
        assertNotNull(exception.getMessage(), "异常消息不应该为空");
        assertTrue(exception.getMessage().contains("角色信息验证失败"), "异常消息应该包含前缀");
    }

    /**
     * 测试 map 和 of 混合使用：多个字段为空
     */
    @Test
    public void testMapAndOfMixed() {
        userEntity.setName(null);
        userEntity.getRoleData().setRoleName(null);
        boolean hasNull = Null.ofCheck(userEntity)
                .of(UserEntity::getId)
                .of(UserEntity::getName)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getRoleName)
                .of(RoleEntity::getRoleDescription)
                .is();
        assertTrue(hasNull, "当存在多个空字段时（name 和 roleName 都为空），is() 方法应该返回 true");
    }

    /**
     * 测试多层 map 嵌套：所有字段都不为空
     */
    @Test
    public void testMultipleMapNested() {
        // 测试 map 后继续 of（虽然当前实体没有更深层，但测试链式调用）
        boolean hasNull = Null.ofCheck(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getId)
                .of(RoleEntity::getRoleName)
                .is();
        assertFalse(hasNull, "当所有字段都不为空时，is() 方法应该返回 false");
    }

    /**
     * 测试 map 方法：多个内部字段为空，应该抛出异常
     */
    @Test
    public void testMapMethodMultipleNullFields() {
        userEntity.getRoleData().setRoleName(null);
        userEntity.getRoleData().setRoleDescription(null);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .of(RoleEntity::getRoleDescription)
                    .doThrow(RuntimeException.class);
        }, "当存在多个空字段时，doThrow 应该抛出 RuntimeException");
        assertNotNull(exception.getMessage(), "异常消息不应该为空");
    }
}

