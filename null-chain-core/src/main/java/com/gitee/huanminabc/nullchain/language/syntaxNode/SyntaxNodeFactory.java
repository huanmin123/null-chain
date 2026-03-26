package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.NfPerformanceMonitor;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.ForSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.FunDefSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.IFSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.SwitchSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.DoWhileSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.WhileSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.*;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * 语法节点工厂类
 * 
 * <p>负责管理所有语法节点的识别器实例。工厂中的节点实例主要用于：
 * <ul>
 *   <li>识别Token：通过analystToken()方法判断Token是否可以解析为对应的语法节点</li>
 *   <li>测试用途：getSyntaxNode()方法返回的实例可用于测试</li>
 * </ul>
 * </p>
 * 
 * <p>注意：buildStatement()方法中会创建新的节点实例，而不是使用工厂中的实例。
 * 这是因为每个语法节点在语法树中都是独立的实例。</p>
 * 
 * @author huanmin
 * @date 2024/11/22
 * @since 1.1.4
 */
public class SyntaxNodeFactory {
    /**
     * 语法节点识别器映射表
     * 存储所有语法节点类型的识别器实例，用于识别Token
     * 使用LinkedHashMap保证识别顺序的稳定性
     */
    private static final Map<SyntaxNodeType, SyntaxNode> syntaxNodeMap = new LinkedHashMap<>();
    private static final Map<TokenType, List<SyntaxNode>> syntaxNodeCandidatesByFirstToken = new EnumMap<>(TokenType.class);
    
    static {
        registerRecognizer(SyntaxNodeType.IMPORT_EXP, new ImportSyntaxNode(), TokenType.IMPORT);
        registerRecognizer(SyntaxNodeType.FUN_DEF_EXP, new FunDefSyntaxNode(), TokenType.FUN); // 函数定义优先级高于函数调用
        registerRecognizer(SyntaxNodeType.RETURN_EXP, new ReturnSyntaxNode(), TokenType.RETURN); // return语句优先级高于函数调用
        registerRecognizer(SyntaxNodeType.VAR_EXP, new VarSyntaxNode(), TokenType.VAR);
        registerRecognizer(SyntaxNodeType.LAMBDA_EXP, new LambdaSyntaxNode(), TokenType.FUN_TYPE); // Lambda表达式优先级高于ASSIGN，避免被当作普通赋值处理
        registerRecognizer(SyntaxNodeType.ASSIGN_EXP, new AssignSyntaxNode(), TokenType.IDENTIFIER, TokenType.FUN_TYPE);
        registerRecognizer(SyntaxNodeType.FUN_REF_EXP, new FunRefSyntaxNode(), TokenType.FUN_TYPE); // 函数引用（特殊的赋值语句）
        registerRecognizer(SyntaxNodeType.DECLARE_EXP, new DeclareSyntaxNode(), TokenType.IDENTIFIER);
        registerRecognizer(SyntaxNodeType.RUN_EXP, new RunSyntaxNode(), TokenType.RUN);
        registerRecognizer(SyntaxNodeType.EXPORT_EXP, new ExportSyntaxNode(), TokenType.EXPORT);
        registerRecognizer(SyntaxNodeType.ECHO_EXP, new EchoSyntaxNode(), TokenType.ECHO);
        registerRecognizer(SyntaxNodeType.FUN_CALL_EXP, new FunCallSyntaxNode(), TokenType.IDENTIFIER); // 函数调用
        registerRecognizer(SyntaxNodeType.FUN_EXE_EXP, new FunExeSyntaxNode(), TokenType.IDENTIFIER);
        registerRecognizer(SyntaxNodeType.IF_EXP, new IFSyntaxNode(), TokenType.IF);
        registerRecognizer(SyntaxNodeType.WHILE_EXP, new WhileSyntaxNode(), TokenType.WHILE);
        registerRecognizer(SyntaxNodeType.DO_WHILE_EXP, new DoWhileSyntaxNode(), TokenType.DO);
        registerRecognizer(SyntaxNodeType.SWITCH_EXP, new SwitchSyntaxNode(), TokenType.SWITCH);
        registerRecognizer(SyntaxNodeType.FOR_EXP, new ForSyntaxNode(), TokenType.FOR);
        registerRecognizer(SyntaxNodeType.BREAK_EXP, new BreakSyntaxNode(), TokenType.BREAK);
        registerRecognizer(SyntaxNodeType.BREAK_ALL_EXP, new BreakALLSyntaxNode(), TokenType.BREAK_ALL);
        registerRecognizer(SyntaxNodeType.CONTINUE_EXP, new ContinueSyntaxNode(), TokenType.CONTINUE);
    }

    private static void registerRecognizer(SyntaxNodeType type, SyntaxNode recognizer, TokenType... firstTokenTypes) {
        syntaxNodeMap.put(type, recognizer);
        for (TokenType firstTokenType : firstTokenTypes) {
            syntaxNodeCandidatesByFirstToken
                .computeIfAbsent(firstTokenType, key -> new ArrayList<>())
                .add(recognizer);
        }
    }

    /**
     * 获取指定类型的语法节点识别器实例
     * 
     * <p>主要用于测试和识别Token。注意：此方法返回的是识别器实例，
     * 不是用于构建语法树的新实例。buildStatement()方法中会创建新实例。</p>
     * 
     * @param type 语法节点类型
     * @return 语法节点识别器实例，如果类型不存在返回null
     */
    public static SyntaxNode getSyntaxNode(SyntaxNodeType type) {
        return syntaxNodeMap.get(type);
    }
    /**
     * 遍历所有节点识别器分析Token，进行解析构建语法树
     * 
     * <p>使用工厂中的识别器实例来识别Token，如果识别成功则调用buildStatement()构建语法节点。
     * buildStatement()方法会创建新的节点实例并添加到语法树中。</p>
     * 
     * @param tokens Token列表
     * @param syntaxNodeList 语法节点列表（用于添加新构建的节点）
     * @return 如果成功识别并构建了节点返回true，否则返回false
     */
    public static boolean forEachNode(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        if (tokens == null || tokens.isEmpty()) {
            return false;
        }

        List<SyntaxNode> candidateRecognizers = syntaxNodeCandidatesByFirstToken.get(tokens.get(0).type);
        if (tryBuild(candidateRecognizers, tokens, syntaxNodeList)) {
            return true;
        }

        Set<SyntaxNode> triedRecognizers = candidateRecognizers == null ? null : new HashSet<>(candidateRecognizers);
        for (Map.Entry<SyntaxNodeType, SyntaxNode> entry : syntaxNodeMap.entrySet()) {
            SyntaxNode recognizer = entry.getValue();
            if (triedRecognizers != null && triedRecognizers.contains(recognizer)) {
                continue;
            }
            if (recognizer.analystToken(tokens)) {
                return recognizer.buildStatement(tokens, syntaxNodeList);
            }
        }
        return false;
    }

    private static boolean tryBuild(List<SyntaxNode> recognizers, List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        if (recognizers == null || recognizers.isEmpty()) {
            return false;
        }
        for (SyntaxNode recognizer : recognizers) {
            if (recognizer.analystToken(tokens)) {
                return recognizer.buildStatement(tokens, syntaxNodeList);
            }
        }
        return false;
    }

    /**
     * 遍历全部的语法节点并执行
     * 
     * <p>按照语法节点的顺序执行，支持break、continue和breakAll控制流。</p>
     * 
     * @param syntaxNodeList 语法节点列表
     * @param context NF上下文
     */
    public static void executeAll(List<SyntaxNode> syntaxNodeList, NfContext context) {
        executeAll(syntaxNodeList, context, null);
    }
    
    /**
     * 遍历全部的语法节点并执行（支持性能监控）
     * 
     * <p>按照语法节点的顺序执行，支持break、continue和breakAll控制流。
     * 如果提供了性能监控器，会记录每个节点的执行时间。</p>
     * 
     * @param syntaxNodeList 语法节点列表
     * @param context NF上下文
     * @param monitor 性能监控器（可选，如果为null则不监控）
     */
    public static void executeAll(List<SyntaxNode> syntaxNodeList, NfContext context, NfPerformanceMonitor monitor) {
        NfPerformanceMonitor actualMonitor = monitor != null ? monitor : context.getPerformanceMonitor();
        for (SyntaxNode syntaxNode : syntaxNodeList) {
            //检查超时（在执行节点前检查）
            context.checkTimeout();
            
            //保留当前的作用域id
            String currentScopeId = context.getCurrentScopeId();
            
            //记录执行开始时间
            long startTime = actualMonitor != null ? System.nanoTime() : 0;
            
            syntaxNode.run(context, syntaxNode);
            
            //记录执行时间
            if (actualMonitor != null) {
                long duration = System.nanoTime() - startTime;
                actualMonitor.recordNodeExecution(syntaxNode.getType().name(), duration);
            }
            
            //恢复当前的作用域id
            context.setCurrentScopeId(currentScopeId);
            
            //检查超时（在执行节点后检查）
            context.checkTimeout();

            //检查是否需要中断执行
            //优先检查全局breakAll标志（由breakall语句设置）
            if (context.isGlobalBreakAll()) {
                break;
            }

            NfContextScope currentScope = context.getCurrentScope();
            //判断作用域中是否存在break或者continue,如果存在break或者continue,则跳出当前循环
            boolean shouldBreak = currentScope.isBreak() || currentScope.isContinue();
            //只有当作用域是FOR类型时，breakAll标志才会导致跳出循环
            //主作用域（ALL类型）的breakAll标志应该被忽略
            if (currentScope.isBreakAll() && currentScope.getType() == NfContextScopeType.FOR) {
                shouldBreak = true;
            }
            if (shouldBreak) {
                break;
            }
        }
    }
}
