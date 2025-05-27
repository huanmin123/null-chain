package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.common.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.common.reflect.BeanCopyUtil;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.ExportSyntaxNode;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * 执行语法树最终返回结果
 */
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class NfRun {

    public static Object run(List<SyntaxNode> syntaxNodes, NfContext context, Logger logger, NullMap<String,Object> mainSystemContext) {
        String mainScopeId = NfContext.generateScopeId();
        //设置全局作用域
        context.setMainScopeId(mainScopeId);
        context.setCurrentScopeId(mainScopeId);
        //创建一个作用域(全局)
        NfContextScope scope = context.createScope(mainScopeId, null, NfContextScopeType.ALL);
        //添加日志
        if (logger != null) {
            scope.addVariable(new NfVariableInfo("log", logger, logger.getClass()));
        }

        //因为任务的缘故需要把系统上下文传递给语法树 先给默认值,便于测试, 后面的循环会覆盖的如果有的话
        scope.addVariable(new NfVariableInfo("params", null, null));
        scope.addVariable(new NfVariableInfo("preValue", null, null));
        scope.addVariable(new NfVariableInfo("threadFactoryName", ThreadFactoryUtil.DEFAULT_THREAD_FACTORY_NAME , String.class));

        //添加系统上下文
        if (mainSystemContext != null) {
            for (Map.Entry<String, Object> entry : mainSystemContext.entrySet()) {
                //对值都深度复制,避免被修改 , 虽然会有性能损耗,但是为了安全性,还是这样做
                Object deepCopy = BeanCopyUtil.deepCopy(entry.getValue());
                scope.addVariable(new NfVariableInfo(entry.getKey(), deepCopy, deepCopy.getClass()));
            }
        }

        SyntaxNodeFactory.executeAll(syntaxNodes, context);
        //获取返回值给调用者
        NfVariableInfo variable = scope.getVariable(ExportSyntaxNode.EXPORT);
        //清除上下文
        context.clear();
        return variable == null ? null : variable.getValue();
    }

    //运行语法树
    public static Object run(List<SyntaxNode> syntaxNodes, Logger logger, NullMap<String,Object> mainSystemContext) {
        //创建上下文
        NfContext context = new NfContext();
        return run(syntaxNodes, context, logger, mainSystemContext);
    }
    public static Object run(List<SyntaxNode> syntaxNodes) {
        //创建上下文
        NfContext context = new NfContext();
        return run(syntaxNodes, context, null, null);
    }

}
