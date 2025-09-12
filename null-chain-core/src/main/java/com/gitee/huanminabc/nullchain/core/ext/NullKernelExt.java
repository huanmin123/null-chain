package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.common.*;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullChainBase;

/**
 * 简要描述
 *
 * @Author: mac
 * @Date: 2025/5/29 19:38
 * @Version: 1.0
 * @Description: 文件作用详细描述....
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
