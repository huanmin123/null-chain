package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.common.executor.ThreadFactoryUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.sync.NullChain;
import com.gitee.huanminabc.nullchain.common.NullBuild;
import com.gitee.huanminabc.nullchain.common.NullNode;
import com.gitee.huanminabc.nullchain.common.NullType;
import com.gitee.huanminabc.nullchain.language.NfCheckException;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeAbs;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeStructType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.vessel.NullMap;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 运行任务表达式: run test1( a,b )
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Slf4j
public class RunSyntaxNode extends SyntaxNodeAbs implements SyntaxNode {
    public RunSyntaxNode(SyntaxNodeType type) {
        super(type);
        super.setStructType(SyntaxNodeStructType.LINE_NODE);
    }


    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        // 遍历标记序列
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            //判断是否是RUN
            if (token.type == TokenType.RUN) {
                //记录结束下标, 用于截取和删除
                int endIndex = 0;
                //遇到LINE_END结束
                for (int j = i; j < tokens.size(); j++) {
                    if (tokens.get(j).type == TokenType.LINE_END) {
                        endIndex = j;
                        break;
                    }
                }
                //截取Run语句的标记序列 不包含Run和LINE_END
                List<Token> newToken = new ArrayList(tokens.subList(i + 1, endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();

                //去掉注释
                newToken.removeIf(t -> t.type == TokenType.COMMENT);

                //判断是否存在-> 我们需要把->后面的变量转化为DeclareSyntaxNode
                for (int j = 0; j < newToken.size(); j++) {
                    Token t = newToken.get(j);
                    if (t.type == TokenType.ARROW_ASSIGN) {

                        //截取->后面的标记序列
                        List<Token> newToken2 = new ArrayList(newToken.subList(j + 1, newToken.size()));
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
                        newToken.add(new Token(TokenType.ARROW_ASSIGN, "->", t.getLine()));

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
    public boolean analystToken(List<Token> tokens) {
        Token token = tokens.get(0);
        if (token.type == TokenType.RUN) {
            return true;
        }
        return false;
    }

    @Override
    public boolean analystSyntax(SyntaxNode syntaxNode) {
        return syntaxNode instanceof RunSyntaxNode;
    }

    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        NfContextScope mainScope = context.getMainScope();
        NfContextScope currentScope = context.getCurrentScope();
        List<Token> value = syntaxNode.getValue();
        //如果最后一位是IDENTIFIER就是需要赋值的变量,需要取出来
        Token tokenArrowAssign = value.get(value.size() - 2);
        Token tokenVariate = value.get(value.size() - 1);

        boolean isVariate = false;
        if (tokenArrowAssign.type == TokenType.ARROW_ASSIGN && tokenVariate.type == TokenType.IDENTIFIER) {
            isVariate = true;
        }

        List<NullNode<String, List<Object>>> nullNodes = new ArrayList<>();
        //是这样解析的   test1( a ,b ),test1( a ,b )
        //第一个是任务名字 然后跳过(  然后是参数  然后跳过)  ,如果后面的是,就是下一个任务
        for (int i = 0; i < value.size(); i++) {
            Token token = value.get(i);
            //遇到箭头符号那么就结束了,这里不需要处理后面的内容
            if (token.type == TokenType.ARROW_ASSIGN) {
                break;
            }
            if (token.type == TokenType.IDENTIFIER) {
                String taskName = token.value;
                //跳过IDENTIFIER
                i++;
                //跳过(
                i++;
                //参数
                List<Object> params = new ArrayList();
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
                                params.add(value1);
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
                        params.add(token1.value);
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
            NullMap<String, Object> nullChainMap = NullMap.newConcurrentHashMap();

            //获取线程池名称
            NfVariableInfo threadFactory = mainScope.getVariable("threadFactoryName");
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
                //判断类型是否一致
                if (!variableType.equals(nullChainMap.getClass())) {
                    throw new NfException("Line:{}, 变量 {} 值类型和声明的型不匹配 {} vs {} ,syntax:{}", syntaxNode.getLine(), tokenVariate.value, variableType, nullChainMap.getClass(), syntaxNode);
                }
                currentScope.addVariable(new NfVariableInfo(tokenVariate.value, nullChainMap, nullChainMap.getClass()));
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
                //判断类型是否一致
                if (!variableType.equals(o.getClass())) {
                    throw new NfException("Line:{}, 变量 {} 值类型和声明的型不匹配 {} vs {} , syntax: {}", syntaxNode.getLine(), tokenVariate.value, variableType, o.getClass(), syntaxNode);
                }
                currentScope.addVariable(new NfVariableInfo(tokenVariate.value, o, o.getClass()));
            }
        }
    }

    //运行任务
    private Object runTask(String taskName, NfContext context, List<Object> args, SyntaxNode syntaxNode) throws NfCheckException {
        NullTask nullTask = NullTaskFactory.getTask(taskName);
        NfContextScope mainScope = context.getMainScope();
        //必然有值,不然就进不来这里
        NfVariableInfo preValue= mainScope.getVariable("preValue");
        NullMap<String, Object> mainScopeMap = NullMap.newHashMap();
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


    @Override
    public String toString() {
        return "run " + TokenUtil.mergeToken(getValue());
    }
}
