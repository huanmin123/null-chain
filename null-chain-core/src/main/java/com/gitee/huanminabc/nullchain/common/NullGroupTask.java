package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.task.NullTask;
import lombok.Data;

/**
 * @program: java-huanmin-utils
 * @description: 组合任务参数
 * @author: huanmin
 * @create: 2025-02-10 10:25
 **/
@Data
public class NullGroupTask {
    NullTaskInfo[] list;

    public NullGroupTask(NullTaskInfo... list) {
        this.list = list;
    }

    @Data
    public static class NullTaskInfo {
        Object[] params;
        Class<? extends NullTask<?, ?>> taskClass;
        String taskClassName;

        //获取任务名称
        public String getTaskName() {
            if (taskClass != null) {
                return taskClass.getName();
            }
            return taskClassName;
        }

        //获取参数
        public Object[] getParams() {
            return params == null ? new Object[]{} : params;
        }
    }

    public static NullGroupTask buildGroup(NullTaskInfo... taskInfos) {
        if (taskInfos == null || taskInfos.length == 0) {
            throw new NullChainException("NullGroupTask::buildGroup-> 任务为空");
        }
        //校验是否有同类型的任务
        for (int i = 0; i < taskInfos.length; i++) {
            for (int j = i + 1; j < taskInfos.length; j++) {
                if (taskInfos[i].equals(taskInfos[j])) {
                    throw new NullChainException("NullGroupTask::buildGroup-> {}任务重复",taskInfos[j].getTaskName());
                }
            }
        }
        return new NullGroupTask(taskInfos);
    }

    public static NullTaskInfo task(Class<? extends NullTask<?, ?>> taskClass, Object... params) {
        NullTaskInfo info = new NullTaskInfo();
        info.setTaskClass(taskClass);
        info.setParams(params);
        return info;
    }

    public static NullTaskInfo task(String taskClassName, Object... params) {
        try {
            Class<?> aClass1 = Class.forName(taskClassName);
            //判断是否是NULLTask的子类
            if (!NullTask.class.isAssignableFrom(aClass1)) {
                throw new NullChainException("NullGroupTask::task-> {}不是NULLTask的子类",taskClassName);
            }
            NullTaskInfo info = new NullTaskInfo();
            info.setTaskClassName(taskClassName);
            info.setParams(params);
            return info;
        } catch (ClassNotFoundException e) {
            throw new NullChainException("NullGroupTask::task-> 找不到类:{}", taskClassName);
        }

    }

}
