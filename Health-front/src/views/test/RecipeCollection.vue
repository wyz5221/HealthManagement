<template>
  <div class="recipe-container">
    <!-- 食谱分类导航 -->
    <div class="recipe-nav">
      <el-tabs v-model="activeMealType" @tab-click="handleTabClick">
        <el-tab-pane label="早餐食谱" name="早餐"></el-tab-pane>
        <el-tab-pane label="午餐食谱" name="午餐"></el-tab-pane>
        <el-tab-pane label="晚餐食谱" name="晚餐"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 食谱列表 -->
    <div class="recipe-list">
      <el-row :gutter="20">
        <el-col
          :xs="24" :sm="12" :md="8" :lg="6"
          v-for="recipe in recipes"
          :key="recipe.id"
          class="recipe-col"
        >
          <el-card
            :body-style="{ padding: '0px' }"
            shadow="hover"
            class="recipe-card"
            @click.native="showRecipeDetail(recipe)"
          >
            <div class="recipe-image">
              <img :src="recipe.imageUrl || defaultImage" :alt="recipe.name">
            </div>
            <div class="recipe-content">
              <h3 class="recipe-name">{{ recipe.name }}</h3>
              <div class="recipe-meta">
                <el-tag :type="getMealTypeTag(recipe.mealType)" size="small">
                  {{ recipe.mealType }}
                </el-tag>
              </div>
              <div class="recipe-ingredients">
                <h4>主要食材</h4>
                <p>{{ recipe.ingredients }}</p>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 无数据提示 -->
      <div class="empty-tip" v-if="recipes.length === 0">
        <el-empty description="暂无相关食谱"></el-empty>
      </div>
    </div>

    <!-- 食谱详情对话框 -->
    <el-dialog
      :title="currentRecipe ? currentRecipe.name : '食谱详情'"
      :visible.sync="detailDialogVisible"
      width="60%"
      top="5vh"
      class="recipe-detail-dialog"
    >
      <div class="recipe-detail-content" v-if="currentRecipe">
        <el-row :gutter="20">
          <el-col :span="12">
            <div class="detail-image">
              <img :src="currentRecipe.imageUrl || defaultImage" :alt="currentRecipe.name">
            </div>
          </el-col>
          <el-col :span="12">
            <div class="detail-info">
              <div class="detail-meta">
                <el-tag :type="getMealTypeTag(currentRecipe.mealType)">
                  {{ currentRecipe.mealType }}
                </el-tag>
              </div>

              <div class="detail-section">
                <h3>食材清单</h3>
                <div class="detail-text">{{ currentRecipe.ingredients }}</div>
              </div>

              <div class="detail-section">
                <h3>制作步骤</h3>
                <div class="detail-steps">
                  <div
                    v-for="(step, index) in currentRecipe.steps.split('\n')"
                    :key="index"
                    class="step-item"
                  >
                    <span class="step-number">{{ index + 1 }}.</span>
                    <span class="step-content">{{ step }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section" v-if="currentRecipe.nutritionInfo">
                <h3>营养信息</h3>
                <div class="detail-text">{{ currentRecipe.nutritionInfo }}</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关 闭</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import recipeApi from '@/api/recipe'
import defaultImage from '@/assets/default.jpg'

export default {
  name: 'RecipeCollection',
  data() {
    return {
      activeMealType: '早餐',
      recipes: [],
      currentRecipe: null,
      detailDialogVisible: false,
      defaultImage
    }
  },
  created() {
    this.fetchRecipes(this.activeMealType)
  },
  methods: {
    fetchRecipes(mealType) {
      recipeApi.getByMealType(mealType).then(response => {
        this.recipes = response.data.recipes
      })
    },
    handleTabClick(tab) {
      this.fetchRecipes(tab.name)
    },
    showRecipeDetail(recipe) {
      this.currentRecipe = recipe
      this.detailDialogVisible = true
    },
    getMealTypeTag(mealType) {
      const typeMap = {
        '早餐': 'success',
        '午餐': 'warning',
        '晚餐': 'danger'
      }
      return typeMap[mealType] || ''
    }
  }
}
</script>

<style scoped>
.recipe-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.recipe-nav {
  background: #fff;
  padding: 10px 20px;
  margin-bottom: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.recipe-list {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.recipe-col {
  margin-bottom: 20px;
}

.recipe-card {
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.recipe-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.15);
}

.recipe-image {
  height: 160px;
  overflow: hidden;
}

.recipe-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.recipe-card:hover .recipe-image img {
  transform: scale(1.05);
}

.recipe-content {
  padding: 15px;
}

.recipe-name {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recipe-meta {
  margin-bottom: 10px;
}

.recipe-ingredients h4 {
  margin: 10px 0 5px 0;
  font-size: 14px;
  color: #666;
}

.recipe-ingredients p {
  margin: 0;
  font-size: 13px;
  color: #999;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-tip {
  padding: 40px 0;
}

/* 详情对话框样式 */
.recipe-detail-dialog >>> .el-dialog__header {
  border-bottom: 1px solid #eee;
}

.recipe-detail-content {
  padding: 0 10px;
}

.detail-image {
  height: 400px;
  overflow: hidden;
  border-radius: 8px;
}

.detail-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-info {
  padding-left: 20px;
}

.detail-meta {
  margin-bottom: 20px;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #67C23A;
  padding-bottom: 5px;
  border-bottom: 1px dashed #eee;
}

.detail-text {
  line-height: 1.6;
  color: #333;
}

.detail-steps {
  padding-left: 20px;
}

.step-item {
  margin-bottom: 10px;
  display: flex;
}

.step-number {
  font-weight: bold;
  margin-right: 8px;
  color: #67C23A;
}

.step-content {
  flex: 1;
}

@media (max-width: 768px) {
  .detail-image {
    height: 200px;
    margin-bottom: 20px;
  }

  .detail-info {
    padding-left: 0;
  }
}
</style>
