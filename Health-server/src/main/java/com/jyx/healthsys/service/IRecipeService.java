package com.jyx.healthsys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jyx.healthsys.entity.Recipe;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public interface IRecipeService extends IService<Recipe> {
    List<Recipe> getRecipesByMealType(String mealType);
    boolean addRecipe(Recipe recipe);
    void updateRecipe(Recipe recipe);
    Recipe getRecipeById(Integer id);
    void deleteRecipeById(Integer id);
    boolean importRecipes(MultipartFile file);

    void exportRecipeData(HttpServletResponse response) throws IOException;
}