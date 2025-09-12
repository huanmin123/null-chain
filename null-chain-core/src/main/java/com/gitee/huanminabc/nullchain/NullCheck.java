package com.gitee.huanminabc.nullchain;

/**
 * Null检查接口 - 提供空值检查功能
 * 
 * <p>该接口为自定义的类和容器等提供可接入空链的校验功能。
 * 通过实现此接口，可以自定义空值检查逻辑，与Null链框架无缝集成。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>空值检查：提供自定义的空值检查逻辑</li>
 *   <li>框架集成：与Null链框架无缝集成</li>
 *   <li>灵活实现：支持各种自定义的空值判断规则</li>
 * </ul>
 * 
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>自定义类的空值检查</li>
 *   <li>容器的空值检查</li>
 *   <li>特殊业务逻辑的空值判断</li>
 * </ul>
 * 
 * <h3>实现要求：</h3>
 * <ul>
 *   <li>必须实现isEmpty()方法</li>
 *   <li>返回true表示对象为空</li>
 *   <li>返回false表示对象不为空</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 */
public interface NullCheck {

    /**
     * 判断对象是否为空
     * 
     * <p>该方法由子类实现，用于定义具体的空值检查逻辑。
     * 不同的实现类可以根据自己的业务需求定义空值的判断规则。</p>
     * 
     * @return 如果对象为空则返回true，否则返回false
     */
    boolean isEmpty();
}
