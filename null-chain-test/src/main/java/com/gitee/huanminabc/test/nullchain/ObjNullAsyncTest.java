package com.gitee.huanminabc.test.nullchain;


import com.gitee.huanminabc.common.multithreading.executor.SleepTools;
import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.common.test.CodeTimeUtil;
import com.gitee.huanminabc.common.exception.BizException;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * @author huanmin
 * @date 2024/2/2
 */
@Slf4j
public class ObjNullAsyncTest {
    UserEntity userEntity = new UserEntity();

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

    @Test
    public void test1() {
//        userEntity.setRoleData(null);
//        userEntity.getRoleData().setRoleName(null);
//        NullChainAsync<String> map = Null.of(userEntity).map(UserEntity::getRoleData).async().map(RoleEntity::getRoleName);
         Null.of(userEntity).map(UserEntity::getRoleData).async().map(null).then(()->{
            System.out.println("=====");
            throw  new RuntimeException("123");
        }).except(e->{
           log.error("",e);
         });
    }
    @Test
    public void test11() {
//        userEntity.setRoleData(null);
//        userEntity.getRoleData().setRoleName(null);
        NullChainAsync<String> map = Null.of(userEntity).map(UserEntity::getRoleData).async().map(RoleEntity::getRoleName);
        String s = map.get((BizException::new));
        System.out.println(s);
        System.out.println("====");
    }

    @Test
    public void test2()  {
        //不推荐这样一条龙进行异步转同步, 因为这样意义不大(和同步没啥区别都,都阻塞在这一行了, 异步链路的前一个没处理完毕后面的会等待前一个结果)
        Null.of(userEntity).map(UserEntity::getRoleData).async().map(RoleEntity::getRoleName).orElse("123");
        System.out.println("====");

        userEntity.getRoleData().setRoleName(null);
        //推荐这样的写法, 先异步各种处理,  让代码自己跑脱离主线程,  之后我们可以写自己的逻辑,    然后再同步获取结果
        NullChainAsync<String> map = Null.of(userEntity).async().map(UserEntity::getRoleData).map(RoleEntity::getRoleName);

        //执行其他逻辑代码
        //xxxxx

        //获取之前异步的结果
        String res = map.orElse("123");
        System.out.println(res);
    }

    @Test
    public void test3()  {
//        userEntity.setRoleData(null);
//        userEntity.getRoleData().setRoleName(null);
        Null.of(userEntity).map(UserEntity::getRoleData).async().map(RoleEntity::getRoleName).ifPresent((data) -> {
            System.out.println(data);
            SleepTools.second(2);
            throw new RuntimeException("123");
        });
        System.out.println("====");
        SleepTools.second(21);
    }

    @Test
    public void test4()  {
//        Null.of(userEntity).map(UserEntity::getRoleData).async().map(RoleEntity::getRoleName).then(()->{
//            int i = 1/0;
//        }).catchError((e)->{
//            log.error("",e);
//        });
//        userEntity.setRoleData(null);
//        userEntity.getRoleData().setRoleName(null);
        Null.of(userEntity).map(UserEntity::getRoleData).async().map(RoleEntity::getRoleName).then((data)->{
//            System.out.println(data);
            int i = 1/0;
        });
        System.out.println("====");
        SleepTools.second(21);
    }

    @Test
    public void test5()  {
        ThreadFactoryUtil.addExecutor("test1",10,100);
        Null.of(userEntity).map(UserEntity::getRoleData).async("test1").map(RoleEntity::getRoleName).ifPresent((data) -> {
            System.out.println(data);
            SleepTools.second(2);
        });
        SleepTools.second(21);
    }
    @Test
    public void collect() {
        NullChainAsync<RoleEntity> map = Null.of(userEntity).async().map(UserEntity::getRoleData);
        new Thread(()->{
            SleepTools.second(2);
            NullCollect mapCollect = map.collect();
            NullChain<RoleEntity> roleEntityNullChain = mapCollect.get(RoleEntity.class);
            roleEntityNullChain.ifPresent(System.out::println);
            NullChain<UserEntity> userEntityNullChain = mapCollect.get(UserEntity.class);
            userEntityNullChain.ifPresent(System.out::println);
        }).start();
        System.out.println("====");
        SleepTools.second(21);
    }

    @Test
    public void time() throws Exception {
        Null.of(userEntity);
        CodeTimeUtil.creator(() -> {
            for (int i = 0; i < 100000; i++) {
                try {
                    Null.of(userEntity).async().map(UserEntity::getRoleData).map(RoleEntity::getRoleDescription).getSafe();
                } catch (NullChainCheckException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}