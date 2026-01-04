import com.gitee.huanminabc.nullchain.language.token.*;
import java.util.*;

public class TestKeywordCheck {
    public static void main(String[] args) {
        String script = "fun if(int x)Integer {\n    return x\n}\nInteger result = if(10)\nexport result";
        
        // 模拟 tokenizer
        List<Token> tokens = new ArrayList<>();
        // 简化测试：直接测试关键字的 token 类型
        Token ifToken = new Token(TokenType.IF, "if", 1);
        Token identifierToken = new Token(TokenType.IDENTIFIER, "if", 1);
        
        System.out.println("IF token - type: " + ifToken.type + ", value: " + ifToken.value);
        System.out.println("IDENTIFIER token - type: " + identifierToken.type + ", value: " + identifierToken.value);
        
        // 测试 KeywordUtil
        System.out.println("isForbidKeyword(\"if\"): " + 
            com.gitee.huanminabc.nullchain.language.utils.KeywordUtil.isForbidKeyword("if"));
    }
}
