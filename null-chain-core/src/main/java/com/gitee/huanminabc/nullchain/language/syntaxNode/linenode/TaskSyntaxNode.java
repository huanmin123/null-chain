package com.gitee.huanminabc.nullchain.language.syntaxNode.linenode;

import com.gitee.huanminabc.nullchain.common.NullConstants;
import com.gitee.huanminabc.nullchain.language.NfException;
import com.gitee.huanminabc.nullchain.language.internal.NfContext;
import com.gitee.huanminabc.nullchain.language.syntaxNode.LineSyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNode;
import com.gitee.huanminabc.nullchain.language.syntaxNode.SyntaxNodeType;
import com.gitee.huanminabc.nullchain.language.token.Token;
import com.gitee.huanminabc.nullchain.language.token.TokenType;
import com.gitee.huanminabc.nullchain.language.utils.TokenUtil;
import com.gitee.huanminabc.nullchain.task.NullTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * task语句  例如: task com.xxx.ClassTaskName as test1
 */

/**
 * @author huanmin
 * @date 2024/11/22
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class TaskSyntaxNode extends LineSyntaxNode {
    public TaskSyntaxNode() {
        super(SyntaxNodeType.TASK_EXP);
    }
    
    public TaskSyntaxNode(SyntaxNodeType type) {
        super(type);
    }
    
    @Override
    protected TokenType getTargetTokenType() {
        return TokenType.TASK;
    }

    @Override
    public boolean buildStatement(List<Token> tokens,List<SyntaxNode> syntaxNodeList) {
        //优化：缓存size，避免在循环中重复调用
        int tokensSize = tokens.size();
        // 遍历标记序列
        for (int i = 0; i < tokensSize; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.TASK) {
                //记录结束下标, 用于截取和删除
                int endIndex = findLineEndIndex(tokens, i);
                //截取task语句的标记序列 不包含task和LINE_END
                List<Token> newToken = new ArrayList<>(tokens.subList(i + 1, endIndex));
                //删除已经解析的标记
                tokens.subList(i, endIndex).clear();
                //去掉注释
                removeComments(newToken);
                //如果是newToken的长度小于3,那么就是语法错误
                if (newToken.size() < 3) {
                    throw new NfException("Line:{} ,task 语句错误,语法错误 , syntax: task {} ???", token.getLine(), TokenUtil.mergeToken(newToken).toString());
                }

                //去掉as和之后的
                int asIndex = 0;
                for (int j = 0; j < newToken.size(); j++) {
                    if (newToken.get(j).type == TokenType.AS) {
                        asIndex = j;
                        break;
                    }
                }
                //如果没有as
                if (asIndex == 0) {
                    throw new NfException("Line:{} ,task 语句错误,缺少as关键字 , syntax: task {}", token.getLine(), TokenUtil.mergeToken(newToken).toString());
                }
                //如果长度一样就有问题, 或者类型不是标识符
                if (newToken.size() == asIndex + 1 || newToken.get(asIndex + 1).type != TokenType.IDENTIFIER) {
                    throw new NfException("Line:{} ,task 语句错误,as关键字后面必须是标识符 , syntax: task {}", token.getLine(), TokenUtil.mergeToken(newToken).toString());
                }

                List<Token> taskToken = new ArrayList<>(newToken.subList(0, asIndex));

                //校验import语句是否合法
                String imp = TokenUtil.mergeToken(taskToken).toString();

                try {
                    Class.forName(imp);
                } catch (ClassNotFoundException e) {
                    throw new NfException("Line:{} ,import {} 找不到导入的类", token.getLine(), imp);
                }

                TaskSyntaxNode taskStatement = new TaskSyntaxNode(SyntaxNodeType.TASK_EXP);
                taskStatement.setValue(newToken);
                //设置行号
                taskStatement.setLine(token.getLine());
                syntaxNodeList.add(taskStatement);

                return true;
            }

        }
        return false;
    }



    @Override
    public void run(NfContext context, SyntaxNode syntaxNode) {
        List<Token> value = syntaxNode.getValue();
        if (value == null || value.isEmpty()) {
            throw new NfException("Line:{} ,task表达式tokens不能为空 , syntax: {}", 
                syntaxNode.getLine(), syntaxNode);
        }
        //最后一个token就是类型
        String type = value.get(value.size() - 1).value;
        //截取到as的位置
        int asIndex = 0;
        for (int i = 0; i < value.size(); i++) {
            if (value.get(i).type == TokenType.AS) {
                asIndex = i;
                break;
            }
        }
        //截取到as的位置
        List<Token> taskToken = new ArrayList<>(value.subList(0, asIndex));
        StringBuilder sb = new StringBuilder(NullConstants.STRING_BUILDER_INITIAL_CAPACITY);
        for (Token token : taskToken) {
            sb.append(token.value);
        }
        context.addTask(type,sb.toString());

        try {
            //如果之前没有注册,那么我这里手动尝试注册
            NullTask task = NullTaskFactory.getTask(sb.toString());
            if (task == null) {
                Class<?> aClass = Class.forName(sb.toString());
                NullTaskFactory.registerTask((Class)aClass);
            }
        } catch (ClassNotFoundException e) {
            throw new NfException(e,"Line:{} ,task 语句错误,找不到导入的任务类:{}", syntaxNode.getLine(), sb.toString());
        }

    }


}