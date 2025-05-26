package com.gitee.huanminabc.nullchain.base.sync.ext;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.common.function.NullConsumer2;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.function.NullFun2;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.tool.NullTool;
import com.gitee.huanminabc.nullchain.vessel.NullMap;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
@SuppressWarnings("unchecked")
public interface NullChainExt<T> extends NullChain<T>, NullConvertExt<T> {
    @Override
    default <U> NullChain<T> of(NullFun<? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.of(function);
    }


    @Override
    default <U> NullChain<T> ofAny(NullFun<? super T, ? extends U>... function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.ofAny(function);
    }

    @Override
    default NullChain<T> ifGo(NullFun<? super T, Boolean> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.ifGo(function);
    }
    @Override
    default <X extends RuntimeException> NullChain<T> check(Supplier<? extends X> exceptionSupplier) throws X{
         NullChain<T> tNullChain = toNULL();
         return tNullChain.check(exceptionSupplier);
    }

    @Override
    default <U> NullChain<T> isNull(NullFun<? super T, ? extends U> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.isNull(function);
    }

    @Override
    default NullChain<T> then(Runnable function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.then(function);
    }
    @Override
    default NullChain<T> then(Consumer<? super T> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.then(function);
    }

    @Override
    default   NullChain<T> then2(NullConsumer2<NullChain<T>, ? super T> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.then2(function);
    }



    @Override
    default <U> NullChain<U> map(NullFun<? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.map(function);
    }

    @Override
    default <U> NullChain<U> map2(NullFun2<NullChain<T>, ? super T, ? extends U> function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.map2(function);
    }


    @Override
    default <U> NullChain<U> flatChain(NullFun<? super T, ? extends NullChain<U>> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.flatChain(function);
    }

    @Override
    default <U> NullChain<U> flatOptional(NullFun<? super T, ? extends Optional<U>> function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.flatOptional(function);
    }

    @Override
    default NullChain<T> or(Supplier<? extends T> supplier) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.or(supplier);
    }

    @Override
    default NullChain<T> or(T defaultValue){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.or(defaultValue);
    }


    @Override
    default <R> NullChain<R> tool(Class<? extends NullTool<T, R>> convert) throws NullChainException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.tool(convert);
    }

    @Override
    default <R> NullChain<R> tool(Class<? extends NullTool<T, R>> convert, Object... params) throws NullChainException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.tool(convert, params);
    }


    @Override
    default <R> NullChain<R> task(Class<? extends NullTask<T, R>> task, Object... objects) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(task, objects);
    }


    @Override
    default NullChain<?> task(String classPath, Object... objects) throws NullChainException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(classPath, objects);
    }

    @Override
    default NullChain<NullMap<String, Object>> task(NullGroupTask nullGroupTask){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(nullGroupTask);
    }

    @Override
    default NullChain<NullMap<String, Object>> task( NullGroupTask nullGroupTask,String threadFactoryName){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.task(nullGroupTask,threadFactoryName );
    }

    @Override
    default  NullChain<?> nfTask(String filePath, Object... params){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTask(filePath, params);
    }

    @Override
    default NullChain<?> nfTask(NullGroupNfTask.NullTaskInfo nullTaskInfo){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTask(nullTaskInfo);
    }

    @Override
    default NullChain<?> nfTask( NullGroupNfTask.NullTaskInfo nullTaskInfo,String threadFactoryName){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTask(nullTaskInfo,threadFactoryName);
    }

    @Override
    default NullChain<NullMap<String, Object>> nfTasks( NullGroupNfTask nullGroupNfTask,String threadFactoryName){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nfTasks( nullGroupNfTask,threadFactoryName);
    }
}
