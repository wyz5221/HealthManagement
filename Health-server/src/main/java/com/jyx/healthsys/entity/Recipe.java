package com.jyx.healthsys.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@TableName("recipe")
@NoArgsConstructor
@AllArgsConstructor
public class Recipe implements Serializable {
    @TableId(type = IdType.AUTO)
    @TableField(value = "id")
    @Excel(name = "ID", width = 10)
    private Integer id;

    @TableField(value = "name")
    @Excel(name = "食谱名称", width = 20)
    private String name;

    @TableField(value = "meal_type")
    @Excel(name = "餐点类型", width = 15)
    private String mealType; // 早餐、午餐、晚餐

    @TableField(value = "ingredients")
    @Excel(name = "食材清单", width = 30)  // 确保name值与Excel列名完全一致
    private String ingredients;

    @TableField(value = "steps")
    @Excel(name = "制作步骤", width = 40)
    private String steps; // 制作步骤，用换行符分隔

    @TableField(value = "nutrition_info")
    @Excel(name = "营养信息", width = 25)
    private String nutritionInfo; // 营养信息

    @TableField(value = "image_url")
    @Excel(name = "图片URL", width = 25)
    private String imageUrl; // 食谱图片
}