package com.gitee.huanminabc.nullchain.language.internal;

import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.NullCheck;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.language.NfMain;
import com.gitee.huanminabc.nullchain.leaf.calculate.NullCalculate;
import com.gitee.huanminabc.nullchain.leaf.http.OkHttp;
import com.gitee.huanminabc.nullchain.leaf.stream.NullStream;
import com.gitee.huanminabc.nullchain.language.NfTimeoutException;
import lombok.Data;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    /**
     * 默认导入类型映射（静态常量，类加载时初始化一次）
     * 使用不可变Map包装，确保线程安全
     */
    private static final Map<String, String> DEFAULT_IMPORT_MAP = Collections.unmodifiableMap(initDefaultImportType());

    /**
     * 接口到默认实现类的映射（静态常量，类加载时初始化一次）
     * 使用不可变Map包装，确保线程安全
     */
    private static final Map<Class<?>, Class<?>> DEFAULT_INTERFACE_IMPL_MAP = Collections.unmodifiableMap(initInterfaceDefaultImplMap());

    /**
     * 全局脚本执行超时时间（毫秒）
     * 默认 5 分钟，可以通过 setGlobalTimeout() 方法修改
     */
    private static long globalTimeoutMillis = 5 * 60 * 1000;

    /**
     * 设置全局脚本执行超时时间
     * 
     * @param timeoutMillis 超时时间（毫秒），必须大于 0
     * @throws IllegalArgumentException 如果 timeoutMillis <= 0
     */
    public static void setGlobalTimeout(long timeoutMillis) {
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("超时时间必须大于 0，当前值: " + timeoutMillis);
        }
        globalTimeoutMillis = timeoutMillis;
    }

    /**
     * 获取全局脚本执行超时时间
     * 
     * @return 超时时间（毫秒）
     */
    public static long getGlobalTimeout() {
        return globalTimeoutMillis;
    }

    //全局作用域
    private String mainScopeId;
    //当前作用域的id
    private String currentScopeId;
    //全局breakAll标志的getter和setter
    //全局breakAll标志（用于跳出所有FOR循环）
    private boolean globalBreakAll = false;

    /**
     * 构造函数：初始化上下文
     * 从静态常量复制默认导入和接口映射到实例Map，保证每个实例有独立的Map
     */
    public NfContext() {
        //从静态常量复制默认导入类型
        importMap.putAll(DEFAULT_IMPORT_MAP);
        //从静态常量复制接口到默认实现类的映射
        interfaceDefaultImplMap.putAll(DEFAULT_INTERFACE_IMPL_MAP);
    }

    //key:是作用域的id, 当作用域结束时, 从map中移除
    private Map<String, NfContextScope> scopeMap = new HashMap<>();
    //类型和类的全路径映射关系
    private Map<String, String> importMap = new HashMap<>();
    private Map<String, String> taskMap = new HashMap<>();
    //函数定义映射关系
    private Map<String, FunDefInfo> functionMap = new HashMap<>();
    //函数引用变量映射关系
    //key: 变量名, value: 函数引用信息（包括函数引用和 Lambda 表达式）
    private Map<String, FunRefInfo> funRefMap = new HashMap<>();
    //接口类型到默认实现类的映射关系
    private Map<Class<?>, Class<?>> interfaceDefaultImplMap = new HashMap<>();
    //导入的NF脚本作用域映射关系
    //key: 脚本名称, value: 脚本执行后的全局作用域
    private Map<String, NfContextScope> importedScriptScopeMap = new HashMap<>();
    //导入的NF脚本上下文映射关系
    //key: 脚本名称, value: 脚本的上下文（用于函数调用时访问脚本的函数定义和作用域）
    private Map<String, NfContext> importedScriptContextMap = new HashMap<>();
    
    //临时变量存储（用于递归处理函数调用时共享临时变量）
    //从 ThreadLocal 改为实例字段，因为每个脚本执行都有独立的 NfContext
    private Map<String, Object> tempVarStorage = new HashMap<>();
    
    //递归深度计数器，用于判断是否是最外层调用
    //从 ThreadLocal 改为实例字段，因为每个脚本执行都有独立的 NfContext
    private int recursionDepth = 0;

    //Lambda 表达式计数器，用于生成唯一的 Lambda 函数名
    private int lambdaCounter = 0;

    //脚本执行开始时间（毫秒时间戳）
    //用于计算脚本执行总时长，判断是否超时
    private long executionStartTime = 0;
    
    //上下文是否已被清除的标志
    //clear() 后设置为 true，防止误用导致 NPE
    private boolean cleared = false;

    /**
     * 检查上下文是否已被清除
     * 
     * @throws IllegalStateException 如果上下文已被清除
     */
    private void checkCleared() {
        if (cleared) {
            throw new IllegalStateException("Context has been cleared and cannot be used anymore");
        }
    }

    //获取类型的全路径
    public String getImportType(String type) {
        checkCleared();
        return importMap.get(type);
    }

    //获取task的全路径
    public String getTask(String taskName) {
        checkCleared();
        return taskMap.get(taskName);
    }

    //添加导入
    public void addImport(String type, String classPath) {
        checkCleared();
        importMap.put(type, classPath);
    }

    //添加task
    public void addTask(String taskName, String classPath) {
        checkCleared();
        taskMap.put(taskName, classPath);
    }

    //添加函数定义
    public void addFunction(String functionName, FunDefInfo funDef) {
        checkCleared();
        functionMap.put(functionName, funDef);
    }

    //获取函数定义
    public FunDefInfo getFunction(String functionName) {
        checkCleared();
        return functionMap.get(functionName);
    }

    //检查函数是否存在
    public boolean hasFunction(String functionName) {
        checkCleared();
        return functionMap.containsKey(functionName);
    }

    /**
     * 添加导入脚本的作用域
     * 
     * @param scriptName 脚本名称
     * @param scope 脚本的全局作用域
     */
    public void addImportedScriptScope(String scriptName, NfContextScope scope) {
        checkCleared();
        importedScriptScopeMap.put(scriptName, scope);
    }

    /**
     * 获取导入脚本的作用域
     * 
     * @param scriptName 脚本名称
     * @return 脚本的全局作用域，如果不存在返回 null
     */
    public NfContextScope getImportedScriptScope(String scriptName) {
        checkCleared();
        return importedScriptScopeMap.get(scriptName);
    }

    /**
     * 检查导入脚本是否存在
     * 
     * @param scriptName 脚本名称
     * @return 如果脚本已导入返回 true，否则返回 false
     */
    public boolean hasImportedScript(String scriptName) {
        checkCleared();
        return importedScriptScopeMap.containsKey(scriptName);
    }

    /**
     * 添加导入脚本的上下文
     * 
     * @param scriptName 脚本名称
     * @param scriptContext 脚本的上下文
     */
    public void addImportedScriptContext(String scriptName, NfContext scriptContext) {
        checkCleared();
        importedScriptContextMap.put(scriptName, scriptContext);
    }

    /**
     * 获取导入脚本的上下文
     * 
     * @param scriptName 脚本名称
     * @return 脚本的上下文，如果不存在返回 null
     */
    public NfContext getImportedScriptContext(String scriptName) {
        checkCleared();
        return importedScriptContextMap.get(scriptName);
    }


    //创建一个作用域
    public NfContextScope createScope(String id, String parentScopeId, NfContextScopeType type) {
        checkCleared();
        NfContextScope nfContextScope = new NfContextScope(id, parentScopeId, type);
        scopeMap.put(id, nfContextScope);
        return nfContextScope;
    }

    //获取当前作用域
    public NfContextScope getCurrentScope() {
        checkCleared();
        return scopeMap.get(currentScopeId);
    }

    //获取全局作用域
    public NfContextScope getMainScope() {
        checkCleared();
        return scopeMap.get(mainScopeId);
    }


    //获取一个作用域
    public NfContextScope getScope(String id) {
        checkCleared();
        return scopeMap.get(id);
    }

    //切换作用域
    public void switchScope(String id) {
        checkCleared();
        currentScopeId = id;
    }

    //创建子作用域
    //原理就是将主作用域和父作用域合并到新的作用域
    public NfContextScope createChildScope(String parentScopeId, NfContextScopeType type) {
        checkCleared();
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
        checkCleared();
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
        checkCleared();
        List<NfContextScope> list = new ArrayList<>();
        findByTypeScopeList(currentScopeId, type, list);
        return list;
    }

    //获取所有指定类型的活动作用域（遍历整个scopeMap）
    //用于breakall等需要影响所有FOR循环的场景，而不仅仅是当前作用域的祖先
    public List<NfContextScope> findAllActiveScopesByType(NfContextScopeType type) {
        checkCleared();
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
        checkCleared();
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
        checkCleared();
        return getVariable(name, currentScopeId);
    }

    //递归获取变量, 获取父作用域的变量
    private NfVariableInfo getVariable(String name, String scopeId) {
        if (scopeId == null) {
            return null;
        }

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
        checkCleared();
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
        checkCleared();
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

    /**
     * 开始脚本执行，记录执行开始时间
     * 此方法应在脚本执行开始时调用，用于后续的超时检查
     * 
     * <p>注意：使用精确时间戳记录开始时间，确保时间计算的准确性。</p>
     */
    public void startExecution() {
        checkCleared();
        // 使用精确时间戳记录开始时间，确保时间计算的准确性
        executionStartTime = System.currentTimeMillis();
    }

    /**
     * 检查脚本执行是否超时
     * 如果执行时间超过全局超时限制，抛出 NfTimeoutException
     * 
     * <p>优化：使用缓存的时间戳，避免频繁调用 System.currentTimeMillis()。
     * 在循环场景下，频繁调用会导致性能问题。
     * 时间精度为100毫秒，对于超时检查场景已经足够。</p>
     * 
     * @throws NfTimeoutException 如果脚本执行超时
     */
    public void checkTimeout() {
        if (cleared) {
            return; // 已清除的上下文不需要检查超时
        }
        if (executionStartTime > 0) {
            // 直接使用 System.currentTimeMillis()，因为 TimestampCache 已被移除
            long elapsed = System.currentTimeMillis() - executionStartTime;
            if (elapsed > globalTimeoutMillis) {
                throw new NfTimeoutException("脚本执行超时，已执行 %d 毫秒，超过限制 %d 毫秒", elapsed, globalTimeoutMillis);
            }
        }
    }

    /**
     * 初始化默认导入类型映射（私有静态方法）
     * 用于初始化静态常量 DEFAULT_IMPORT_MAP
     * 
     * @return 包含所有默认导入类型的Map
     */
    private static Map<String, String> initDefaultImportType() {
        Map<String, String> map = new HashMap<>();

        map.put("Object", Object.class.getName());

        //空链相关的工具
        map.put("NullChain", NullChain.class.getName());
        map.put("NullCheck", NullCheck.class.getName());
        map.put("NullCalculate", NullCalculate.class.getName());
        map.put("NullStream", NullStream.class.getName());
        map.put("OkHttp", OkHttp.class.getName());
        map.put("Null", Null.class.getName());
        map.put("DateFormatEnum", DateFormatEnum.class.getName());

        //函数引用类型
        map.put("Fun", FunRefInfo.class.getName());
        map.put("FunRef", FunRefInfo.class.getName());

        map.put("String", String.class.getName());
        map.put("Integer", Integer.class.getName());
        map.put("Float", Float.class.getName());
        map.put("Double", Double.class.getName());
        map.put("Boolean", Boolean.class.getName());

        map.put("HashMap", HashMap.class.getName());
        map.put("HashSet", HashSet.class.getName());
        map.put("TreeMap", TreeMap.class.getName());
        map.put("LinkedList", LinkedList.class.getName());
        map.put("ArrayList", ArrayList.class.getName());

        map.put("Map", Map.class.getName());
        map.put("Set", Set.class.getName());
        map.put("List", List.class.getName());

        map.put("CopyOnWriteArrayList", CopyOnWriteArrayList.class.getName());
        map.put("ConcurrentHashMap", ConcurrentHashMap.class.getName());

        //Date
        map.put("Date", Date.class.getName());
        //BigDecimal
        map.put("BigDecimal", BigDecimal.class.getName());
        //LocalDate
        map.put("LocalDate", LocalDate.class.getName());
        //LocalDateTime
        map.put("LocalDateTime", LocalDateTime.class.getName());
        //LocalTime
        map.put("LocalTime", LocalTime.class.getName());
        //DateTimeFormatter
        map.put("DateTimeFormatter", DateTimeFormatter.class.getName());

        //工具包
        map.put("Objects", Objects.class.getName());
        map.put("Arrays", Arrays.class.getName());
        map.put("Collections", Collections.class.getName());
        map.put("UUID", UUID.class.getName());
        map.put("Files", Files.class.getName());

        //字符串处理
        map.put("StringBuilder", StringBuilder.class.getName());
        map.put("StringBuffer", StringBuffer.class.getName());

        //数学工具
        map.put("Math", Math.class.getName());
        map.put("BigInteger", BigInteger.class.getName());

        //系统工具
        map.put("System", System.class.getName());
        map.put("Runtime", Runtime.class.getName());

        //IO 文件操作
        map.put("File", File.class.getName());
        map.put("Path", Path.class.getName());
        map.put("Paths", Paths.class.getName());

        //随机数
        map.put("Random", Random.class.getName());

        //正则表达式
        map.put("Pattern", Pattern.class.getName());
        map.put("Matcher", Matcher.class.getName());

        //编码解码
        map.put("Base64", Base64.class.getName());

        //网络
        map.put("URL", URL.class.getName());
        map.put("URI", URI.class.getName());

        //时间单位
        map.put("TimeUnit", TimeUnit.class.getName());

        //8大基本类型, 转化为包装类型
        map.put("int", Integer.class.getName());
        map.put("long", Long.class.getName());
        map.put("short", Short.class.getName());
        map.put("byte", Byte.class.getName());
        map.put("char", Character.class.getName());
        map.put("float", Float.class.getName());
        map.put("double", Double.class.getName());
        map.put("boolean", Boolean.class.getName());
        map.put("bool", Boolean.class.getName());

        return map;
    }

    /**
     * 初始化接口到默认实现类的映射（私有静态方法）
     * 用于初始化静态常量 DEFAULT_INTERFACE_IMPL_MAP
     * 
     * @return 包含接口到默认实现类映射的Map
     */
    private static Map<Class<?>, Class<?>> initInterfaceDefaultImplMap() {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(Map.class, HashMap.class);
        map.put(List.class, ArrayList.class);
        map.put(Set.class, HashSet.class);
        return map;
    }

    //获取接口的默认实现类
    public Class<?> getInterfaceDefaultImpl(Class<?> interfaceType) {
        return interfaceDefaultImplMap.get(interfaceType);
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
     * 添加函数引用
     *
     * @param varName 变量名
     * @param funRef 函数引用信息
     */
    public void addFunRef(String varName, FunRefInfo funRef) {
        checkCleared();
        funRefMap.put(varName, funRef);
    }

    /**
     * 获取函数引用
     *
     * @param varName 变量名
     * @return 函数引用信息，如果不存在返回 null
     */
    public FunRefInfo getFunRef(String varName) {
        checkCleared();
        return funRefMap.get(varName);
    }

    /**
     * 检查函数引用是否存在
     *
     * @param varName 变量名
     * @return 如果函数引用存在返回 true，否则返回 false
     */
    public boolean hasFunRef(String varName) {
        checkCleared();
        return funRefMap.containsKey(varName);
    }

    /**
     * 移除函数引用
     *
     * @param varName 变量名
     * @return 被移除的函数引用信息，如果不存在返回 null
     */
    public FunRefInfo removeFunRef(String varName) {
        checkCleared();
        return funRefMap.remove(varName);
    }

    /**
     * 销毁上下文并释放资源
     * 
     * <p><b>重要：</b>此方法会将内部字段清空并设置 cleared 标志，调用后对象不可再使用。
     * 此方法仅在脚本执行完成后调用，用于释放资源和减轻 GC 压力。</p>
     * 
     * <p><b>注意：</b>此方法与普通的 clear() 方法不同，它会彻底销毁对象状态。
     * 如果需要重用上下文，请创建新的 NfContext 实例。</p>
     * 
     * @see NfMain#run
     */
    public void clear() {
        // 设置清除标志，防止后续误用
        cleared = true;
        
        // 销毁所有作用域
        if (scopeMap != null) {
            for (Map.Entry<String, NfContextScope> entry : scopeMap.entrySet()) {
                entry.getValue().clear();
            }
            scopeMap.clear();
        }
        
        // 清空所有内部 Map（不清空，避免 NPE，但清空内容）
        if (importMap != null) {
            importMap.clear();
        }
        if (taskMap != null) {
            taskMap.clear();
        }
        if (functionMap != null) {
            functionMap.clear();
        }
        if (funRefMap != null) {
            funRefMap.clear();
        }
        if (importedScriptScopeMap != null) {
            importedScriptScopeMap.clear();
        }
        if (importedScriptContextMap != null) {
            importedScriptContextMap.clear();
        }
        if (interfaceDefaultImplMap != null) {
            interfaceDefaultImplMap.clear();
        }
        if (tempVarStorage != null) {
            tempVarStorage.clear();
        }

        // 重置其他字段
        recursionDepth = 0;
        executionStartTime = 0;
    }
}