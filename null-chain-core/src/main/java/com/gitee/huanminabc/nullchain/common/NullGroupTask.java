package com.gitee.huanminabc.nullchain.common;

import com.gitee.huanminabc.nullchain.task.NullTask;
import lombok.Data;

/**
 * Null组合任务类 - 提供组合任务的参数管理功能
 * 
 * <p>该类提供了组合任务的参数管理功能，支持将多个任务组合成一个任务组进行执行。
 * 通过任务组合机制，为复杂的业务流程提供灵活的任务组织能力。</p>
 * 
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>任务组合：将多个任务组合成一个任务组</li>
 *   <li>参数管理：管理任务组的参数信息</li>
 *   <li>任务信息：提供任务信息的管理</li>
 *   <li>批量执行：支持批量任务执行</li>
 * </ul>
 * 
 * <h3>设计特点：</h3>
 * <ul>
 *   <li>任务组合：支持任务组合功能</li>
 *   <li>参数管理：提供统一的参数管理</li>
 *   <li>灵活配置：支持灵活的任务配置</li>
 *   <li>批量处理：支持批量任务处理</li>
 * </ul>
 * 
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 * @see NullTask 任务接口
 * @see NullTaskInfo 任务信息类
 */
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
