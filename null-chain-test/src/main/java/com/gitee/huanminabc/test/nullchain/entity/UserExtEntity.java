package com.gitee.huanminabc.test.nullchain.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.gitee.huanminabc.nullchain.NullExt;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserExtEntity  implements Serializable, NullExt<UserExtEntity> {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name; //名称
    private String pass; //密码
    private Integer age; //年龄

    private String sex;//性别
    private String site; //地址
    private Boolean del; //是否删除
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date date; //日期
    private RoleEntity roleData;

    public void sayHello(String name) {
        System.out.println("Hello, " + name);
    }

    public void sayGoodbye(String name) {
        System.out.println("Goodbye, " + name);
    }
}

