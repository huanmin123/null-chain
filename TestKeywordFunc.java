import com.gitee.huanminabc.nullchain.language.*;
import java.util.HashMap;

public class TestKeywordFunc {
    public static void main(String[] args) {
        String script = "fun if(int x)Integer {\n    return x\n}\nInteger result = if(10)\nexport result";
        try {
            Object result = NfMain.run(script, null, new HashMap<>());
            System.out.println("Result: " + result);
        } catch (NfSyntaxException e) {
            System.out.println("Syntax Exception:");
            System.out.println("  Message: " + e.getMessage());
        } catch (NfException e) {
            System.out.println("Exception:");
            System.out.println("  Message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Other Exception:");
            e.printStackTrace();
        }
    }
}
