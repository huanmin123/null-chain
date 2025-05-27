package com.gitee.huanminabc.nullchain.leaf.date;

import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;

/**
 * 简要描述
 *
 * @Author: huanmin
 * @Date: 2025/5/28 00:25
 * @Version: 1.0
 * @Description: 文件作用详细描述....
 */
public class NullDateBase<T> extends NullChainBase<T> implements  NullDate<T>  {

    public NullDateBase(StringBuilder linkLog, boolean isNull, NullCollect collect, NullTaskList taskList) {
        super(linkLog, isNull, collect, taskList);
    }

    public NullDateBase(T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect, taskList);
    }
    public NullDateBase( boolean isNull,T object, StringBuilder linkLog, NullCollect collect, NullTaskList taskList) {
        super(object, linkLog, collect, taskList);
        this.isNull = isNull;
    }

    //将时间类型(Date,LocalDate,LocalDateTime), 10或13位时间戳, 转换为指定格式的时间字符串
    @Override
    public NullDate<String> dateFormat(DateFormatEnum dateFormatEnum) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            String string;
            try {
                string = NullDateFormat.toString(value, dateFormatEnum);
            } catch (Exception e) {
                linkLog.append("dateFormat? ").append(value).append(" to ").append(dateFormatEnum.getValue()).append(" 失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (string == null) {
                linkLog.append("dateFormat? 转换时间格式失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("dateFormat->");
            return NullBuild.noEmpty(string, linkLog, collect,taskList);
        });
        return  NullBuild.busyDate(this);
    }

    @Override
    public NullDate<T> dateOffset(DateOffsetEnum offsetEnum, int num, TimeEnum timeEnum) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }
            T t;
            try {
                t = NullDateFormat.dateOffset((T)value, offsetEnum, num, timeEnum);
            } catch (Exception e) {
                linkLog.append("dateOffset? ").append(value).append(" 偏移时间失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (t == null) {
                linkLog.append("dateOffset? 偏移时间失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("dateOffset->");
            return NullBuild.noEmpty(t, linkLog, collect,taskList);
        });
        return  NullBuild.busyDate(this);
    }

    @Override
    public NullDate<T> dateOffset(DateOffsetEnum controlEnum, TimeEnum timeEnum) {
        return dateOffset(controlEnum, 0, timeEnum);
    }

    @Override
    public NullDate<Integer> dateCompare(Object date) {
        this.taskList.add((value)->{
            if (isNull) {
                return NullBuild.empty(linkLog, collect, taskList);
            }

            Integer compare;
            try {
                compare = NullDateFormat.dateCompare(value, date);
            } catch (Exception e) {
                linkLog.append("dateCompare? ").append(value).append(" 比较时间失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (compare == null) {
                linkLog.append("dateCompare? 比较时间失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append("dateCompare->");
            return NullBuild.noEmpty(compare, linkLog, collect, taskList);
        });
        return  NullBuild.busyDate(this);
    }

}
