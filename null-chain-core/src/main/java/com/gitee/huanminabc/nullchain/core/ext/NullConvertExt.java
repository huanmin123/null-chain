package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.jcommon.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullConvert;
import com.gitee.huanminabc.jcommon.enums.DateFormatEnum;

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
 * @version 1.1.1
 * @see NullConvert 类型转换接口
 * @see NullWorkFlowExt 工作流扩展接口
 * @since 1.0.0
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

    @Override
    default <U> NullChain<T> pick(NullFun<? super T, ? extends U>... mapper) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.pick(mapper);
    }

    @Override
    default NullChain<T> deepCopy() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.deepCopy();
    }

    @Override
    default NullChain<T> copy() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.copy();
    }

    @Override
    default NullChain<Long> dateBetween(Object date, TimeEnum timeEnum) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.dateBetween(date, timeEnum);
    }

    @Override
    default NullChain<Integer> dateCompare(Object date) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.dateCompare(date);
    }

    @Override
    default NullChain<T> dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum controlEnum, TimeEnum timeEnum) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.dateOffset(controlEnum, timeEnum);

    }

    @Override
    default NullChain<T> dateOffset(com.gitee.huanminabc.jcommon.enums.DateOffsetEnum controlEnum, int num, TimeEnum timeEnum) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.dateOffset(controlEnum, num, timeEnum);

    }


    @Override
    default NullChain<String> dateFormat(DateFormatEnum dateFormatEnum) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.dateFormat(dateFormatEnum);
    }

    @Override
    default <U> NullChain<U> fromJson(U uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.fromJson(uClass);
    }

    @Override
    default <U> NullChain<U> fromJson(Class<U> uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.fromJson(uClass);
    }

    @Override
    default NullChain<String> json() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.json();
    }


}
