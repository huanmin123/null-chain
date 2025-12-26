package com.gitee.huanminabc.nullchain.leaf.date;

import com.gitee.huanminabc.nullchain.common.*;
import com.gitee.huanminabc.nullchain.core.NullChainBase;
import static com.gitee.huanminabc.nullchain.common.NullLog.*;
import com.gitee.huanminabc.nullchain.enums.DateFormatEnum;
import com.gitee.huanminabc.nullchain.enums.DateOffsetEnum;
import com.gitee.huanminabc.nullchain.enums.TimeEnum;

/**
 * Null日期操作基础实现类
 * 
 * @param <T> 日期值的类型
 * @author huanmin
 * @since 1.0.0
 * @version 1.1.1
 */
public class NullDateBase<T> extends NullChainBase<T> implements  NullDate<T>  {

    public NullDateBase(StringBuilder linkLog, NullTaskList taskList) {
        super(linkLog, taskList);
    }


    //将时间类型(Date,LocalDate,LocalDateTime), 10或13位时间戳, 转换为指定格式的时间字符串
    @Override
    public NullDate<String> dateFormat(DateFormatEnum dateFormatEnum) {
        this.taskList.add((value)->{
            String string;
            try {
                string = NullDateFormat.toString(value, dateFormatEnum);
            } catch (Exception e) {
                linkLog.append(DATE_FORMAT_Q).append(value).append(" to ").append(dateFormatEnum.getValue()).append(" 失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (string == null) {
                linkLog.append(DATE_FORMAT_Q).append("转换时间格式失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_FORMAT_ARROW);
            return NullBuild.noEmpty(string);
        });
        return  NullBuild.busyDate(this);
    }

    @Override
    public NullDate<T> dateOffset(DateOffsetEnum offsetEnum, int num, TimeEnum timeEnum) {
        this.taskList.add((value)->{
            T t;
            try {
                t = NullDateFormat.dateOffset((T)value, offsetEnum, num, timeEnum);
            } catch (Exception e) {
                linkLog.append(DATE_OFFSET_Q).append(value).append(" 偏移时间失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (t == null) {
                linkLog.append(DATE_OFFSET_Q).append("偏移时间失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_OFFSET_ARROW);
            return NullBuild.noEmpty(t);
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
            Integer compare;
            try {
                compare = NullDateFormat.dateCompare(value, date);
            } catch (Exception e) {
                linkLog.append(DATE_COMPARE_Q).append(value).append(" 比较时间失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (compare == null) {
                linkLog.append(DATE_COMPARE_Q).append("比较时间失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_COMPARE_ARROW);
            return NullBuild.noEmpty(compare);
        });
        return  NullBuild.busyDate(this);
    }

    @Override
    public NullDate<Long> dateBetween(Object date, TimeEnum timeEnum) {
        this.taskList.add((value)->{
            Long between;
            try {
                between = NullDateFormat.dateBetween(value, date, timeEnum);
            } catch (Exception e) {
                linkLog.append(DATE_BETWEEN_Q).append(value).append(" 计算日期间隔失败:").append(e.getMessage());
                throw NullReflectionKit.addRunErrorMessage(e, linkLog);
            }
            if (between == null) {
                linkLog.append(DATE_BETWEEN_Q).append("计算日期间隔失败数据格式不正确");
                throw new NullChainException(linkLog.toString());
            }
            linkLog.append(DATE_BETWEEN_ARROW);
            return NullBuild.noEmpty(between);
        });
        return  NullBuild.busyDate(this);
    }

}
