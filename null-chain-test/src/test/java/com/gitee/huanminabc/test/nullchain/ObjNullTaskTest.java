package com.gitee.huanminabc.test.nullchain;

import com.gitee.huanminabc.common.file.ReadFileBytesUtil;
import com.gitee.huanminabc.common.test.PathUtil;
import com.gitee.huanminabc.nullchain.Null;
import com.gitee.huanminabc.nullchain.common.NullGroupNfTask;
import com.gitee.huanminabc.nullchain.common.NullGroupTask;
import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.task.TestTask;
import com.gitee.huanminabc.nullchain.tool.file.BytesToWriteFileTool;
import com.gitee.huanminabc.nullchain.tool.file.ZipToBytesByteTool;
import com.gitee.huanminabc.test.nullchain.task.Test1Task;
import com.gitee.huanminabc.test.nullchain.task.Test2Task;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2024-12-30 14:49
 **/
public class ObjNullTaskTest {



    @Test
    public  void task2()  {
        String a="123131";
        Null.of(a).task(TestTask.class,"123",true).ifPresent(System.out::println);
//        Null.of(a).task(TestTask.class.getName(),"xxxxxx").ifPresent(System.out::println);

    }
    @Test
    public  void task3()  {
        String a="123131";
        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(TestTask.class.getName(),"123213"),
                NullGroupTask.task(Test2Task.class.getName())
        );
        Null.of(a).task(nullGroupTask).ifPresent(System.out::println);
    }

    @Test
    public  void zipTask(){
        String file1 = PathUtil.getCurrentProjectTestResourcesAbsolutePath("nf/test.nf" );
        String file2 = PathUtil.getCurrentProjectTestResourcesAbsolutePath("nf/test1.nf" );
        String file3 = PathUtil.getCurrentProjectTestResourcesAbsolutePath("nf/test2.nf" );
        byte[] bytes = ReadFileBytesUtil.readByte(new File(file1));
        byte[] bytes1 = ReadFileBytesUtil.readByte(new File(file2));
        byte[] bytes2 = ReadFileBytesUtil.readByte(new File(file3));
        Map<String,byte[]> map = new HashMap<>();
        map.put("nfzip/test.nf",bytes);
        map.put("nfzip/test1.nf",bytes1);
        map.put("nfzip/test2.nf",bytes2);
        map.put("nfzipdir/",null);
//        Null.of(map).task(ZipToBytesByteTool.class).type(byte[].class).ifPresent(System.out::println);

        String wFile = PathUtil.getCurrentProjectTestResourcesAbsolutePath("test.zip" );
        Null.of(map).tool(ZipToBytesByteTool.class).tool(BytesToWriteFileTool.class,wFile).ifPresent(System.out::println);
    }


    @Test
    public  void addTaskClass(){

        NullGroupTask nullGroupTask = NullGroupTask.buildGroup(
                NullGroupTask.task(Test1Task.class.getName()),
                NullGroupTask.task(Test2Task.class.getName())
        );

        String preValue="123131";

        Null.of(preValue).task(nullGroupTask).ifPresent(System.out::println);

        Null.of(Arrays.asList("1","2"));
        String classPath ="com.gitee.huanminabc.nullchain.task.Test1Task";
        String currentProjectTargetClassAbsolutePath = PathUtil.getCurrentProjectTargetTestClassAbsolutePath(classPath.replace(".", "/"))+".class";

        //断点打住, 然后修改Test1Task的内容编译新的class文件,然后再次执行
        NullTaskFactory.loadTaskClass(classPath,currentProjectTargetClassAbsolutePath);
        Null.of(preValue).task(nullGroupTask).ifPresent(System.out::println);
    }

    @Test
    public  void nfFileTask(){
        String file = PathUtil.getCurrentProjectTestResourcesAbsolutePath("nf/test.nf" );
        String a="123131";
        Null.of(a).nfTask(NullGroupNfTask.taskFile(file)).type(String.class).ifPresent(System.out::println);
    }


}
