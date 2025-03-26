package com.gitee.huanminabc.nullchain.register;

import com.gitee.huanminabc.nullchain.task.NullTaskFactory;
import com.gitee.huanminabc.nullchain.task.TestTask;

/**
 * @author huanmin
 * @date 2024/11/22
 */
public class RegisterTask {
    static {
        NullTaskFactory.registerTask(TestTask.class);
    }
}
