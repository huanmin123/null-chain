import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.test.nullchain.utils.TestUtil;
import java.util.List;

public class TestTokens {
    public static void main(String[] args) {
        String file = TestUtil.readFile("syntax/for_error_missing_variable.nf");
        List<com.gitee.huanminabc.nullchain.language.token.Token> tokens = NfToken.tokens(file);
        System.out.println("Tokens:");
        for (var token : tokens) {
            System.out.println("  " + token.type + " : " + token.value);
        }
    }
}
