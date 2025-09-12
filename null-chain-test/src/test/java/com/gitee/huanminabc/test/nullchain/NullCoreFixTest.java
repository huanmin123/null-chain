package com.gitee.huanminabc.test.nullchain;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NullCoreFixTest {

    @Test
    public void testMap2GenericAndBehavior() {
        // map2 返回应为新的泛型 NullChain<U>，而不是 this 强转
        NullChain<Integer> base = Null.of(10);
        NullChain<String> mapped = base.map2((chain, v) -> "val=" + v);
        Assertions.assertEquals("val=10", mapped.get());
    }

    @Test
    public void testOfStreamFromCollectionAndArray() {
        List<Integer> data = Arrays.asList(1, null, 2, 3, null, 4);

        List<Integer> viaCollection = Null.ofStream(data)
                .map(x -> x) // 保持原值
                .collect(Collectors.toList());
        // 应自动过滤 null（由内部 filter(Null::non) 保障）
        Assertions.assertEquals(Arrays.asList(1, 2, 3, 4), viaCollection);

        Integer[] arr = new Integer[]{1, null, 2, 3};
        List<Integer> viaArray = Null.ofStreamArray(Null.of(arr))
                .map(x -> x)
                .collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(1, 2, 3), viaArray);
    }
}


