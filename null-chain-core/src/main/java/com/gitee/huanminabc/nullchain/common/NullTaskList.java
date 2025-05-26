package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.base.sync.NullChainBase;
import com.gitee.huanminabc.nullchain.common.function.NullTaskFun;
import com.gitee.huanminabc.nullchain.task.NullTask;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @description:
 * @author: huanmin
 * @create: 2025-05-26 14:22
 **/
@Slf4j
public class NullTaskList {
    private Queue<NullTaskFun> tasks;

    public NullTaskList() {
        tasks = new LinkedList<>();
    }

    public void add(NullTaskFun task) {
        tasks.add(task);
    }


    //运行任务返回结果
    //async 如果调用方支持异步那么开启异步之后的节点将脱离主线程
    public <T> NullChainBase<T> runTaskAll() {
        NullChainBase<T> chain = null;
        while (!tasks.isEmpty()) {
            NullTaskFun task = tasks.poll();
            NullChainBase task1 = (NullChainBase) task.task(chain == null ? null : chain.value);
            if (task1.isNull) {
                return task1;
            }
            chain = task1;
        }
        tasks=null;//避免被重复调用
        return chain;
    }

    public <T> void runTaskAll(Consumer<NullChainBase<T>> supplier) {
        NullChainBase chain = null;
        CompletableFuture<NullChainBase> completableFuture = null;
        while (!tasks.isEmpty()) {
            NullTaskFun poll = tasks.poll();
            if (completableFuture==null) {
                NullChainBase task = (NullChainBase) poll.task(chain == null ? null : chain.value);
                if (task.isNull) {
                    supplier.accept(task);
                    return ;
                }
                if (task.async){
                    completableFuture = new CompletableFuture();
                    completableFuture.complete(task);
                }
                chain = task;
            }else{
                completableFuture=completableFuture.thenComposeAsync ((taskFut)-> {
                    NullChainBase task1 = (NullChainBase)poll.task(taskFut.value);
                    if (task1.isNull) {
                        supplier.accept(task1);
                        return CompletableFuture.completedFuture(null);
                    }
                    //继续执行
                    return CompletableFuture.completedFuture(task1);

                });
            }
        }
        tasks=null;
        if (completableFuture==null) {
            supplier.accept(chain);
        }else{
            completableFuture.thenComposeAsync((nullChainBase)->{
                supplier.accept(nullChainBase);
                return CompletableFuture.completedFuture(null);
            });
        }

    }

}
