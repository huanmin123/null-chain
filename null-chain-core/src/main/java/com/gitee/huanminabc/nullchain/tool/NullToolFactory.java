package com.gitee.huanminabc.nullchain.tool;

import com.gitee.huanminabc.nullchain.common.NullChainException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
/**
 * @author huanmin
 * @date 2024/11/22
 */
@Slf4j
public class NullToolFactory {

   private static Map<String, NullTool<?, ?>> toolMap =new HashMap<>();

   //注册转换器
    public static   void registerTool(Class<? extends NullTool>  tool){
        try {
            String name = tool.getName();
            //判断是否存在如果存在就异常
            if(toolMap.containsKey(name)){
                throw new NullChainException("已经存在工具:"+name);
            }
            //获取转换器的类型
            NullTool nullTool = tool.newInstance();
            toolMap.put(name, nullTool);
        } catch (Exception e) {
            log.error("注册工具失败: ",e);
        }
    }

    //获取转换器
    public static  <T,R> NullTool<T,R> getTool(Class<? extends NullTool<T,R>> clazz){
        //获取转换器的类型
        String name = clazz.getName();
        //获取转换器
        return (NullTool<T,R>) toolMap.get(name);
    }

    //内部自己使用的注册任务
    public static  void __registerTool__(NullTool tool){
        String name = tool.getClass().getName();
        //判断是否存在如果存在就异常
        if(toolMap.containsKey(name)){
            throw new NullChainException("已经存在工具:"+name);
        }
        toolMap.put(name,tool);
    }

}
