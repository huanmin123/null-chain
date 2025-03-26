package com.gitee.huanminabc.test.nullchain;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.tool.base64.Base64StrEncodeTool;
import com.gitee.huanminabc.nullchain.tool.file.StrToWriteFileTool;
import com.gitee.huanminabc.nullchain.tool.object.DeserializeTool;
import com.gitee.huanminabc.nullchain.tool.object.SerializeTool;
import com.gitee.huanminabc.nullchain.tool.other.NumberToCnTool;
import com.gitee.huanminabc.nullchain.tool.base64.Base64StrDecodeTool;
import com.gitee.huanminabc.nullchain.tool.hash.MD5Tool;
import com.gitee.huanminabc.test.nullchain.entity.RoleEntity;
import com.gitee.huanminabc.test.nullchain.entity.UserEntity;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class NullTooltTest {
    UserEntity userEntity = new UserEntity();
    List<UserEntity> userEntityList = Arrays.asList(userEntity);

    @Before
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
    public void NumberToCnToolTest(){
        Integer  a=123;
        Null.of(a).tool( NumberToCnTool.class).ifPresent(System.out::println);

    }


    @Test
    public  void hashTest() throws NullChainCheckException {
        String name="12313";
        Null.of(name).tool(MD5Tool.class).ifPresent(System.out::println);


        String encode = Null.of(name).tool(Base64StrEncodeTool.class).getSafe();
        System.out.println(encode);

        String decode = Null.of(encode).tool(Base64StrDecodeTool.class).getSafe();
        System.out.println(decode);



        Integer num=123;
        //NumberToCnTool.class
        Null.of(num).tool(NumberToCnTool.class).ifPresent(System.out::println);

        //如果是未知的类型,无法在NULLConvert类中写死类型,可以通过task的方式转换

    }


    @Test
    public  void StrToWriteFileToolTest()  {

        //入参和出参都一致
        UserEntity userEntity = new UserEntity();
//        Null.of(userEntity).task(ObjFillTask.class).ifPresent(System.out::println);

        //入参和出参都不一致 出参需要指定类型
        String a="123131";
        Null.of(a).tool(StrToWriteFileTool.class,"test.txt",true).type(Boolean.class).ifPresent(System.out::println);

    }


    @Test
    public void serializeTest() throws NullChainCheckException {
        byte[] bytes1 = Null.of(userEntity).type(Serializable.class).tool(SerializeTool.class).getSafe();
//        System.out.println(Arrays.toString(bytes1));
        Null.of(bytes1).tool(DeserializeTool.class).type(UserEntity.class).ifPresent(System.out::println);
    }
}
