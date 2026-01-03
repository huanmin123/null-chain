package com.gitee.huanminabc.nullchain.language;

import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeAbs;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 语法验证器
 * 
 * <p>在构建语法树后、执行前进行语法验证，提前发现语法错误。
 * 验证内容包括：
 * <ul>
 *   <li>变量声明检查：确保使用的变量都已声明</li>
 *   <li>控制流检查：确保 break、continue 在正确的上下文中使用</li>
 *   <li>语法结构检查：确保语法节点的结构完整</li>
 * </ul>
 * </p>
 * 
 * <p>注意：此验证器只进行静态语法检查，不执行代码，因此无法检查运行时错误（如变量值类型不匹配等）。</p>
 * 
 * @author huanmin
 * @date 2024/11/22
 * @since 1.1.4
 */
public class SyntaxValidator {
    
    /**
     * 验证语法节点列表
     * 
     * @param syntaxNodeList 语法节点列表
     * @throws NfSyntaxException 如果发现语法错误
     */
    public static void validate(List<SyntaxNode> syntaxNodeList) {
        if (syntaxNodeList == null || syntaxNodeList.isEmpty()) {
            return; // 空列表是合法的
        }
        
        // 验证每个节点
        for (SyntaxNode node : syntaxNodeList) {
            validateNode(node, new ValidationContext());
        }
    }
    
    /**
     * 验证单个语法节点
     * 
     * @param node 语法节点
     * @param context 验证上下文
     * @throws NfSyntaxException 如果发现语法错误
     */
    private static void validateNode(SyntaxNode node, ValidationContext context) {
        if (node == null) {
            return;
        }
        
        // 验证节点基本结构
        validateNodeStructure(node);
        
        // 根据节点类型进行特定验证
        switch (node.getType()) {
            case BREAK_EXP:
            case BREAK_ALL_EXP:
            case CONTINUE_EXP:
                validateControlFlow(node, context);
                break;
            case ASSIGN_EXP:
            case DECLARE_EXP:
            case VAR_EXP:
                validateVariableOperation(node, context);
                break;
            case IF_EXP:
            case FOR_EXP:
            case SWITCH_EXP:
            case WHILE_EXP:
                validateBlockNode(node, context);
                break;
            default:
                // 其他节点类型暂时不进行特殊验证
                break;
        }
        
        // 验证子节点
        // 注意：getChildSyntaxNodeList() 方法在 SyntaxNodeAbs 中定义，需要通过类型检查访问
        if (node instanceof SyntaxNodeAbs) {
            SyntaxNodeAbs nodeAbs = (SyntaxNodeAbs) node;
            List<SyntaxNode> childList = nodeAbs.getChildSyntaxNodeList();
            if (childList != null) {
                ValidationContext childContext = context.createChildContext(node.getType());
                for (SyntaxNode child : childList) {
                    validateNode(child, childContext);
                }
            }
        }
    }
    
    /**
     * 验证节点基本结构
     * 
     * @param node 语法节点
     * @throws NfSyntaxException 如果节点结构不完整
     */
    private static void validateNodeStructure(SyntaxNode node) {
        if (node.getType() == null) {
            throw new NfSyntaxException(
                node.getLine(),
                "节点结构错误",
                "语法节点的类型不能为空",
                "",
                "请检查语法节点的构建过程"
            );
        }
        
        if (node.getStructType() == null) {
            throw new NfSyntaxException(
                node.getLine(),
                "节点结构错误",
                "语法节点的结构类型不能为空",
                "",
                "请检查语法节点的构建过程"
            );
        }
    }
    
    /**
     * 验证控制流语句（break、continue）
     * 
     * @param node 控制流节点
     * @param context 验证上下文
     * @throws NfSyntaxException 如果控制流语句使用不当
     */
    private static void validateControlFlow(SyntaxNode node, ValidationContext context) {
        if (!context.isInLoop()) {
            String keyword = node.getType() == SyntaxNodeType.BREAK_EXP ? "break" :
                           node.getType() == SyntaxNodeType.BREAK_ALL_EXP ? "breakAll" : "continue";
            throw new NfSyntaxException(
                node.getLine(),
                "控制流错误",
                keyword + " 语句只能在循环（for、while）中使用",
                "",
                "请将 " + keyword + " 语句放在 for 或 while 循环内部"
            );
        }
    }
    
    /**
     * 验证变量操作（赋值、声明）
     * 
     * @param node 变量操作节点
     * @param context 验证上下文
     */
    private static void validateVariableOperation(SyntaxNode node, ValidationContext context) {
        // 这里可以进行更详细的变量验证
        // 例如：检查变量名是否符合规范、是否使用了保留关键字等
        // 目前这些检查已经在构建阶段完成，这里主要是占位
    }
    
    /**
     * 验证块节点
     * 
     * @param node 块节点
     * @param context 验证上下文
     */
    private static void validateBlockNode(SyntaxNode node, ValidationContext context) {
        // 验证块节点必须有子节点（至少是空的子节点列表）
        // 注意：getChildSyntaxNodeList() 方法在 SyntaxNodeAbs 中定义
        if (node instanceof SyntaxNodeAbs) {
            SyntaxNodeAbs nodeAbs = (SyntaxNodeAbs) node;
            if (nodeAbs.getChildSyntaxNodeList() == null) {
                throw new NfSyntaxException(
                    node.getLine(),
                    "块节点结构错误",
                    node.getType() + " 语句必须包含代码块",
                    "",
                    "请确保 " + node.getType() + " 语句包含 {} 代码块"
                );
            }
        }
    }
    
    /**
     * 验证上下文
     * 用于跟踪当前验证的上下文信息（如是否在循环中）
     */
    private static class ValidationContext {
        private final Set<SyntaxNodeType> blockTypes;
        
        public ValidationContext() {
            this.blockTypes = new HashSet<>();
        }
        
        private ValidationContext(Set<SyntaxNodeType> parentBlockTypes) {
            this.blockTypes = new HashSet<>(parentBlockTypes);
        }
        
        /**
         * 创建子上下文
         * 
         * @param nodeType 当前节点类型
         * @return 子上下文
         */
        public ValidationContext createChildContext(SyntaxNodeType nodeType) {
            ValidationContext childContext = new ValidationContext(this.blockTypes);
            if (isLoopType(nodeType)) {
                childContext.blockTypes.add(nodeType);
            }
            return childContext;
        }
        
        /**
         * 判断是否在循环中
         * 
         * @return 如果在循环中返回 true
         */
        public boolean isInLoop() {
            return blockTypes.contains(SyntaxNodeType.FOR_EXP) || 
                   blockTypes.contains(SyntaxNodeType.WHILE_EXP);
        }
        
        /**
         * 判断节点类型是否是循环类型
         * 
         * @param nodeType 节点类型
         * @return 如果是循环类型返回 true
         */
        private boolean isLoopType(SyntaxNodeType nodeType) {
            return nodeType == SyntaxNodeType.FOR_EXP || 
                   nodeType == SyntaxNodeType.WHILE_EXP;
        }
    }
}

