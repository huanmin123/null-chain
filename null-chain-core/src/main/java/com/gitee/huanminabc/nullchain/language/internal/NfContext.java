package com.gitee.huanminabc.nullchain.language.internal;

import com.gitee.huanminabc.nullchain.base.NullChain;
import com.gitee.huanminabc.nullchain.vessel.NullSuperList;
import com.gitee.huanminabc.nullchain.vessel.NullSuperMap;
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

    public NfContext() {
        //创建默认的导入类型
        initDefaultImportType();
    }

    //key:是作用域的id, 当作用域结束时, 从map中移除
    private Map<String, NfContextScope> scopeMap = new HashMap<>();
    //类型和类的全路径映射关系
    private Map<String, String> importMap = new HashMap<>();
    private Map<String, String> taskMap = new HashMap<>();

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
        importMap.put("TreeMap", TreeMap.class.getName());
        importMap.put("LikedList", LinkedList.class.getName());
        importMap.put("ArrayList", ArrayList.class.getName());

        importMap.put("Map", HashMap.class.getName());
        importMap.put("Set", HashSet.class.getName());
        importMap.put("List", ArrayList.class.getName());

        importMap.put("CopyOnWriteArrayList", CopyOnWriteArrayList.class.getName());
        importMap.put("ConcurrentHashMap", ConcurrentHashMap.class.getName());
        importMap.put("NullSuperMap", NullSuperMap.class.getName());
        importMap.put("NullSuperList", NullSuperList.class.getName());

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

    }


    //清理上下文,减轻gc的自己释放的压力
    public void clear() {
        for (Map.Entry<String, NfContextScope> entry : scopeMap.entrySet()) {
            entry.getValue().clear();
        }
        scopeMap.clear();
        importMap.clear();
        importMap = null;
        scopeMap = null;
    }
}