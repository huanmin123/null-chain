package com.gitee.huanminabc.test.nullchain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String  roleName;
    // 防止 JSON parse error: Cannot deserialize value of type `java.util.Date` from String 错误
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date    roleCreationTime;
    private String  roleDescription;
    private boolean roleStatus;
}

