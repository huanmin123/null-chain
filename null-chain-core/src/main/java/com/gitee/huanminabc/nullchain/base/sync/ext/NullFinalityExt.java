package com.gitee.huanminabc.nullchain.base.sync.ext;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.base.sync.NullFinality;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullChainCheckException;
import com.gitee.huanminabc.nullchain.common.NullCollect;
import com.gitee.huanminabc.nullchain.common.function.NullFun;
import com.gitee.huanminabc.nullchain.common.NullByteBuddy;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
@SuppressWarnings("unchecked")
public interface NullFinalityExt<T> extends NullFinality<T>, NullCheck {


    @Override
    default boolean is() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.is();
    }


    @Override
    default boolean isAny(NullFun<? super T, ?>... function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.isAny(function);
    }
    @Override
    default boolean isAll(NullFun<? super T, ?>... function){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.isAll(function);
    }


    @Override
    default boolean non() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.non();
    }


    @Override
    default boolean nonAll(NullFun<? super T, ?>... function) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.nonAll(function);
    }

    @JSONField(serialize = false)
    @JsonIgnore
    @Override
    default T getSafe() throws NullChainCheckException {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.getSafe();
    }

    @Override
    default T get() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.get();
    }

    @Override
    default <X extends Throwable> T get(Supplier<? extends X> exceptionSupplier) throws X {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.get(exceptionSupplier);
    }

    @Override
    default T get(String exceptionMessage, Object... args) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.get(exceptionMessage, args);
    }


    @Override
    default NullCollect collect() {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.collect();
    }

    @Override
    //安全的收集器,链接不能有空值,否则抛出异常,异常信息可以自定义
    default NullCollect collect(String exceptionMessage, Object... args) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.collect(exceptionMessage, args);
    }

    @Override
    default <X extends Throwable> NullCollect collect(Supplier<? extends X> exceptionSupplier) throws X {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.collect(exceptionSupplier);
    }


    @Override
    default <U extends T> boolean eq(U obj) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.eq(obj);
    }

    @Override
    default <U extends T> boolean eqAny(U... b){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.eqAny(b);
    }

    @Override
    default <U extends T> boolean notEq(U obj){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.notEq(obj);
    }

    @Override
    default <U extends T> boolean notEqAll(U... b){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.notEqAll(b);
    }

    @Override
    default boolean logic(Function<T, Boolean> obj){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.logic(obj);
    }

    @Override
    default <U extends T> boolean inAny(U... obj) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.inAny(obj);
    }



    @Override
    default <U extends T> boolean notIn(U... obj) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.notIn(obj);
    }

    @Override
    default <C extends Comparable<T>> boolean le(C obj){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.le(obj);
    }

    @Override
    default <C extends Comparable<T>> boolean lt(C obj){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.lt(obj);
    }

    @Override
    default <C extends Comparable<T>> boolean ge(C obj){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.ge(obj);
    }

    @Override
    default <C extends Comparable<T>> boolean gt(C obj){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.gt(obj);
    }


    @Override
    default void ifPresent(Consumer<? super T> action) {
        NullChain<T> tNullChain = toNULL();
        tNullChain.ifPresent(action);
    }


    @Override
    default  void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction){
        NullChain<T> tNullChain = toNULL();
        tNullChain.ifPresentOrElse(action, emptyAction);
    }
    @Override
    default T orElseNull(){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.orElseNull();
    }
    @Override
    default T orElse(T defaultValue) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.orElse(defaultValue);
    }

    @Override
    default T orElse(Supplier<T> defaultValue) {
        NullChain<T> tNullChain = toNULL();
        return tNullChain.orElse(defaultValue);
    }

    @Override
    default   int length(){
        NullChain<T> tNullChain = toNULL();
        return tNullChain.length();
    }

    /**
     * 判断是否为空,内部使用外部不要调用, 请使用{@link #is()}方法
     */
    @Deprecated
    default boolean isEmpty() {
        return NullByteBuddy.getEmptyMember(this);
    }




    default NullChain<T> toNULL() {
        StringBuilder linkLog = new StringBuilder();
        boolean empty = isEmpty();
        if (empty) {
            linkLog.append("Null?");
            return NullBuild.empty(linkLog, new NullCollect());
        }
        linkLog.append(" Null.");
        return NullBuild.noEmpty((T) this, linkLog, new NullCollect());
    }
}
