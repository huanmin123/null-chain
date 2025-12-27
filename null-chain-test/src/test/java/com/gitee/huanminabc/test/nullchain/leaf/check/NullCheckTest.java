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
        System.out.println(hasNull); //true
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
        System.out.println(hasNull);//false
    }

    /**
     * 测试自定义异常类型
     */
    @Test
    public void testCustomExceptionType() {
//        userEntity=null ;
        userEntity.setName(null);
        Null.ofCheck(userEntity)
                .of(UserEntity::getName)
                .doThrow(BizException.class);
    }

    @Test
    public void testCustomExceptionType1() {
//        userEntity=null ;
        userEntity.setName(null);
        Null.ofCheck(userEntity)
                .of(UserEntity::getName)
                .doThrow(BizException.class, "xxx缺少参数");
    }

    /**
     * 测试多个空值
     */
    @Test
    public void testMultipleNullMessages() {
        userEntity.setName(null);
        userEntity.setRoleData(null);

        Null.ofCheck(userEntity)
                .of(UserEntity::getId)
                .of(UserEntity::getName)
                .of(UserEntity::getAge)
                .of(UserEntity::getRoleData)
                .doThrow(RuntimeException.class);
    }

    /**
     * 测试 map 方法：进入内部对象继续判空
     */
    @Test
    public void testMapMethod() {
        boolean hasNull = Null.ofCheck(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getRoleName)
                .of(RoleEntity::getRoleDescription)
                .is();
        System.out.println(hasNull); // false（所有字段都不为空）
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
        System.out.println(hasNull); // true（roleName 为空）
    }

    /**
     * 测试 map 方法：内部对象本身为 null
     */
    @Test
    public void testMapMethodWithNullObject() {
        userEntity.setRoleData(null);
       Null.ofCheck(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getRoleName)
                .doThrow();
    }
    @Test
    public void testMapMethodWithNullObject1() {
        userEntity.setRoleData(null);
        Null.ofCheck(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getRoleName)
                .doThrow("参数错误");
    }

    /**
     * 测试 map 方法：抛出异常
     */
    @Test
    public void testMapMethodDoThrow() {
        userEntity.getRoleData().setRoleName(null);
        try {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .doThrow(BizException.class);
            fail("应该抛出异常");
        } catch (BizException e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }
    }

    /**
     * 测试 map 方法：带前缀消息的异常
     */
    @Test
    public void testMapMethodDoThrowWithPrefix() {
        userEntity.getRoleData().setRoleName(null);
        try {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .doThrow(BizException.class, "角色信息验证失败");
            fail("应该抛出异常");
        } catch (BizException e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }
    }

    /**
     * 测试 map 和 isNull 混合使用
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
        System.out.println(hasNull); // true（name 和 roleName 都为空）
    }

    /**
     * 测试多层 map 嵌套（如果有更深层的对象）
     */
    @Test
    public void testMultipleMapNested() {
        // 测试 map 后继续 map（虽然当前实体没有更深层，但测试链式调用）
        boolean hasNull = Null.ofCheck(userEntity)
                .map(UserEntity::getRoleData)
                .of(RoleEntity::getId)
                .of(RoleEntity::getRoleName)
                .is();
        System.out.println(hasNull); // false
    }

    /**
     * 测试 map 方法：多个内部字段为空
     */
    @Test
    public void testMapMethodMultipleNullFields() {

        userEntity.getRoleData().setRoleName(null);
        userEntity.getRoleData().setRoleDescription(null);
        try {
            Null.ofCheck(userEntity)
                    .map(UserEntity::getRoleData)
                    .of(RoleEntity::getRoleName)
                    .of(RoleEntity::getRoleDescription)
                    .doThrow(RuntimeException.class);
            fail("应该抛出异常");
        } catch (RuntimeException e) {
            System.out.println("捕获到异常: " + e.getMessage());
        }
    }
}

