package com.gitee.huanminabc.test.nullchain;


import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

/**
 * @Author huanmin
 * @Date 2024/1/11
 */
public class ObjNullErrorTest {
    UserEntity userEntity = new UserEntity();
    UserEntity[] userEntitys= new UserEntity[10];
    @Before
    public void before() {
        userEntity.setId(1);
        userEntity.setName("huanmin");
        userEntity.setAge(33);
        userEntity.setDate(new Date());

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("admin");
        roleEntity.setRoleDescription("xxxx");
        userEntity.setRoleData(roleEntity);
        userEntitys[0] = userEntity;

    }
    @Test
    public void of_error1()  {
       Null.of(userEntity).map(UserEntity::getRoleData).then((data) -> {
            data.setRoleName("default");
            throw new RuntimeException("测试主动异常");
//            return data;
        }).ifPresent(System.out::println);

//        userEntity=null;
//        String roleName = Null.of(userEntity).map(UserEntity::getRoleData).map(RoleEntity::getRoleName).orElse("default");
    }
    @Test
    public void of_error2()  {
        userEntity.setRoleData(null);
        String s = Null.of(userEntity).map(UserEntity::getRoleData).map(RoleEntity::getRoleName).get();
    }

    @Test
    public void of_error4() {
        userEntity.getRoleData().setRoleName(null);
        String s2 = Null.of(userEntity).map(UserEntity::getRoleData).map(RoleEntity::getRoleName).get(RuntimeException::new);
        System.out.println(s2);
    }

    @Test
    public void of_error5() throws NullChainCheckException {

        UserEntity user = Null.of(userEntity).map((data) -> {
            data.getRoleData().setRoleName(null);
            return data;
        }).of(UserEntity::getRoleData).orElse(new UserEntity());
        System.out.println(user);


        String roleName = Null.of(userEntity).map((data) -> {
            data.getRoleData().setRoleName(null);
            return data;
        }).of(UserEntity::getRoleData).map(UserEntity::getRoleData).of(RoleEntity::getRoleName).map(RoleEntity::getRoleName).getSafe();
        System.out.println(roleName);//error
    }

}
