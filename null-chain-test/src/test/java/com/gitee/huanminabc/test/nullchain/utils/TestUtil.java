package com.gitee.huanminabc.test.nullchain.utils;    //读取文件


import com.gitee.huanminabc.jcommon.test.PathUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public  class TestUtil {
    public static String readFile(String fileName) {
        String resourceName = "nf/" + fileName;
        try (InputStream inputStream = TestUtil.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream != null) {
                byte[] bytes = readAllBytes(inputStream);
                return new String(bytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read classpath resource: " + resourceName, e);
        }

        String file = PathUtil.getCurrentProjectTestResourcesAbsolutePath(resourceName);
        try {
            return new String(Files.readAllBytes(new File(file).toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file: " + file, e);
        }
    }

    public static String resourcePath(String resourceName) {
        URL resource = TestUtil.class.getClassLoader().getResource(resourceName);
        if (resource != null && "file".equalsIgnoreCase(resource.getProtocol())) {
            try {
                return Paths.get(resource.toURI()).toString();
            } catch (Exception e) {
                throw new RuntimeException("Failed to resolve resource path: " + resourceName, e);
            }
        }
        return PathUtil.getCurrentProjectTestResourcesAbsolutePath(resourceName);
    }

    private static byte[] readAllBytes(InputStream inputStream) throws Exception {
        byte[] buffer = new byte[4096];
        int length;
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }
}
