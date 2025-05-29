package com.gitee.huanminabc.test.nullchain.entity;


import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String name; //名称
    private String pass; //密码
    private Integer age; //年龄
    private String sex;//性别
    private String site; //地址
    private Boolean del; //是否删除
    private Date date; //日期
    private RoleEntity roleData;
    private int anInt;
    @ToString.Exclude
    @JSONField(serialize = false)
    private List<UserEntity> list=new ArrayList<>();

}

