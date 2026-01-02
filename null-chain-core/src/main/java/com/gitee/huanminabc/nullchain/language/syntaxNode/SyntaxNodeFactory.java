package com.gitee.huanminabc.nullchain.language.syntaxNode;

import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScope;
import com.gitee.huanminabc.nullchain.language.internal.NfContextScopeType;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.ForSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.IFSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.blocknode.SwitchSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.linenode.*;
import com.gitee.huanminabc.nullchain.language.token.Token;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * @author huanmin
 * @date 2024/11/22
 */
public class SyntaxNodeFactory {
   private static final Map<SyntaxNodeType, SyntaxNode> syntaxNodeMap=new HashMap<>();
    static{
        syntaxNodeMap.put(SyntaxNodeType.IMPORT_EXP,new ImportSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.TASK_EXP,new TaskSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.ASSIGN_EXP,new AssignSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.DECLARE_EXP,new DeclareSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.RUN_EXP,new RunSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.EXPORT_EXP,new ExportSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.ECHO_EXP,new EchoSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.FUN_EXE_EXP,new FunExeSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.IF_EXP,new IFSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.SWITCH_EXP,new SwitchSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.FOR_EXP,new ForSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.BREAK_EXP,new BreakSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.BREAK_ALL_EXP,new BreakALLSyntaxNode());
        syntaxNodeMap.put(SyntaxNodeType.CONTINUE_EXP,new ContinueSyntaxNode());

    }

    public static SyntaxNode getSyntaxNode(SyntaxNodeType type){
        return syntaxNodeMap.get(type);
    }
    //遍历所有节点分析Token,进行解析构建语法树
    public static boolean forEachNode(List<Token> tokens, List<SyntaxNode> syntaxNodeList){
        Set<Map.Entry<SyntaxNodeType, SyntaxNode>> entries = syntaxNodeMap.entrySet();
        for (Map.Entry<SyntaxNodeType, SyntaxNode> entry : entries) {
            SyntaxNode node = entry.getValue();
            if (node.analystToken(tokens)){
               return node.buildStatement(tokens,syntaxNodeList);
            }
        }
        return false;
    }

    //遍历全部的语法节点并执行
    public static void executeAll(List<SyntaxNode> syntaxNodeList, NfContext context){
        for (SyntaxNode syntaxNode : syntaxNodeList) {
            if (syntaxNode.analystSyntax(syntaxNode)){
                //保留当前的作用域id
                String currentScopeId = context.getCurrentScopeId();
                syntaxNode.run(context, syntaxNode);
                //恢复当前的作用域id
                context.setCurrentScopeId(currentScopeId);
                NfContextScope currentScope = context.getCurrentScope();
                //判断作用域中是否存在break或者continue,如果存在break或者continue,则跳出当前循环
                //注意：breakAll只应该在FOR循环中生效，不应该影响主作用域（ALL类型）的后续语句
                boolean shouldBreak = currentScope.isBreak() || currentScope.isContinue();
                //只有当作用域是FOR类型时，breakAll标志才会导致跳出循环
                //主作用域（ALL类型）的breakAll标志应该被忽略
                if (currentScope.isBreakAll() && currentScope.getType() == NfContextScopeType.FOR) {
                    shouldBreak = true;
                }
                if (shouldBreak) {
//                    System.out.println("=========:"+currentScope.getType());
                    break;
                }
            }
        }
    }
}
