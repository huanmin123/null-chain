public class TestKeyword {
    public static void main(String[] args) {
        String[] keywords = {"if", "else", "while", "for", "fun", "return", "global"};
        for (String kw : keywords) {
            System.out.println("Testing: " + kw);
        }
    }
}
java TestKeyword.java
