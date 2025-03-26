package com.gitee.huanminabc.test.nullchain;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson.annotation.JSONField;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.base.async.NullChainAsync;
import com.gitee.huanminabc.nullchain.enums.OkHttpPostEnum;
import com.gitee.huanminabc.utils_common.file.PathUtil;
import com.gitee.huanminabc.utils_common.file.ReadFileBytesUtil;
import com.gitee.huanminabc.utils_common.multithreading.executor.ExecutorTestUtil;
import lombok.Data;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NullHttpTest {
    UserInfo userEntity = new UserInfo();
    @Data
    public  static   class UserInfo{
        private String username;
        private String password;
        private File file;



        private byte[] fileByte;
        private String fileName;


        //        private File[] files;
        @JSONField(name="files") //可以指定别名,否则默认为属性名
        private List<File> fileList;
    }

    @Before
    public void before() {
        userEntity.setUsername("huanmin");
        userEntity.setPassword("12313123");
    }


    @Test
    public void http_get(){
        Map<String,String> from=new HashMap<>();
        from.put("username","admin");
        from.put("password","123456");
        Null.of(from).http("http://127.0.0.1:8798/user/userInfo/get").get().toStr().ifPresent(System.out::println);

        UserInfo userEntity = new UserInfo();
        userEntity.setUsername("huanmin");
        userEntity.setPassword("123456");
        Null.of(userEntity).http("http://127.0.0.1:8798/user/userInfo/get").get().toStr().ifPresent(System.out::println);

        String param = "username=admin&password=123456";
        Null.of(param).http("http://127.0.0.1:8798/user/userInfo/get").get().toStr().ifPresent(System.out::println);

        String param1 = "username=admin";
        Null.of(param1).http("http://127.0.0.1:8798/user/userInfo/get?password=123456").get().toStr().ifPresent(System.out::println);

        Null.of(Void.TYPE).http("http://www.baidu.com").get().toStr().ifPresent(System.out::println);
    }
    @Test
    public void http_get_async(){
        Map<String,Object> from=new HashMap<>();
        from.put("username","admin");
        from.put("password","123");
        NullChainAsync<String> str = Null.of(from).async().http("http://127.0.0.1:8798/user/userInfo/get").get().toStr().then((v)->{
            System.out.println("异步回调:"+v);
        });
        System.out.println("===========================");

        str.ifPresent(System.out::println);

    }
    @Test
    public void http_Post_json(){
        Map<String,Object> from=new HashMap<>();
        from.put("username","admin");
        from.put("password","123456");
        Null.of(from).http("http://127.0.0.1:8798/user/userInfo/post/json").post(OkHttpPostEnum.JSON).toStr().ifPresent(System.out::println);

        UserInfo userEntity = new UserInfo();
        userEntity.setUsername("huanmin");
        userEntity.setPassword("123456");
        Null.of(userEntity).http("http://127.0.0.1:8798/user/userInfo/post/json").post(OkHttpPostEnum.JSON).toStr().ifPresent(System.out::println);
    }

    @Test
    public void http_Post_form(){
        Map<String,Object> from=new HashMap<>();
        from.put("username","admin");
        from.put("password","123456");
        Null.of(from).http("http://127.0.0.1:8798/user/login/post/form").post(OkHttpPostEnum.FORM).toStr().ifPresent(System.out::println);
        Null.of(userEntity).http("http://127.0.0.1:8798/user/login/post/form").post(OkHttpPostEnum.FORM).toStr().ifPresent(System.out::println);
    }



    @Test
    public void http_Post_file_file(){
        String filePatch = PathUtil.getCurrentProjectTestResourcesAbsoluteFile("nf/" + "test.nf");

        Map<String,Object> from=new HashMap<>();
        from.put("file", new File(filePatch));
        Null.of(from).http("http://127.0.0.1:8798/upload/file").post(OkHttpPostEnum.FILE).toStr().ifPresent(System.out::println);

        UserInfo userEntity = new UserInfo();
        userEntity.setFile(new File(filePatch));
        Null.of(userEntity).http("http://127.0.0.1:8798/upload/file").post(OkHttpPostEnum.FILE).toStr().ifPresent(System.out::println);
    }
    @Test
    public void http_Post_file_byte(){
        String filePatch = PathUtil.getCurrentProjectTestResourcesAbsoluteFile("nf/" + "test.nf");
        Map<String,Object> from=new HashMap<>();
        File file = new File(filePatch);
        byte[] bytes = ReadFileBytesUtil.readByte(file);
        from.put("file", bytes);
        from.put("fileName", file.getName());
//        from.put("password","123456");
//        Null.of(from).http("http://127.0.0.1:8798/upload/stream").post(OkHttpPostEnum.FILE).toStr().ifPresent(System.out::println);

        userEntity.setFileByte(bytes);
        userEntity.setFileName(file.getName());
        Null.of(userEntity).http("http://127.0.0.1:8798/upload/stream").post(OkHttpPostEnum.FILE).toStr().ifPresent(System.out::println);
    }

    @Test
    public void http_Post_files(){
        String filePatch = PathUtil.getCurrentProjectTestResourcesAbsoluteFile("nf/" + "test.nf");
        File file = new File(filePatch);
        userEntity.setFileList(Arrays.asList(file));
        Null.of(userEntity).http("http://127.0.0.1:8798/upload/files").post(OkHttpPostEnum.FILE).toStr().ifPresent(System.out::println);
    }

    @Test
    public void http_get_dow(){
       String fileParam="filePath=test.nf";
       Null.of(fileParam).http("http://127.0.0.1:8798/download/file").get().downloadFile("D:/test.nf").ifPresent(System.out::println);
       Null.of(fileParam).http("http://127.0.0.1:8798/download/file").get().toBytes().ifPresent(System.out::println);
       Null.of(fileParam).http("http://127.0.0.1:8798/download/file").get().toInputStream().ifPresent(System.out::println);

    }


    @Test
    public void http_get_config(){

        Map<String,Object> from=new HashMap<>();
        from.put("username","admin");
        from.put("password","123456");
        Null.of(from).http("http://127.0.0.1:8798/user/userInfo/get").
                connectTimeout(30, TimeUnit.SECONDS).
                get().
                toStr().ifPresent(System.out::println);

    }

    @Test
    public void http_get_thread(){

        Map<String,Object> from=new HashMap<>();
        from.put("token","7861f3da9c048478eb8899aa708785c8203532187f47a6fa0459915b50f1baec");
        from.put("id","25288");

        ExecutorTestUtil.createAndWaitAll(100,()->{
            for (int i = 0; i < 100; i++) {
                Null.of(from).http("http://192.168.40.65:3000/api/interface/get").
                        get().
                        toStr().ifPresent(System.out::println);
            }
        });

        //等待20s
        ThreadUtil.sleep(6000);
        ExecutorTestUtil.createAndWaitAll(100,()->{
            for (int i = 0; i < 100; i++) {
                Null.of(from).http("http://192.168.40.65:3000/api/interface/get").
                        get().
                        toStr().ifPresent(System.out::println);
            }



        });
    }

}
