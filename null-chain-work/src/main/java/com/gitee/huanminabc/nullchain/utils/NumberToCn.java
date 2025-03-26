package com.gitee.huanminabc.nullchain.utils;

/**
 * 数字转中文
 */
public class NumberToCn {
    //num 表示数字对应的中文
    private static final String[] num_lower = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    //unit 表示单位对应的中文
    private static final String[] unit_lower = {"", "十", "百", "千"};
    private static final String[] unit_common = {"", "万", "亿", "兆", "京", "垓", "秭", "穰", "沟", "涧", "正", "载"};

    public static String toChineseLower(int num) {
        return format(num, num_lower, unit_lower);
    }

    private static String format(int num, String[] numArray, String[] unit) {
        String intnum = String.valueOf(num);
        //格式化整数部分
        String result = formatIntPart(intnum, numArray, unit);

        return result;
    }

    private static String formatIntPart(String num, String[] numArray, String[] unit) {

        //按4位分割成不同的组（不足四位的前面补0）
        Integer[] intnums = splitIntArray(num);

        boolean zero = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < intnums.length; i++) {
            //格式化当前4位
            String r = formatInt(intnums[i], numArray, unit);
            if ("".equals(r)) {//
                if ((i + 1) == intnums.length) {
                    sb.append(numArray[0]);//结果中追加“零”
                } else {
                    zero = true;
                }
            } else {//当前4位格式化结果不为空（即不为0）
                if (zero || (i > 0 && intnums[i] < 1000)) {//如果前4位为0，当前4位不为0
                    sb.append(numArray[0]);//结果中追加“零”
                }
                sb.append(r);
                sb.append(unit_common[intnums.length - 1 - i]);//在结果中添加权值
                zero = false;
            }
        }
        return sb.toString();
    }

    private static Integer[] splitIntArray(String num) {
        String prev = num.substring(0, num.length() % 4);
        String stuff = num.substring(num.length() % 4);
        if (!"".equals(prev)) {
            num = String.format("%04d", Integer.valueOf(prev)) + stuff;
        }
        Integer[] ints = new Integer[num.length() / 4];
        int idx = 0;
        for (int i = 0; i < num.length(); i += 4) {
            String n = num.substring(i, i + 4);
            ints[idx++] = Integer.valueOf(n);
        }
        return ints;
    }

    private static String formatInt(int num, String[] numArray, String[] unit) {
        char[] val = String.valueOf(num).toCharArray();
        int len = val.length;
        StringBuilder sb = new StringBuilder();
        boolean isZero = false;
        for (int i = 0; i < len; i++) {
            //获取当前位的数值
            int n = Integer.parseInt(val[i] + "");
            if (n == 0) {
                isZero = true;
            } else {
                if (isZero) {
                    sb.append(numArray[Integer.parseInt(val[i - 1] + "")]);
                }
                sb.append(numArray[n]);
                sb.append(unit[(len - 1) - i]);
                isZero = false;
            }
        }
        return sb.toString();
    }
}
