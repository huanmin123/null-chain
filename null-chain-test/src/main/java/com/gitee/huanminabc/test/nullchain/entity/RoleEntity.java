package com.gitee.huanminabc.test.nullchain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date    roleCreationTime;
    private String  roleDescription;
    private boolean roleStatus;
}

