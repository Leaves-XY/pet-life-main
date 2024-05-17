package com.yxy.pet.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author YeXingyi
 * @version 1.0
 * @date 2024/3/5 16:28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("prediction_result")
public class PredictionResult implements Serializable {
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;

    @JsonProperty("num_boxes")
    private int numBoxes;

    @JsonProperty("resnet_result")
    private Map<String, ResnetResult> resnetResult;

    @JsonProperty("yolo_time")
    private double yoloTime;

    @JsonProperty("resnet_time")
    private double resnetTime;

    @JsonProperty("wxyy_time")
    private double wxyyTime;

    @JsonProperty("all_time")
    private double allTime;

    @JsonProperty("title")
    private String title;

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
