package com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.NfSyntaxException;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.internal.NfVariableInfo;

import com.gitee.huanminabc.nullchain.language.syntaxNode.BlockSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeFactory;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * for表达式支持两种模式:
 * 1. 数值范围: for i in 1..10 {}
 * 2. 变量迭代: for item in list {} 或 for k, v in map {}
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class ForSyntaxNode extends BlockSyntaxNode {
    // 循环类型标识
    private ForLoopType loopType;

    public ForSyntaxNode() {
        super(SyntaxNodeType.FOR_EXP);
    }

    public ForSyntaxNode(SyntaxNodeType type) {
        super(type);
    }

    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.FOR;
    }

    @Override
    public boolean buildStatement(List<Token> tokens, List<SyntaxNode> syntaxNodeList) {
        // 优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.FOR) {
                // 记录结束下标, 用于截取和删除
                int endIndex = skipForEnd(tokens, i);
                // 截取for表达式的标记序列
                List<Token> forTokens = new ArrayList<>(tokens.subList(i, endIndex));
                // 删除
                tokens.subList(i, endIndex).clear();

                ForSyntaxNode forStatement = new ForSyntaxNode(SyntaxNodeType.FOR_EXP);
                forStatement.setValue(forTokens);
                forStatement.setLine(token.getLine());
                // 构建子节点
                if (!buildChildStatement(forStatement)) {
                    return false;
                }
                syntaxNodeList.add(forStatement);
                return true;
            }
        }
        return false;
    }

    /**
     * 构建for语句的子节点
     *
     * <p>此方法会解析for语句的tokens，提取循环条件和循环体，构建子节点。
     * 支持两种循环模式：
     * <ul>
     *   <li>RANGE: for i in 1..10 { ... }</li>
     *   <li>VARIABLE_ITERATION: for item in list { ... } 或 for k, v in map { ... }</li>
     * </ul>
     *
     * <p><b>副作用说明</b>：此方法会修改传入的 syntaxNode 节点：
     * <ul>
     *   <li>会修改 syntaxNode.getValue() 返回的 tokens 列表（移除已解析的 tokens）</li>
     *   <li>会将原始的 tokens 分解，条件部分保留在父节点的 value 中，循环体部分构建为子节点</li>
     * </ul>
     *
     * @param syntaxNode for语句节点（会被修改）
     * @return 如果成功构建返回 true，否则返回 false
     */
    @Override
    public boolean buildChildStatement(SyntaxNode syntaxNode) {
        // for的token
        List<Token> tokenList = syntaxNode.getValue();
        // 将条件提取出来
        // 去掉开头的FOR
        tokenList.remove(0);
        // 一直找到第一个{+LINE_END
        int endIndex = 0;
        // 优化：缓存size，避免在循环中重复调用
        int tokenListSize = tokenList.size();
        for (int i = 0; i < tokenListSize; i++) {
            Token token = tokenList.get(i);
            if (token.type == TokenType.LBRACE && tokenList.get(i + 1).type == TokenType.LINE_END) {
                endIndex = i;
                break;
            }
        }
        // 截取for表达式条件
        List<Token> forTokens = new ArrayList<>(tokenList.subList(0, endIndex));

        // 基本语法验证：for语句至少需要3个token: 变量名 in ...
        if (forTokens.size() < 3) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                    forTokens.isEmpty() ? 0 : forTokens.get(0).line,
                    "for表达式语法错误",
                    "for语句格式不完整",
                    context,
                    "支持的格式：for i in 1..10 {}, for item in list {}, for k, v in map {}"
            );
        }

        // 检查第一个token是否为IDENTIFIER（变量名）
        if (forTokens.get(0).type != TokenType.IDENTIFIER) {
            String context = printFor(forTokens);
            throw new NfSyntaxException(
                    forTokens.get(0).line,
                    "for表达式语法错误",
                    "for循环变量名必须是标识符",
                    context,
                    "支持的格式：for i in 1..10 {}, for item in list {}, for k, v in map {}"
            );
        }

        // 判断是否为双变量模式（Map迭代）: for k, v in map
        boolean isDoubleVariable = forTokens.size() >= 5 && forTokens.get(1).type == TokenType.COMMA;

        // 根据模式验证语法
        if (isDoubleVariable) {
            // 双变量模式: IDENTIFIER, COMMA, IDENTIFIER, IN, IDENTIFIER
            if (forTokens.size() < 5) {
                String context = printFor(forTokens);
                throw new NfSyntaxException(
                        forTokens.get(0).line,
                        "for表达式语法错误",
                        "双变量模式格式不完整，应为：for k, v in map",
                        context,
                        "正确格式：for k, v in map"
                );
            }
            // 检查第3个token是否为IDENTIFIER
            if (forTokens.get(2).type != TokenType.IDENTIFIER) {
                String context = printFor(forTokens);
                throw new NfSyntaxException(
                        forTokens.get(2).line,
                        "for表达式语法错误",
                        "双变量模式中，逗号后必须是标识符",
                        context,
                        "正确格式：for k, v in map"
                );
            }
            // 检查第4个token是否为IN
            if (forTokens.get(3).type != TokenType.IN) {
                String context = printFor(forTokens);
                throw new NfSyntaxException(
                        forTokens.get(3).line,
                        "for表达式语法错误",
                        "for语句缺少in关键字",
                        context,
                        "正确格式：for k, v in map"
                );
            }
            // 检查第5个token是否为IDENTIFIER（目标变量）
            if (forTokens.get(4).type != TokenType.IDENTIFIER) {
                String context = printFor(forTokens);
                throw new NfSyntaxException(
                        forTokens.get(4).line,
                        "for表达式语法错误",
                        "in关键字后必须是变量名",
                        context,
                        "正确格式：for k, v in map"
                );
            }
            // 双变量模式使用VARIABLE_ITERATION
            ((ForSyntaxNode) syntaxNode).setLoopType(ForLoopType.VARIABLE_ITERATION);
        } else {
            // 单变量模式: IDENTIFIER IN ...
            // 检查第二个token是否为IN关键字
            if (forTokens.get(1).type != TokenType.IN) {
                String context = printFor(forTokens);
                throw new NfSyntaxException(
                        forTokens.get(1).line,
                        "for表达式语法错误",
                        "for语句缺少in关键字",
                        context,
                        "正确格式：for i in 1..10 或 for item in list"
                );
            }

            // 判断循环类型：检查是否有..符号
            boolean hasDot2 = forTokens.stream().anyMatch(t -> t.type == TokenType.DOT2);

            if (hasDot2) {
                // RANGE模式: for i in 1..10
                ((ForSyntaxNode) syntaxNode).setLoopType(ForLoopType.RANGE);
                // 验证格式：IDENTIFIER IN INTEGER DOT2 INTEGER
                if (forTokens.size() != 5) {
                    String context = printFor(forTokens);
                    throw new NfSyntaxException(
                            forTokens.get(0).line,
                            "for范围循环语法错误",
                            "范围循环格式应为：for i in 起始值..结束值",
                            context,
                            "正确格式：for i in 1..10"
                    );
                }
                // 检查第3个和第5个token的类型
                Token startToken = forTokens.get(2);
                Token endToken = forTokens.get(4);
                // 第3个token必须是整数或标识符
                if (startToken.type != TokenType.INTEGER && startToken.type != TokenType.IDENTIFIER) {
                    String context = printFor(forTokens);
                    throw new NfSyntaxException(
                            startToken.line,
                            "for范围循环语法错误",
                            "范围起始值必须是整数或变量名",
                            context,
                            "正确格式：for i in 1..10 或 for i in start..end"
                    );
                }
                // 第4个token必须是DOT2
                if (forTokens.get(3).type != TokenType.DOT2) {
                    String context = printFor(forTokens);
                    throw new NfSyntaxException(
                            forTokens.get(3).line,
                            "for范围循环语法错误",
                            "范围值之间必须使用..符号连接",
                            context,
                            "正确格式：for i in 1..10"
                    );
                }
                // 第5个token必须是整数或标识符
                if (endToken.type != TokenType.INTEGER && endToken.type != TokenType.IDENTIFIER) {
                    String context = printFor(forTokens);
                    throw new NfSyntaxException(
                            endToken.line,
                            "for范围循环语法错误",
                            "范围结束值必须是整数或变量名",
                            context,
                            "正确格式：for i in 1..10 或 for i in start..end"
                    );
                }
                // 验证范围顺序：如果起始值和结束值都是整数字面量，检查起始值是否大于结束值
                if (startToken.type == TokenType.INTEGER && endToken.type == TokenType.INTEGER) {
                    int start = Integer.parseInt(startToken.value);
                    int end = Integer.parseInt(endToken.value);
                    if (start > end) {
                        String context = printFor(forTokens);
                        throw new NfSyntaxException(
                                startToken.line,
                                "for范围循环错误",
                                "起始值不能大于结束值",
                                context,
                                "请修正范围顺序，例如: for i in 1..10"
                        );
                    }
                }
            } else {
                // VARIABLE_ITERATION模式: for item in list
                // 验证格式：第3个token必须是IDENTIFIER（目标变量名）
                if (forTokens.size() < 3 || forTokens.get(2).type != TokenType.IDENTIFIER) {
                    String context = printFor(forTokens);
                    throw new NfSyntaxException(
                            forTokens.size() >= 3 ? forTokens.get(2).line : forTokens.get(0).line,
                            "for表达式语法错误",
                            "集合迭代的目标必须是变量名",
                            context,
                            "正确格式：for item in list"
                    );
                }
                // 在运行时根据变量的实际类型判断是List还是Map
                ((ForSyntaxNode) syntaxNode).setLoopType(ForLoopType.VARIABLE_ITERATION);
            }
        }

        // 删除
        tokenList.subList(0, endIndex).clear();
        // 删除{+LINE_END
        tokenList.remove(0);
        tokenList.remove(0);
        // 删除最后的}
        tokenList.remove(tokenList.size() - 1);
        // 设置for条件
        syntaxNode.setValue(forTokens);
        // 构建子节点
        List<SyntaxNode> syntaxNodes = NfSynta.buildMainStatement(tokenList);
        if (!(syntaxNode instanceof ForSyntaxNode)) {
            throw new NfException("Line:{} ,语法节点类型错误，期望ForSyntaxNode，实际:{}",
                    syntaxNode.getLine(), syntaxNode.getClass().getName());
        }
        ((ForSyntaxNode) syntaxNode).setChildSyntaxNodeList(syntaxNodes);
        return true;
    }


    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        if (!(syntaxNode instanceof ForSyntaxNode)) {
            throw new NfException("Line:{} ,语法节点类型错误，期望ForSyntaxNode，实际:{}",
                    syntaxNode.getLine(), syntaxNode.getClass().getName());
        }
        ForSyntaxNode forSyntaxNode = (ForSyntaxNode) syntaxNode;
        ForLoopType loopType = forSyntaxNode.getLoopType();
        List<SyntaxNode> childList = forSyntaxNode.getChildSyntaxNodeList();

        // 保留当前作用域id
        String currentScopeId = context.getCurrentScopeId();

        // 根据循环类型执行不同的逻辑
        if (loopType == ForLoopType.RANGE) {
            executeRangeLoop(context, forSyntaxNode, childList, currentScopeId);
        } else if (loopType == ForLoopType.VARIABLE_ITERATION) {
            executeVariableIteration(context, forSyntaxNode, childList, currentScopeId);
        }
    }

    /**
     * 解析范围值，支持常量和变量两种形式
     *
     * <p>解析优先级：
     * <ol>
     *   <li>首先尝试作为整数字面量解析（如 "1", "10"）</li>
     *   <li>如果解析失败，则作为变量名从上下文中获取值</li>
     * </ol>
     *
     * <p><b>类型限制</b>：只支持整数类型（Integer, Long, Short, Byte），
     * 不支持浮点数（Double, Float），小数会直接报错。
     *
     * @param context  NF上下文
     * @param valueStr 值字符串（可能是常量或变量名）
     * @param line     行号（用于错误提示）
     * @return 解析后的整数值
     * @throws NfException 如果值无法解析为整数、变量不存在、或是小数
     */
    private int parseRangeValue(NfContext context, String valueStr, int line) {
        // 快速检查：如果是小数字面量，直接报错
        if (valueStr.contains(".")) {
            throw new NfException(
                    "Line:{} ,for范围循环不支持小数，范围值: {}",
                    line, valueStr
            );
        }
        // 尝试解析为整数字面量
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            // 不是整数字面量，尝试从上下文获取变量值
            NfVariableInfo variable = context.getVariable(valueStr);
            if (variable == null) {
                throw new NfException(
                        "Line:{} ,范围值 '{}' 既不是整数字面量，也不是已定义的变量",
                        line, valueStr
                );
            }
            Object value = variable.getValue();
            if (value == null) {
                throw new NfException(
                        "Line:{} ,变量 '{}' 的值为null，无法作为范围值使用",
                        line, valueStr
                );
            }
            // 浮点数类型直接报错（必须在整数检查之前）
            if (value instanceof Double || value instanceof Float) {
                throw new NfException(
                        "Line:{} ,for范围循环不支持小数，变量 '{}' 的值为小数: {}",
                        line, valueStr, value
                );
            }
            // 只支持整数类型（Integer, Long, Short, Byte）
            if (value instanceof Integer || value instanceof Long ||
                value instanceof Short || value instanceof Byte) {
                return ((Number) value).intValue();
            }
            throw new NfException("Line:{} ,变量 '{}' 的值不是整数类型，实际类型: {}", line, valueStr, value.getClass().getSimpleName()
            );
        }
    }

    /**
     * 执行数值范围循环: for i in 1..10 { ... } 或 for i in start..end { ... }
     */
    private void executeRangeLoop(NfContext context, ForSyntaxNode forSyntaxNode,
                                  List<SyntaxNode> childList, String currentScopeId) {
        List<Token> forValue = forSyntaxNode.getValue();
        if (forValue == null || forValue.size() < 5) {
            throw new NfException("Line:{} ,for表达式格式错误，必须包含变量名、in关键字和范围值 , syntax:{} ",
                    forSyntaxNode.getLine(), forSyntaxNode);
        }
        // i in 1..10 或 for i in start..end (动态范围)
        // 取出循环变量名
        String i = forValue.get(0).value;
        // 取出start（可能是常量或变量名）
        String start = forValue.get(2).value;
        // 取出end（可能是常量或变量名）
        String end = forValue.get(4).value;
        // 优化：提前解析start和end，支持常量和变量两种形式
        int startInt = parseRangeValue(context, start, forSyntaxNode.getLine());
        int endInt = parseRangeValue(context, end, forSyntaxNode.getLine());

        // 循环
        if (childList == null) {
            // for循环体为空，直接返回
            return;
        }
        for (int j = startInt; j <= endInt; j++) {
            // 检查超时（每次循环迭代检查）
            context.checkTimeout();
            
            // 检查全局breakAll标志（由breakall语句设置）
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 创建子作用域
            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            // 将i的值赋值
            newScope.addVariable(new NfVariableInfo(i, j, Integer.class));
            // 执行子节点
            if (childList != null) {
                SyntaxNodeFactory.executeAll(childList, context);
            }
            // 移除子作用域
            context.removeScope(newScope.getScopeId());

            // 执行子节点后再次检查globalBreakAll标志（breakall可能在子节点中被触发）
            // 如果globalBreakAll被设置，立即跳出循环，不要执行后续的清除操作
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 如果是break,那么就跳出当前的循环
            if (newScope.isBreak()) {
                break;
            }
            // 如果是breakall,那么就跳出所有循环
            if (newScope.isBreakAll()) {
                // 传播breakAll标志到所有祖先FOR作用域
                // 这样可以处理嵌套场景，例如：FOR -> FOR -> IF，IF中的breakall需要传播到外层FOR
                propagateBreakAllToAncestorFors(context, currentScopeId);
                break;
            }
        }
        // 循环结束后，清除当前FOR作用域的breakAll标志
        NfContextScope currentForScope = context.getScope(currentScopeId);
        if (currentForScope != null && currentForScope.getType() == NfContextScopeType.FOR) {
            currentForScope.setBreakAll(false);
        }
        // 只有当父作用域不是FOR类型时，才清除全局breakAll标志
        // 这样可以确保嵌套的FOR循环也能正确响应breakall
        NfContextScope parentScope = context.getScope(currentForScope != null ? currentForScope.getParentScopeId() : null);
        if (parentScope == null || parentScope.getType() != NfContextScopeType.FOR) {
            context.setGlobalBreakAll(false);
        }
    }

    /**
     * 执行变量迭代循环（在运行时根据变量实际类型决定是List还是Map）
     * 支持两种格式：
     * - for item in list { ... }
     * - for k, v in map { ... }
     */
    private void executeVariableIteration(NfContext context, ForSyntaxNode forSyntaxNode,
                                          List<SyntaxNode> childList, String currentScopeId) {
        List<Token> forValue = forSyntaxNode.getValue();
        if (forValue == null || forValue.isEmpty()) {
            throw new NfException("Line:{} ,for表达式格式错误，必须包含变量名和in关键字 , syntax:{} ",
                    forSyntaxNode.getLine(), forSyntaxNode);
        }

        // 判断是单变量还是双变量模式
        boolean hasComma = forValue.stream().anyMatch(t -> t.type == TokenType.COMMA);
        String varName1 = forValue.get(0).value;
        String varName2 = null;
        String targetVarName = null;

        if (hasComma) {
            // 双变量模式: for k, v in map
            // 格式：IDENTIFIER, COMMA, IDENTIFIER, IN, IDENTIFIER
            if (forValue.size() < 5) {
                throw new NfException("Line:{} ,for表达式格式错误，双变量模式需要: key, value in map , syntax:{} ",
                        forSyntaxNode.getLine(), forSyntaxNode);
            }
            varName2 = forValue.get(2).value;
            targetVarName = forValue.get(4).value;
        } else {
            // 单变量模式: for item in list
            // 格式：IDENTIFIER, IN, IDENTIFIER
            if (forValue.size() < 3) {
                throw new NfException("Line:{} ,for表达式格式错误，单变量模式需要: item in list , syntax:{} ",
                        forSyntaxNode.getLine(), forSyntaxNode);
            }
            targetVarName = forValue.get(2).value;
        }

        // 从上下文获取目标变量
        NfVariableInfo targetVarInfo = context.getVariable(targetVarName);
        if (targetVarInfo == null) {
            throw new NfException("Line:{} ,变量 {} 不存在 , syntax:{} ",
                    forSyntaxNode.getLine(), targetVarName, forSyntaxNode);
        }

        Object targetValue = targetVarInfo.getValue();
        // 检查是否为null
        if (targetValue == null) {
            throw new NfException("Line:{} ,变量 {} 的值为null , syntax:{} ",
                    forSyntaxNode.getLine(), targetVarName, forSyntaxNode);
        }

        // 根据变量实际类型决定是List、Set还是Map迭代
        if (targetValue instanceof java.util.Map) {
            // Map迭代
            executeMapIteration(context, forSyntaxNode, childList, currentScopeId,
                    varName1, varName2, targetValue);
        } else if (targetValue instanceof java.util.List || targetValue.getClass().isArray()) {
            // List迭代
            executeListIteration(context, forSyntaxNode, childList, currentScopeId,
                    varName1, targetValue);
        } else if (targetValue instanceof java.util.Set) {
            // Set迭代
            executeSetIteration(context, forSyntaxNode, childList, currentScopeId,
                    varName1, targetValue);
        } else {
            throw new NfException("Line:{} ,变量 {} 不是List、Map、Set或数组类型，实际类型: {} , syntax:{} ",
                    forSyntaxNode.getLine(), targetVarName, targetValue.getClass().getSimpleName(), forSyntaxNode);
        }
    }

    /**
     * 执行List迭代（实际执行部分）
     */
    private void executeListIteration(NfContext context, ForSyntaxNode forSyntaxNode,
                                      List<SyntaxNode> childList, String currentScopeId,
                                      String itemName, Object listValue) {
        // 转换为List
        java.util.List<?> list = null;
        if (listValue instanceof java.util.List) {
            list = (java.util.List<?>) listValue;
        } else if (listValue.getClass().isArray()) {
            // 数组转换为List
            list = java.util.Arrays.asList((Object[]) listValue);
        }

        // 循环
        if (childList == null) {
            // for循环体为空，直接返回
            return;
        }

        for (Object item : list) {
            // 检查超时（每次循环迭代检查）
            context.checkTimeout();
            
            // 检查全局breakAll标志（由breakall语句设置）
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 创建子作用域
            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            // 将当前元素的值赋给循环变量
            Class<?> itemClass = item != null ? item.getClass() : Object.class;
            newScope.addVariable(new NfVariableInfo(itemName, item, itemClass));
            // 执行子节点
            if (childList != null) {
                SyntaxNodeFactory.executeAll(childList, context);
            }
            // 移除子作用域
            context.removeScope(newScope.getScopeId());

            // 执行子节点后再次检查globalBreakAll标志
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 如果是break,那么就跳出当前的循环
            if (newScope.isBreak()) {
                break;
            }
            // 如果是breakall,那么就跳出所有循环
            if (newScope.isBreakAll()) {
                propagateBreakAllToAncestorFors(context, currentScopeId);
                break;
            }
        }
        // 循环结束后，清除当前FOR作用域的breakAll标志
        NfContextScope currentForScope = context.getScope(currentScopeId);
        if (currentForScope != null && currentForScope.getType() == NfContextScopeType.FOR) {
            currentForScope.setBreakAll(false);
        }
        // 只有当父作用域不是FOR类型时，才清除全局breakAll标志
        NfContextScope parentScope = context.getScope(currentForScope != null ? currentForScope.getParentScopeId() : null);
        if (parentScope == null || parentScope.getType() != NfContextScopeType.FOR) {
            context.setGlobalBreakAll(false);
        }
    }

    /**
     * 执行Map迭代（实际执行部分）
     */
    private void executeMapIteration(NfContext context, ForSyntaxNode forSyntaxNode,
                                     List<SyntaxNode> childList, String currentScopeId,
                                     String keyName, String valueName, Object mapValue) {
        @SuppressWarnings("unchecked")
        java.util.Map<Object, Object> map = (java.util.Map<Object, Object>) mapValue;

        // 循环
        if (childList == null) {
            // for循环体为空，直接返回
            return;
        }

        for (java.util.Map.Entry<Object, Object> entry : map.entrySet()) {
            // 检查超时（每次循环迭代检查）
            context.checkTimeout();
            
            // 检查全局breakAll标志（由breakall语句设置）
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 创建子作用域
            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            // 将键的值赋给键变量
            Object key = entry.getKey();
            Class<?> keyClass = key != null ? key.getClass() : Object.class;
            newScope.addVariable(new NfVariableInfo(keyName, key, keyClass));
            // 将值的值赋给值变量
            Object value = entry.getValue();
            Class<?> valueClass = value != null ? value.getClass() : Object.class;
            newScope.addVariable(new NfVariableInfo(valueName, value, valueClass));
            // 执行子节点
            if (childList != null) {
                SyntaxNodeFactory.executeAll(childList, context);
            }
            // 移除子作用域
            context.removeScope(newScope.getScopeId());

            // 执行子节点后再次检查globalBreakAll标志
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 如果是break,那么就跳出当前的循环
            if (newScope.isBreak()) {
                break;
            }
            // 如果是breakall,那么就跳出所有循环
            if (newScope.isBreakAll()) {
                propagateBreakAllToAncestorFors(context, currentScopeId);
                break;
            }
        }
        // 循环结束后，清除当前FOR作用域的breakAll标志
        NfContextScope currentForScope = context.getScope(currentScopeId);
        if (currentForScope != null && currentForScope.getType() == NfContextScopeType.FOR) {
            currentForScope.setBreakAll(false);
        }
        // 只有当父作用域不是FOR类型时，才清除全局breakAll标志
        NfContextScope parentScope = context.getScope(currentForScope != null ? currentForScope.getParentScopeId() : null);
        if (parentScope == null || parentScope.getType() != NfContextScopeType.FOR) {
            context.setGlobalBreakAll(false);
        }
    }

    /**
     * 执行Set迭代（实际执行部分）
     */
    private void executeSetIteration(NfContext context, ForSyntaxNode forSyntaxNode,
                                     List<SyntaxNode> childList, String currentScopeId,
                                     String itemName, Object setValue) {
        // 转换为Set
        java.util.Set<?> set = null;
        if (setValue instanceof java.util.Set) {
            set = (java.util.Set<?>) setValue;
        } else {
            throw new NfException("Line:{} ,变量 {} 不是Set类型，实际类型: {} , syntax:{} ",
                    forSyntaxNode.getLine(), itemName, setValue.getClass().getSimpleName(), forSyntaxNode);
        }

        // 循环
        if (childList == null) {
            // for循环体为空，直接返回
            return;
        }

        for (Object item : set) {
            // 检查超时（每次循环迭代检查）
            context.checkTimeout();
            
            // 检查全局breakAll标志（由breakall语句设置）
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 创建子作用域
            NfContextScope newScope = context.createChildScope(currentScopeId, NfContextScopeType.FOR);
            // 将当前元素的值赋给循环变量
            Class<?> itemClass = item != null ? item.getClass() : Object.class;
            newScope.addVariable(new NfVariableInfo(itemName, item, itemClass));
            // 执行子节点
            if (childList != null) {
                SyntaxNodeFactory.executeAll(childList, context);
            }
            // 移除子作用域
            context.removeScope(newScope.getScopeId());

            // 执行子节点后再次检查globalBreakAll标志
            if (context.isGlobalBreakAll()) {
                break;
            }
            // 如果是break,那么就跳出当前的循环
            if (newScope.isBreak()) {
                break;
            }
            // 如果是breakall,那么就跳出所有循环
            if (newScope.isBreakAll()) {
                propagateBreakAllToAncestorFors(context, currentScopeId);
                break;
            }
        }
        // 循环结束后，清除当前FOR作用域的breakAll标志
        NfContextScope currentForScope = context.getScope(currentScopeId);
        if (currentForScope != null && currentForScope.getType() == NfContextScopeType.FOR) {
            currentForScope.setBreakAll(false);
        }
        // 只有当父作用域不是FOR类型时，才清除全局breakAll标志
        NfContextScope parentScope = context.getScope(currentForScope != null ? currentForScope.getParentScopeId() : null);
        if (parentScope == null || parentScope.getType() != NfContextScopeType.FOR) {
            context.setGlobalBreakAll(false);
        }
    }

    //跳到For结束位置获取结束下标
    private int skipForEnd(List<Token> tokens, int i) {
        return BlockSyntaxNode.skipBlockEnd(tokens, i, false);
    }

    //打印for 的token
    private String printFor(List<Token> tokens) {
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            //遇到LINE_END换行
            if (token.type == TokenType.LINE_END) {
                sb.append("\n");
                continue;
            }
            //遇到DOT2跳过
            if (token.type == TokenType.DOT2) {
                sb.append(token.value);
                continue;
            }
            //如果是INTEGER,判断前一个如果是DOT2
            if (token.type == TokenType.INTEGER) {
                if (i > 0 && tokens.get(i - 1).type == TokenType.DOT2) {
                    sb.append(token.value);
                    continue;
                }
            }
            sb.append(" ").append(token.value);
        }
        return sb.toString();
    }

    /**
     * 将breakAll标志传播到所有祖先FOR作用域
     * 这个方法处理嵌套循环场景，确保breakall能够跳出所有层级的FOR循环
     *
     * @param context 上下文
     * @param scopeId 起始作用域ID（当前FOR作用域）
     */
    private void propagateBreakAllToAncestorFors(NfContext context, String scopeId) {
        String currentScopeId = scopeId;
        // 向上遍历作用域链，找到所有FOR类型的作用域并设置breakAll标志
        while (currentScopeId != null) {
            NfContextScope scope = context.getScope(currentScopeId);
            if (scope == null) {
                break;
            }
            // 如果是FOR作用域，设置breakAll标志
            if (scope.getType() == NfContextScopeType.FOR) {
                scope.setBreakAll(true);
            }
            // 如果到达主作用域（ALL类型），停止传播
            if (scope.getType() == NfContextScopeType.ALL) {
                break;
            }
            // 继续向上遍历
            currentScopeId = scope.getParentScopeId();
        }
    }

}
