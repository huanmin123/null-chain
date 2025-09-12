package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.common.*;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;

/**
 * Null内核扩展接口 - 提供内核操作的扩展功能
 * 
 * <p>该接口扩展了NullKernel接口，提供了额外的内核操作功能。
 * 通过默认方法实现，为内核操作提供更丰富的功能支持。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>内核操作扩展：提供额外的内核操作方法</li>
 *   <li>异步执行：支持异步执行功能</li>
 *   <li>空值安全：所有操作都处理null值情况</li>
 * </ul>
 * 
 * @param <T> 内核处理的值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullKernel 内核接口
 */
public interface NullKernelExt<T>  extends NullKernel<T>, NullCheck {

    @Override
    default NullChain<T> async() {
        NullChainBase<T> tNullChain = (NullChainBase)toNULL();
        return tNullChain.async();
    }
    @Override
    default NullChain<T> async(String threadFactoryName) throws NullChainException {
        NullChainBase<T> tNullChain = (NullChainBase)toNULL();
        return tNullChain.async(threadFactoryName);
    }

    /**
     * 尽量使用is()方法,此方法用于内部判断是否为空,如果外部调用了也没关系
     */
    default boolean isEmpty() {
        return NullByteBuddy.getEmptyMember(this);
    }

    default NullChain<T> toNULL() {
        NullTaskList nullTaskList = new NullTaskList();
        StringBuilder linkLog = new StringBuilder();
        nullTaskList.add((__) -> {
            boolean empty = isEmpty();
            if (empty) {
                linkLog.append(NULL_EXT_Q);
                return NullBuild.empty();
            }
            linkLog.append(NULL_EXT_DOT);
            return NullBuild.noEmpty((T) this);
        });
        return NullBuild.busy(linkLog, nullTaskList);
    }
}
