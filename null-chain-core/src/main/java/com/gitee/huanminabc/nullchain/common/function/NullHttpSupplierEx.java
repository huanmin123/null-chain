package com.gitee.huanminabc.nullchain.common.function;

import java.io.IOException;

/**
 * Null HTTP供应商异常接口 - 提供HTTP操作的供应商功能
 * 
 * <p>该接口定义了HTTP操作的供应商功能，支持可能抛出IOException的操作。
 * 用于在Null链中处理HTTP相关的供应商操作。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>HTTP供应商：提供HTTP操作的供应商功能</li>
 *   <li>异常处理：支持IOException异常处理</li>
 *   <li>类型安全：通过泛型保证类型安全</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>HTTP请求的供应商操作</li>
 *   <li>网络IO相关的供应商操作</li>
 *   <li>可能抛出IOException的供应商操作</li>
 * </ul>
 * 
 * @param <T> 供应商返回值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 */
@FunctionalInterface
public interface NullHttpSupplierEx<T> {
    /**
     * 获取值
     * 
     * @return 供应商提供的值
     * @throws IOException IO异常
     */
    T get() throws IOException;
}
