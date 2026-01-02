public class TestRegex {
    public static void main(String[] args) {
        String[] tests = {
            "value instanceof Integer",
            "valueinstanceof Integer",
            "value instanceofInteger",
            "valueinstanceofInteger"
        };
        
        for (String test : tests) {
            String result = test.replaceAll(
                "(\w+)\s*instanceof\s*(Integer|Double|Boolean|String|Long|Float|Short|Byte|Character|Object)",
                "$1.class.name == 'java.lang.$2'"
            );
            System.out.println("Input:  " + test);
            System.out.println("Output: " + result);
            System.out.println();
        }
    }
}
