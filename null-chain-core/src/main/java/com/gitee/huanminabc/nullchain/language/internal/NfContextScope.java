package com.gitee.huanminabc.nullchain.language.internal;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 上下文作用域:
 * 1.用于存储在作用域中产生的变量等信息
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@Data
public class NfContextScope {
    private boolean isBreak = false;
    private boolean isContinue = false;
    private boolean isBreakAll = false;
    private NfContextScopeType type;
    //父作用域id
    private String parentScopeId;
    private String scopeId;//作用域id
    //key:变量名, value:变量信息
    private Map<String, NfVariableInfo> value = new HashMap<>();

    public NfContextScope(String scopeId, String parentScopeId, NfContextScopeType type) {
        this.scopeId = scopeId;
        this.parentScopeId = parentScopeId;
        this.type = type;
    }

    //清除作用域中全部变量
    public void clear(){
        value.clear();
        value = null;
    }
    //添加或者更新一个变量
    public void addVariable(NfVariableInfo nfVariableInfo){
        value.put(nfVariableInfo.getName(), nfVariableInfo);
    }
    //获取一个变量
    public NfVariableInfo getVariable(String name){
        return value.get(name);
    }


    //将作用域转化为  Map<String, Object>
    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        for (Map.Entry<String, NfVariableInfo> entry : value.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getValue());
        }
        return map;
    }
}
