package com.gitee.huanminabc.nullchain.language.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 静态区域: 用于存储全局信息多任务共享数据,（待定）
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfStaticRegion {
  public  static   Map<String,Object> threadMap = new ConcurrentHashMap<>();
}
