import com.gitee.huanminabc.nullchain.language.NfToken;
import com.gitee.huanminabc.nullchain.language.NfSynta;
import com.gitee.huanminabc.nullchain.language.token.Token;
import java.util.List;

public class TestForDebug {
    public static void main(String[] args) {
        String file = "for in 1..10 {\n    Integer a = 1\n}\nexport 0\n";
        System.out.println("File content:");
        System.out.println(file);
        System.out.println("\nParsing...");
        try {
            List<Token> tokens = NfToken.tokens(file);
            System.out.println("Tokens: " + tokens);
            NfSynta.buildMainStatement(tokens);
        } catch (Exception e) {
            System.out.println("Exception type: " + e.getClass().getName());
            System.out.println("Exception message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
