package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.jcommon.multithreading.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.core.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullNode;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.language.NfCheckException;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.DataType;
import com.gitee.huanminabc.nullchain.language.utils.SyntaxNodeUtil;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/*
  运行任务表达式: run test1( a,b )
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class RunSyntaxNode extends LineSyntaxNode {
    public RunSyntaxNode() {
        super(SyntaxNodeType.RUN_EXP);
    }
    
    public RunSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.RUN;
    }


    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            //判断是否是RUN
            if (token.type == TokenType.RUN) {
                //优化：缓存size，避免在循环中重复调用
                //记录结束下标, 用于截取和删除
                int endIndex = SyntaxNodeUtil.findLineEndIndex(tokens, i);
                //截取Run语句的标记序列 不包含Run和LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i + 1, endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();

                //去掉注释
                SyntaxNodeUtil.removeComments(newToken);

                //校验run语句的参数语法有效性
                validateRunSyntax(newToken, token.line);

                //判断是否存在-> 我们需要把->后面的变量转化为DeclareSyntaxNode
                for (int j = 0; j < newToken.size(); j++) {
                    Token t = newToken.get(j);
                    if (t.type == TokenType.ARROW) {

                        //截取->后面的标记序列
                        List<Token> newToken2 = new ArrayList<>(newToken.subList(j + 1, newToken.size()));
                        //判断长度是否为3
                        if (newToken2.size() != 3) {
                            throw new NfException("Line:{}, run语句错误,箭头后面的变量声明错误,tokens: {}", token.line, TokenUtil.mergeToken(newToken).toString());
                        }
                        //删除->后面的标记
                        newToken.subList(j, newToken.size()).clear();


                        //创建DeclareSyntaxNode
                        DeclareSyntaxNode declareSyntaxNode = new DeclareSyntaxNode(SyntaxNodeType.DECLARE_EXP);
                        //将v:String  转化为String v
                        Token name = new Token(TokenType.IDENTIFIER, newToken2.get(0).value, newToken2.get(0).getLine());
                        Token type = new Token(TokenType.IDENTIFIER, newToken2.get(2).value, newToken2.get(2).getLine());

                        //判断类型是否是合法的

                        newToken2 = new ArrayList<>(Arrays.asList(type, name));
                        declareSyntaxNode.setValue(newToken2);
                        //设置行号
                        declareSyntaxNode.setLine(t.getLine());
                        syntaxNodeList.add(declareSyntaxNode);

                        //给newToken最后添加一个箭头, 用于区分结束
                        newToken.add(new Token(TokenType.ARROW, "->", t.getLine()));

                        //给newToken最后添加一个变量
                        newToken.add(name);

                        break;
                    }
                }

                RunSyntaxNode runSyntaxNode = new RunSyntaxNode(SyntaxNodeType.RUN_EXP);
                runSyntaxNode.setValue(newToken);
                //设置行号
                runSyntaxNode.setLine(token.getLine());
                syntaxNodeList.add(runSyntaxNode);
                return true;
            }
        }
        return false;
    }



    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        NfContextScope mainScope = context.getMainScope();
        NfContextScope currentScope = context.getCurrentScope();
        List<Token> value = syntaxNode.getValue();
        if (value == null || value.isEmpty()) {
            throw new NfException("Line:{} ,run表达式tokens不能为空 , syntax: {}", 
                syntaxNode.getLine(), syntaxNode);
        }
        //如果最后一位是IDENTIFIER就是需要赋值的变量,需要取出来
        if (value.size() < 2) {
            throw new NfException("Line:{} ,run表达式格式错误，tokens数量不足 , syntax: {}", 
                syntaxNode.getLine(), syntaxNode);
        }
        Token tokenArrowAssign = value.get(value.size() - 2);
        Token tokenVariate = value.get(value.size() - 1);

        boolean isVariate = tokenArrowAssign.type == TokenType.ARROW && tokenVariate.type == TokenType.IDENTIFIER;

        List<NullNode<String, List<Object>>> nullNodes = new ArrayList<>();
        //是这样解析的   test1( a ,b ),test1( a ,b )
        //第一个是任务名字 然后跳过(  然后是参数  然后跳过)  ,如果后面的是,就是下一个任务
        for (int i = 0; i < value.size(); i++) {
            Token token = value.get(i);
            //遇到箭头符号那么就结束了,这里不需要处理后面的内容
            if (token.type == TokenType.ARROW) {
                break;
            }
            if (token.type == TokenType.IDENTIFIER) {
                String taskName = token.value;
                //跳过IDENTIFIER
                i++;
                //跳过(
                i++;
                //参数
                List<Object> params = new ArrayList<>();
                for (int j = i; j < value.size(); j++) {
                    Token token1 = value.get(j);
                    //如果是逗号就跳过
                    if (token1.type == TokenType.COMMA) {
                        continue;
                    }
                    if (token1.type == TokenType.RPAREN) {
                        i = j;
                        break;
                    }
                    if (token1.type == TokenType.IDENTIFIER) {
                        //需要取出来实际的值
                        NfVariableInfo variable1 = context.getVariable(token1.value);
                        if (variable1 == null) {
                            throw new NfException("Line:{}, 变量{}不存在 , syntax: {}", syntaxNode.getLine(), token1.value, syntaxNode);
                        }
                        params.add(variable1.getValue());
                    } else {//这种是常量
                        switch (token1.type) {
                            case STRING:
                                String value1 = token1.value;
                                //去掉前后的引号
                                value1 = value1.substring(1, value1.length() - 1);
                                // 如果包含占位符，进行替换
                                if (value1.contains("{") && value1.contains("}")) {
                                    value1 = EchoSyntaxNode.replaceTemplate(value1, context);
                                }
                                params.add(value1);
                                break;
                            case TEMPLATE_STRING:
                                // 处理模板字符串，去除首尾的 ```，然后替换占位符
                                String templateValue = (String) DataType.realType(TokenType.TEMPLATE_STRING, token1.value);
                                templateValue = EchoSyntaxNode.replaceTemplate(templateValue, context);
                                params.add(templateValue);
                                break;
                            case INTEGER:
                                params.add(Double.parseDouble(token1.value));
                                break;
                            case BOOLEAN:
                                params.add(Boolean.parseBoolean(token1.value));
                                break;
                            case FLOAT:
                                params.add(Float.parseFloat(token1.value));
                                break;
                            default:
                                throw new NfException("Line:{}, 参数类型错误,只支持String,Number,Boolean, syntax:{}", syntaxNode.getLine(), syntaxNode);
                        }
                    }
                }
                nullNodes.add(new NullNode<>(taskName, params));
            }
        }
        //如果大于1个任务就是并行
        if (nullNodes.size() > 1) {
            //校验是否有同类型的任务
            for (int i = 0; i < nullNodes.size(); i++) {
                NullNode<String, List<Object>> nullNode = nullNodes.get(i);
                for (int j = i + 1; j < nullNodes.size(); j++) {
                    NullNode<String, List<Object>> nullNode1 = nullNodes.get(j);
                    if (nullNode.getKey().equals(nullNode1.getKey())) {
                        throw new NfException("Line:{}, 并发不支持同名称{}任务重复执行 , syntax: {}", syntaxNode.getLine(), nullNode.getKey(), syntaxNode);
                    }
                }
            }
            Map<String, Object> nullChainMap = new ConcurrentHashMap<>();

            //获取线程池名称（使用 $ 前缀的系统变量）
            NfVariableInfo threadFactory = mainScope.getVariable("$threadFactoryName");
            String threadFactoryName = (String) threadFactory.getValue();
            ThreadPoolExecutor executor = ThreadFactoryUtil.getExecutor(threadFactoryName);
            List<Future<?>> futures = new ArrayList<>();
            for (NullNode<String, List<Object>> nullNode : nullNodes) {
                String taskName = nullNode.getKey();
                //将任务转换为实际路径
                String newTaskName = context.getTask(taskName);
                if (newTaskName == null) {
                    throw new NfException("Line:{}, {}任务不存在 , syntax: {}", syntaxNode.getLine(), taskName, syntaxNode);
                }
                List<Object> taskValue = nullNode.getValue();
                Future<?> submit = executor.submit(() -> {
                    try {
                        Object run = runTask(newTaskName, context, taskValue, syntaxNode);
                        if (Null.is(run)) {
                            return;
                        }
                        nullChainMap.put(newTaskName, run);
                    } catch (NfCheckException e) {
                        log.error("", e);
                    } catch (Exception e) {
                        log.error("Line:{}, {}任务并发执行失败, syntax: {}", syntaxNode.getLine(), newTaskName, syntaxNode);
                    }
                });
                futures.add(submit);
            }

            //等待所有任务执行完毕
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception ignored) {

                }
            }
            //判断是否需要赋值
            if (isVariate) {
                NfVariableInfo variable = context.getVariable(tokenVariate.value);
                if (variable == null) {
                    throw new NfException("Line:{}, 变量{}不存在 , syntax: {}", syntaxNode.getLine(), tokenVariate.value, syntaxNode);
                }
                Class<?> variableType = variable.getType();
                //判断类型是否一致（使用isAssignableFrom支持类型兼容性，如Map接口和ConcurrentHashMap实现类）
                if (!variableType.isAssignableFrom(nullChainMap.getClass())) {
                    throw new NfException("Line:{}, 变量 {} 值类型和声明的型不匹配 {} vs {} ,syntax:{}", syntaxNode.getLine(), tokenVariate.value, variableType, nullChainMap.getClass(), syntaxNode);
                }
                //保持变量声明的类型（如果是接口类型，保持接口类型，而不是实际对象的类型）
                currentScope.addVariable(new NfVariableInfo(tokenVariate.value, nullChainMap, variableType));
            }
        } else {
            NullNode<String, List<Object>> stringListNullNode = nullNodes.get(0);
            String taskName = stringListNullNode.getKey();
            //将任务转换为实际路径
            String newTaskName = context.getTask(taskName);
            if (newTaskName == null) {
                throw new NfException("Line:{}, {}任务不存在  , syntax: {}", syntaxNode.getLine(), taskName, syntaxNode);
            }
            List<Object> taskValue = stringListNullNode.getValue();
            Object o = null;
            try {
                o = runTask(newTaskName, context, taskValue, syntaxNode);
            } catch (NfCheckException e) {
                log.error("", e);
            }
            //判断是否需要赋值
            if (isVariate) {
                if (Null.is(o)) {
                    return;
                }
                NfVariableInfo variable = context.getVariable(tokenVariate.value);
                if (variable == null) {
                    throw new NfException("Line:{}, 变量{}不存在 , syntax: {}", syntaxNode.getLine(), tokenVariate.value, syntaxNode);
                }
                Class<?> variableType = variable.getType();
                //判断类型是否一致（使用isAssignableFrom支持类型兼容性）
                if (!variableType.isAssignableFrom(o.getClass())) {
                    throw new NfException("Line:{}, 变量 {} 值类型和声明的型不匹配 {} vs {} , syntax: {}", syntaxNode.getLine(), tokenVariate.value, variableType, o.getClass(), syntaxNode);
                }
                //保持变量声明的类型（如果是接口类型，保持接口类型，而不是实际对象的类型）
                currentScope.addVariable(new NfVariableInfo(tokenVariate.value, o, variableType));
            }
        }
    }

    //运行任务
    private Object runTask(String taskName, NfContext context, List<Object> args, SyntaxNode syntaxNode) throws NfCheckException {
        NullTask nullTask = NullTaskFactory.getTask(taskName);
        NfContextScope mainScope = context.getMainScope();
        //必然有值,不然就进不来这里（使用 $ 前缀的系统变量）
        NfVariableInfo preValue= mainScope.getVariable("$preValue");
        Map<String, Object> mainScopeMap = new HashMap<>();
        //函数的参数
        Object[] array = args.toArray();
        //校验参数类型和长度
        NullType nullType = nullTask.checkTypeParams();
        try {
            if (nullType != null) {
                nullType.checkType(array, mainScopeMap);
            }
        } catch (Exception e) {
            throw new NfCheckException(e, "Line:{}, 任务{}参数校验失败 , syntax: {}", syntaxNode.getLine(), taskName, syntaxNode);
        }
        NullChain<Object>[] nullChains = NullBuild.arrayToNullChain(array);
        try {
            nullTask.init(preValue.getValue(), nullChains, mainScopeMap);
        } catch (Exception e) {
            throw new NfCheckException(e, "Line:{}, 任务{}初始化失败 , syntax: {}", syntaxNode.getLine(), taskName, syntaxNode);
        }
        try {
            return nullTask.run(preValue.getValue(), nullChains, mainScopeMap);
        } catch (Exception e) {
            throw new NfCheckException(e, "Line:{}, 任务{}运行失败 , syntax: {}", syntaxNode.getLine(), taskName, syntaxNode);
        }
    }


    /**
     * 校验run语句的参数语法有效性
     * @param tokens run语句的token列表（不包含run关键字）
     * @param line 行号
     */
    private void validateRunSyntax(List<Token> tokens, int line) {
        //遍历tokens，检查每个任务调用的语法
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            //遇到箭头符号那么就结束了,后面不需要处理
            if (token.type == TokenType.ARROW) {
                break;
            }
            if (token.type == TokenType.IDENTIFIER) {
                //跳过IDENTIFIER（任务名）
                i++;
                if (i >= tokens.size()) {
                    throw new NfException("Line:{}, run语句语法错误，任务名后缺少参数列表", line);
                }
                //检查下一个是否是左括号
                if (tokens.get(i).type != TokenType.LPAREN) {
                    throw new NfException("Line:{}, run语句语法错误，任务名后必须是左括号'('", line);
                }
                //找到右括号
                int rparenIndex = -1;
                for (int j = i + 1; j < tokens.size(); j++) {
                    if (tokens.get(j).type == TokenType.RPAREN) {
                        rparenIndex = j;
                        break;
                    }
                }
                if (rparenIndex == -1) {
                    throw new NfException("Line:{}, run语句语法错误，缺少右括号')'", line);
                }
                //检查括号内是否有尾随逗号（逗号后直接是右括号）
                if (rparenIndex > i + 1) {
                    Token beforeRParen = tokens.get(rparenIndex - 1);
                    if (beforeRParen.type == TokenType.COMMA) {
                        throw new NfException("Line:{}, run语句语法错误，参数列表不能以逗号结尾", line);
                    }
                }
                //移动i到右括号位置
                i = rparenIndex;
            }
        }
    }

    @Override
    public String toString() {
        return "run " + TokenUtil.mergeToken(getValue());
    }
}
