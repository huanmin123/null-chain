package com.gitee.huanminabc.nullchain.utils;

import com.gitee.huanminabc.nullchain.common.NullChainException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 功能说明：哈希工具类 (SHA-1，SHA-224，SHA-256，SHA-384，SHA-512,MD5 )
 * 说明： 一般用于将用户的信息进行hash生成toke,或者进行密码加密后存储数据库
 * 用法:  到测试类有演示
 * @date 2020年6月29日 下午2:28:24
 * @version 0.1
 */
public class HashUtil {

    private static final java.security.SecureRandom random = new java.security.SecureRandom();
    private static final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
    private static final char[] CHAR_ARRAY = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * 功能说明：使用MD5算法对字符串参数进行哈希处理
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:40:32
     * @param srcStr 原字符串
     * @return 返回md5哈希处理后的字符串
     */
    public static String md5(String srcStr){
        return hash("MD5", srcStr);
    }

    /**
     * 功能说明：使用sha1算法对字符串参数进行哈希处理
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:49:43
     * @param srcStr 原字符串
     * @return 返回sha1哈希处理后的字符串
     */
    public static String sha1(String srcStr){
        return hash("SHA-1", srcStr);
    }

    /**
     * 功能说明：使用sha256算法对字符串参数进行哈希处理
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:55:43
     * @param srcStr 原字符串
     * @return 返回sha256哈希处理后的字符串
     */
    public static String sha256(String srcStr){
        return hash("SHA-256", srcStr);
    }
    /**
     * 功能说明：使用SHA-224算法对字符串参数进行哈希处理
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:55:43
     * @param srcStr 原字符串
     * @return 返回sha256哈希处理后的字符串
     */
    public static String sha224(String srcStr){
        return hash("SHA-224", srcStr);
    }

    /**
     * 功能说明：使用sha384算法对字符串参数进行哈希处理
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:57:23
     * @param srcStr 原字符串
     * @return 返回sha384哈希处理后的字符串
     */
    public static String sha384(String srcStr){
        return hash("SHA-384", srcStr);
    }

    /**
     * 功能说明：使用sha512算法对字符串参数进行哈希处理
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:58:22
     * @param srcStr 原字符串
     * @return 返回sha512哈希处理后的字符串
     */
    public static String sha512(String srcStr){
        return hash("SHA-512", srcStr);
    }

    /**
     * 功能说明：对原字符串使用指定加密算法进行哈希处理
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:39:24
     * @param algorithm 加密算法
     * @param srcStr 原字符串
     * @return 返回哈希处理后的字符串
     */
    public static String hash(String algorithm, String srcStr) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest(srcStr.getBytes(StandardCharsets.UTF_8));
            return toHex(bytes);
        }
        catch (Exception e) {
            throw new NullChainException(e);
        }
    }

    /**
     * 功能说明：把字节数组转为16进制字符串
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:34:48
     * @param bytes
     * @return
     */
    private static String toHex(byte[] bytes) {
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i=0; i<bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    /**
     * 功能说明：生成盐值
     * md5 128bit 16bytes
     * sha1 160bit 20bytes
     * sha256 256bit 32bytes
     * sha384 384bit 48bytes
     * sha512 512bit 64bytes
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:35:27
     * @param saltLength 盐值长度
     * @return 返回生成的盐值
     */
    public static String generateSalt(int saltLength) {
        StringBuilder salt = new StringBuilder();
        for (int i=0; i<saltLength; i++) {
            salt.append(CHAR_ARRAY[random.nextInt(CHAR_ARRAY.length)]);
        }
        return salt.toString();
    }

    /**
     * 功能说明：为Sha256加密算法生成32位长度的盐值
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:36:37
     * @return
     */
    public static String generateSaltForSha256() {
        return generateSalt(32);
    }

    /**
     * 功能说明：为Sha512加密算法生成64位长度的盐值
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:37:30
     * @return
     */
    public static String generateSaltForSha512() {
        return generateSalt(64);
    }

    /**
     * 功能说明：比较2个字节数组是否相等
     * 修改说明：
     * @author zheng
     * @date 2020年6月29日 下午2:38:13
     * @param a 待比较字节数组
     * @param b 待比较字节数组
     * @return 相等返回true，否则返回false
     */
    public static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return false;
        }

        int diff = a.length ^ b.length;
        for(int i=0; i<a.length && i<b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
