package com.gitee.huanminabc.nullchain.utils;

import java.util.Base64;
import java.util.Base64.Encoder;

/**
 * <p> Base64编码解码。</p>
 *
 * @author zxc
 * 2022/2/17
 */
public class Base64Util {

    /**
     * 使用base64编码字符串
     *
     * @param message {@link String}待编码的字符串
     * @return {@link String}编码后字符串
     */
    public static String encode(String message) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(message.getBytes());
    }

    /**
     * 解码base64字符串
     *
     * @param encodeString {@link String}编码之后的字符串
     * @return {@link String}解码后的字符串
     */
    public static String decode(String encodeString) {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decode = decoder.decode(encodeString);
        return new String(decode);
    }


    /**
     * 使用base64编码
     *
     * @param message {@link byte[]}待编码的字符串
     * @return {@link byte[]}编码后字符串
     */
    public static byte[] encode(byte[] message) {
        Encoder encoder = Base64.getEncoder();
        return encoder.encode(message);
    }

    /**
     * 解码base64
     * @param encodeString {@link byte[]}编码之后的字符串
     * @return {@link byte[]}解码后的字符串
     */
    public static byte[] decode(byte[] encodeString) {
        Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(encodeString);
    }
    //将二进制数据编码为BASE64字符串
    public static String encodeToString(byte[] binaryData) {
        return Base64.getEncoder().encodeToString(binaryData);
    }
    //将BASE64字符串恢复为二进制数据
    public static byte[] decodeFromString(String encoded) {
        return Base64.getDecoder().decode(encoded);
    }
}