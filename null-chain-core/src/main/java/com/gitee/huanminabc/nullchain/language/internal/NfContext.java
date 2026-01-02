package com.gitee.huanminabc.nullchain.language.internal;

import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.language.NfMain;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 上下文:
 * 1.用于存储变量相关信息和所在的作用域
 * 2. 一个文件一个上下文
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@Data
public class NfContext {
    //全局作用域
    private String mainScopeId;
    //当前作用域的id
    private String currentScopeId;
    //全局breakAll标志（用于跳出所有FOR循环）
    private boolean globalBreakAll = false;

    public NfContext() {
        //创建默认的导入类型
        initDefaultImportType();
        //初始化接口到默认实现类的映射
        initInterfaceDefaultImplMap();
    }

    //key:是作用域的id, 当作用域结束时, 从map中移除
    private Map<String, NfContextScope> scopeMap = new HashMap<>();
    //类型和类的全路径映射关系
    private Map<String, String> importMap = new HashMap<>();
    private Map<String, String> taskMap = new HashMap<>();
    //接口类型到默认实现类的映射关系
    private Map<Class<?>, Class<?>> interfaceDefaultImplMap = new HashMap<>();

    //获取类型的全路径
    public String getImportType(String type) {
        return importMap.get(type);
    }

    //获取task的全路径
    public String getTask(String taskName) {
        return taskMap.get(taskName);
    }

    //添加导入
    public void addImport(String type, String classPath) {
        importMap.put(type, classPath);
    }

    //添加task
    public void addTask(String taskName, String classPath) {
        taskMap.put(taskName, classPath);
    }


    //创建一个作用域
    public NfContextScope createScope(String id, String parentScopeId, NfContextScopeType type) {
        NfContextScope nfContextScope = new NfContextScope(id, parentScopeId, type);
        scopeMap.put(id, nfContextScope);
        return nfContextScope;
    }

    //获取当前作用域
    public NfContextScope getCurrentScope() {
        return scopeMap.get(currentScopeId);
    }

    //获取全局作用域
    public NfContextScope getMainScope() {
        return scopeMap.get(mainScopeId);
    }


    //获取一个作用域
    public NfContextScope getScope(String id) {
        return scopeMap.get(id);
    }

    //切换作用域
    public void switchScope(String id) {
        currentScopeId = id;
    }

    //创建子作用域
    //原理就是将主作用域和父作用域合并到新的作用域
    public NfContextScope createChildScope(String parentScopeId, NfContextScopeType type) {
        //创建一个作用域id
        String scopeId = NfContext.generateScopeId();
        //新的作用域
        NfContextScope scope = createScope(scopeId, parentScopeId, type);
        //切换当前作用域为新的作用域
        switchScope(scopeId);
        return scope;
    }

    //向上查找指定类型的作用域
    public NfContextScope findByTypeScope(NfContextScopeType type) {
        return findByTypeScope(currentScopeId, type);
    }

    //递归查找指定类型的作用域
    private NfContextScope findByTypeScope(String scopeId, NfContextScopeType type) {
        NfContextScope nfContextScope = scopeMap.get(scopeId);
        if (nfContextScope != null) {
            if (nfContextScope.getType() == type) {
                return nfContextScope;
            }
            return findByTypeScope(nfContextScope.getParentScopeId(), type);
        }
        return null;
    }

    //向上查找指定类型的全部作用域返回List
    public List<NfContextScope> findByTypeScopeList(NfContextScopeType type) {
        List<NfContextScope> list = new ArrayList<>();
        findByTypeScopeList(currentScopeId, type, list);
        return list;
    }

    //获取所有指定类型的活动作用域（遍历整个scopeMap）
    //用于breakall等需要影响所有FOR循环的场景，而不仅仅是当前作用域的祖先
    public List<NfContextScope> findAllActiveScopesByType(NfContextScopeType type) {
        List<NfContextScope> list = new ArrayList<>();
        for (NfContextScope scope : scopeMap.values()) {
            if (scope.getType() == type) {
                list.add(scope);
            }
        }
        return list;
    }

    //递归查找指定类型的全部作用域返回List
    private void findByTypeScopeList(String scopeId, NfContextScopeType type, List<NfContextScope> list) {
        NfContextScope nfContextScope = scopeMap.get(scopeId);
        if (nfContextScope != null) {
            if (nfContextScope.getType() == type) {
                list.add(nfContextScope);
            }
            findByTypeScopeList(nfContextScope.getParentScopeId(), type, list);
        }
    }
    //递归查找从当前作用域到指定类型的全部作用域返回List
    public List<NfContextScope> findByTypeScopeListRange(NfContextScopeType type) {
        List<NfContextScope> list = new ArrayList<>();
        findByTypeScopeListRange(currentScopeId, type, list);
        return list;
    }

    private void findByTypeScopeListRange(String scopeId, NfContextScopeType type, List<NfContextScope> list) {
        NfContextScope nfContextScope = scopeMap.get(scopeId);
        if (nfContextScope != null) {
            if (nfContextScope.getType() == type) {
                list.add(nfContextScope);
                return;
            }
            list.add(nfContextScope);
            if (nfContextScope.getParentScopeId() != null) {
                findByTypeScopeListRange(nfContextScope.getParentScopeId(), type, list);
            }
        }
    }


    //获取变量,优先从当前作用域获取,如果没有,那么就从父作用域获取直到全局作用域
    public NfVariableInfo getVariable(String name) {
        return getVariable(name, currentScopeId);
    }

    //递归获取变量, 获取父作用域的变量
    private NfVariableInfo getVariable(String name, String scopeId) {
        NfContextScope nfContextScope = scopeMap.get(scopeId);
        if (nfContextScope != null) {
            NfVariableInfo nfVariableInfo = nfContextScope.getVariable(name);
            if (nfVariableInfo != null) {
                return nfVariableInfo;
            }
            //如果当前没有找到,那么就从父作用域获取
            return getVariable(name, nfContextScope.getParentScopeId());
        }
        return null;
    }

    /**
     * 查找变量所在的作用域
     * 从当前作用域开始向上查找，直到找到包含该变量的作用域
     * 
     * @param name 变量名
     * @return 包含该变量的作用域，如果不存在则返回null
     */
    public NfContextScope findVariableScope(String name) {
        return findVariableScope(name, currentScopeId);
    }

    /**
     * 递归查找变量所在的作用域
     * 
     * @param name 变量名
     * @param scopeId 当前作用域ID
     * @return 包含该变量的作用域，如果不存在则返回null
     */
    private NfContextScope findVariableScope(String name, String scopeId) {
        NfContextScope nfContextScope = scopeMap.get(scopeId);
        if (nfContextScope != null) {
            NfVariableInfo nfVariableInfo = nfContextScope.getVariable(name);
            if (nfVariableInfo != null) {
                return nfContextScope;
            }
            //如果当前作用域没有找到,那么就从父作用域查找
            return findVariableScope(name, nfContextScope.getParentScopeId());
        }
        return null;
    }

    //移除一个作用域
    public void removeScope(String id) {
        //需要先将作用域中的变量移除,减少gc的压力
        NfContextScope nfContextScope = scopeMap.get(id);
        if (nfContextScope != null) {
            nfContextScope.clear();
        }
        scopeMap.remove(id);
    }

    //生成一个作用域id
    public static String generateScopeId() {
        return "scope_" + UUID.randomUUID();
    }

    //java中的常用类型默认导入
    public void initDefaultImportType() {
        importMap.put("NullChain", NullChain.class.getName());

        importMap.put("String", String.class.getName());
        importMap.put("Integer", Integer.class.getName());
        importMap.put("Float", Float.class.getName());
        importMap.put("Double", Double.class.getName());
        importMap.put("Boolean", Boolean.class.getName());

        importMap.put("HashMap", HashMap.class.getName());
        importMap.put("HashSet", HashSet.class.getName());
        importMap.put("TreeMap", TreeMap.class.getName());
        importMap.put("LinkedList", LinkedList.class.getName());
        importMap.put("ArrayList", ArrayList.class.getName());

        importMap.put("Map", Map.class.getName());
        importMap.put("Set", Set.class.getName());
        importMap.put("List", List.class.getName());

        importMap.put("CopyOnWriteArrayList", CopyOnWriteArrayList.class.getName());
        importMap.put("ConcurrentHashMap", ConcurrentHashMap.class.getName());

        //Date
        importMap.put("Date", Date.class.getName());
        //BigDecimal
        importMap.put("BigDecimal", BigDecimal.class.getName());
        //LocalDate
        importMap.put("LocalDate", LocalDate.class.getName());
        //LocalDateTime
        importMap.put("LocalDateTime", LocalDateTime.class.getName());
        //LocalTime
        importMap.put("LocalTime", LocalTime.class.getName());
        //DateTimeFormatter
        importMap.put("DateTimeFormatter", DateTimeFormatter.class.getName());

        //工具包
        importMap.put("Objects", Objects.class.getName());
        importMap.put("Arrays", Arrays.class.getName());
        importMap.put("Collections", Collections.class.getName());
        importMap.put("UUID", UUID.class.getName());

        //8大基本类型, 转化为包装类型
        importMap.put("int", Integer.class.getName());
        importMap.put("long", Long.class.getName());
        importMap.put("short", Short.class.getName());
        importMap.put("byte", Byte.class.getName());
        importMap.put("char", Character.class.getName());
        importMap.put("float", Float.class.getName());
        importMap.put("double", Double.class.getName());
        importMap.put("boolean", Boolean.class.getName());
        importMap.put("bool", Boolean.class.getName());

    }

    //初始化接口到默认实现类的映射
    private void initInterfaceDefaultImplMap() {
        interfaceDefaultImplMap.put(Map.class, HashMap.class);
        interfaceDefaultImplMap.put(List.class, ArrayList.class);
        interfaceDefaultImplMap.put(Set.class, HashSet.class);
    }

    //获取接口的默认实现类
    public Class<?> getInterfaceDefaultImpl(Class<?> interfaceType) {
        return interfaceDefaultImplMap.get(interfaceType);
    }

    //全局breakAll标志的getter和setter
    public boolean isGlobalBreakAll() {
        return globalBreakAll;
    }

    public void setGlobalBreakAll(boolean globalBreakAll) {
        this.globalBreakAll = globalBreakAll;
    }

    //添加接口到默认实现类的映射（允许扩展）
    public void addInterfaceDefaultImpl(Class<?> interfaceType, Class<?> implType) {
        if (!interfaceType.isInterface()) {
            throw new IllegalArgumentException("第一个参数必须是接口类型: " + interfaceType.getName());
        }
        if (!interfaceType.isAssignableFrom(implType)) {
            throw new IllegalArgumentException("实现类 " + implType.getName() + " 必须实现接口 " + interfaceType.getName());
        }
        interfaceDefaultImplMap.put(interfaceType, implType);
    }

    /**
     * 销毁上下文并释放资源
     * 
     * <p><b>重要：</b>此方法会将内部字段置为 null，调用后对象不可再使用。
     * 此方法仅在脚本执行完成后调用，用于释放资源和减轻 GC 压力。</p>
     * 
     * <p><b>注意：</b>此方法与普通的 clear() 方法不同，它会彻底销毁对象状态。
     * 如果需要重用上下文，请创建新的 NfContext 实例。</p>
     * 
     * @see NfMain#run
     */
    public void clear() {
        // 销毁所有作用域
        for (Map.Entry<String, NfContextScope> entry : scopeMap.entrySet()) {
            entry.getValue().clear();
        }
        // 清空并释放所有内部 Map
        scopeMap.clear();
        importMap.clear();
        importMap = null;
        scopeMap = null;
        // 释放接口映射
        if (interfaceDefaultImplMap != null) {
            interfaceDefaultImplMap.clear();
            interfaceDefaultImplMap = null;
        }
    }
}