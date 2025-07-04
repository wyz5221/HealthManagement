package com.jyx.healthsys.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jyx.Data_unification.Unification;
import com.jyx.healthsys.entity.Recipe;
import com.jyx.healthsys.service.IRecipeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/recipe")
public class RecipeController {

    @Autowired
    private IRecipeService recipeService;

    // 获取所有食谱类型
    @GetMapping("/getMealTypes")
    public Unification<List<String>> getMealTypes() {
        List<String> types = Arrays.asList("早餐", "午餐", "晚餐");
        return Unification.success(types);
    }

    // 根据餐点类型获取食谱
    @GetMapping("/getByMealType")
    public Unification<Map<String, Object>> getByMealType(@RequestParam String mealType) {
        List<Recipe> recipes = recipeService.getRecipesByMealType(mealType);
        Map<String, Object> data = new HashMap<>();
        data.put("recipes", recipes);
        return Unification.success(data);
    }

    // 分页获取食谱列表
    @GetMapping("/list")
    public Unification<Map<String, Object>> getRecipeList(
            @RequestParam(value = "mealType", required = false) String mealType,
            @RequestParam(value = "name", required = false) String name, // 添加name参数
            @RequestParam("pageNo") Long pageNo,
            @RequestParam("pageSize") Long pageSize) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasLength(mealType), Recipe::getMealType, mealType);
        // 添加食谱名称的模糊查询条件
        wrapper.like(StringUtils.hasLength(name), Recipe::getName, name);
        Page<Recipe> page = new Page<>(pageNo, pageSize);
        recipeService.page(page, wrapper);
        Map<String, Object> data = new HashMap<>();
        data.put("total", page.getTotal());
        data.put("rows", page.getRecords());
        return Unification.success(data);
    }

    // 添加食谱
    @PostMapping("/add")
    public Unification<?> addRecipe(@RequestBody Recipe recipe) {
        boolean isSuccess = recipeService.addRecipe(recipe);
        if (isSuccess) {
            return Unification.success("新增成功");
        } else {
            return Unification.fail("新增失败，食谱名称已存在");
        }
    }

    // 更新食谱
    @PutMapping("/update")
    public Unification<?> updateRecipe(@RequestBody Recipe recipe) {
        recipeService.updateRecipe(recipe);
        return Unification.success("修改成功");
    }

    // 根据ID获取食谱
    @GetMapping("/{id}")
    public Unification<Recipe> getRecipeById(@PathVariable("id") Integer id) {
        Recipe recipe = recipeService.getRecipeById(id);
        return Unification.success(recipe);
    }

    // 根据ID删除食谱
    @DeleteMapping("/{id}")
    public Unification<Recipe> deleteRecipeById(@PathVariable("id") Integer id) {
        recipeService.deleteRecipeById(id);
        return Unification.success("删除成功");
    }



    // 导出食谱数据
    @GetMapping("/export")
    public void exportRecipeData(HttpServletResponse response) {
        try {
            recipeService.exportRecipeData(response);
        } catch (Exception e) {
            log.error("导出失败: ", e);
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap<>();
            map.put("status", "failure");
            map.put("message", "导出失败: " + e.getMessage());
            try {
                response.getWriter().println(JSON.toJSONString(map));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // 导入食谱数据
    @PostMapping("/import")
    public Unification<?> importRecipes(@RequestParam("file") MultipartFile file) {
        try {
            boolean isSuccess = recipeService.importRecipes(file);
            if (isSuccess) {
                return Unification.success("导入成功");
            } else {
                return Unification.fail("导入失败");
            }
        } catch (Exception e) {
            return Unification.fail("导入失败: " + e.getMessage());
        }
    }
}