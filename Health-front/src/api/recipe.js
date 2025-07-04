import request from '@/utils/request'

export default {
  getMealTypes() {
    return request({
      url: '/recipe/getMealTypes',
      method: 'get'
    });
  },

  getByMealType(mealType) {
    return request({
      url: '/recipe/getByMealType',
      method: 'get',
      params: { mealType }
    });
  },

  getRecipeList(searchModel) {
    return request({
      url: '/recipe/list',
      method: 'get',
      params: {
        pageNo: searchModel.pageNo,
        pageSize: searchModel.pageSize,
        mealType: searchModel.mealType,
        name: searchModel.name // 确保传递name参数
      }
    });
  },

  saveRecipe(recipe) {
    if (recipe.id == null || recipe.id == undefined) {
      return this.addRecipe(recipe);
    }
    return this.updateRecipe(recipe);
  },

  addRecipe(recipe) {
    return request({
      url: '/recipe/add',
      method: 'post',
      data: recipe
    }).catch(error => {
      console.log(error.response.data);
      throw error;
    });
  },

  updateRecipe(recipe) {
    return request({
      url: '/recipe/update',
      method: 'put',
      data: recipe
    });
  },

  getRecipeById(id) {
    return request({
      url: `/recipe/${id}`,
      method: 'get',
    });
  },

  deleteRecipeById(id) {
    return request({
      url: `/recipe/${id}`,
      method: 'delete'
    });
  },

  exportRecipe(params) {
    return request({
      url: '/recipe/export',
      method: 'get',
      params: params,
      responseType: 'blob' // 重要：指定响应类型为blob
    })
  },

  // 导入Excel
  importRecipes(data) {
    return request({
      url: '/recipe/import',
      method: 'post',
      data: data,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
