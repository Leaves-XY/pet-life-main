package com.yxy.pet.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YeXingyi
 * @version 1.0
 * @date 2024/3/4 23:57
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("translated_img")
public class TranslatedImg implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String userOpenId;

    private String url;

    private String title;

    private String source;

    /**
     * 0-正常 1-删除
     */
    @TableLogic(value = "0", delval = "1")
    private String isDeleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
