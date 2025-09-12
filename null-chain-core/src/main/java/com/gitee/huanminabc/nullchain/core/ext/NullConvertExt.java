package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.nullchain.core.NullConvert;
import com.gitee.huanminabc.nullchain.common.NullChainException;

/**
 * Null类型转换扩展接口 - 提供类型转换操作的扩展功能
 * 
 * <p>该接口扩展了NullConvert接口，提供了额外的类型转换操作功能。
 * 通过默认方法实现，为类型转换操作提供更丰富的功能支持。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>类型转换扩展：提供额外的类型转换操作方法</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 *   <li>空值处理：处理转换过程中的空值情况</li>
 *   <li>工作流集成：与工作流操作无缝集成</li>
 * </ul>
 * 
 * @param <T> 转换前的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullConvert 类型转换接口
 * @see NullWorkFlowExt 工作流扩展接口
 */
public interface NullConvertExt<T> extends NullConvert<T>, NullWorkFlowExt<T> {




    @Override
    default <U> NullChain<U> type(Class<U> uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.type(uClass);
    }

    @Override
    default <U> NullChain<U> type(U uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.type(uClass);
    }
}
