package com.gitee.huanminabc.nullchain.core.ext;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.core.NullTools;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.tool.NullTool;

public interface NullToolsExt<T> extends NullTools<T>,  NullFinalityExt<T>{

    @Override
    default <U> NullChain<U> json(Class<U> uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.json(uClass);
    }

    @Override
    default <U> NullChain<U> json(U uClass) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.json(uClass);
    }
//
//    @Override
//    default NullChain<String> dateFormat(DateFormatEnum dateFormatEnum) {
//        NullChain<T> tNullChain = toNULL();
//        return tNullChain.dateFormat(dateFormatEnum);
//    }
//
//    @Override
//    default NullChain<T> dateOffset(DateOffsetEnum controlEnum, int num, TimeEnum timeEnum) {
//        NullChain<T> tNullChain = toNULL();
//        return tNullChain.dateOffset(controlEnum, num, timeEnum);
//    }
//
//    @Override
//    default NullChain<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum) {
//        NullChain<T> tNullChain = toNULL();
//        return tNullChain.dateOffset(controlEnum, timeEnum);
//    }
//
//    @Override
//    default NullChain<Integer> dateCompare(Object date) {
//        NullChain<T> tNullChain = toNULL();
//        return tNullChain.dateCompare(date);
//    }


    @Override
    default NullChain<T> copy() {
        return toNULL().copy();
    }


    @Override
    default NullChain<T> deepCopy() {
        return toNULL().deepCopy();
    }


    @Override
    default NullChain<String> json() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.json();
    }

    @Override
    default <U> NullChain<T> pick(NullFun<? super T, ? extends U>... mapper) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.pick(mapper);
    }

    @Override
    default <R> NullChain<R> tool(Class<? extends NullTool<T, R>> tool){
         NullChain<T> tNullChain = toNULL();
         return tNullChain.tool(tool);
   }

}
