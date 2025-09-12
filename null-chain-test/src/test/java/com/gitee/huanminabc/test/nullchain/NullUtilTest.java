package com.gitee.huanminabc.test.nullchain;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import org.junit.jupiter.api.Test;

public class NullUtilTest {


    @Test
    public void isAny() {
        String a = "";
        String a1 = "12313";
        boolean any = Null.isAny(a, a1);
        System.out.println(any);//true
    }

    @Test
    public void orElse() {
        String a = "";
        String s = Null.orElse(a, "123");
        System.out.println(s);//123
    }

    @Test
    public void orThrow() {
        String a = "";
        String s = Null.orThrow(a, () -> new RuntimeException("空异常"));
        System.out.println(s);//java.lang.RuntimeException: 空异常
    }
   @Test
    public void eq() {
       NullChain<String> stringNullChain1 = Null.of("1123");
       NullChain<String> stringNullChain2 = Null.of("1123");
       System.out.println(Null.eq(stringNullChain1, stringNullChain2));
   }

}
