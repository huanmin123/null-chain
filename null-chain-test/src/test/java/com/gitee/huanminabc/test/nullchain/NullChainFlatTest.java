package com.gitee.huanminabc.test.nullchain;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class NullChainFlatTest {

    @Test
    public void testFlatChainUnwrap() {
        NullChain<String> base = Null.of(10)
                .flatChain(v -> Null.of("v=" + v));
        Assertions.assertEquals("v=10", base.get());
    }

    @Test
    public void testFlatOptionalShortCircuit() {
        // 返回空 Optional 时应短路为 empty
        NullChain<Object> empty = Null.of(1).flatOptional(v -> Optional.empty());
        Assertions.assertTrue(empty.is());

        // 返回非空 Optional 时应正确取值
        NullChain<Integer> got = Null.of(1).flatOptional(v -> Optional.of(2));
        Assertions.assertEquals(2, got.get());
    }
}


