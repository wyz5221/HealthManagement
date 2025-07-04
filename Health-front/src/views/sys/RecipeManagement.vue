<template>
  <div class="recipe-management">
    <!-- 搜索区域 -->
    <div class="search-container">
      <div class="search-header">
        <div class="search-title">
          <i class="el-icon-notebook-2" />
          <span>食谱管理</span>
        </div>
      </div>
      <div class="search-content">
        <el-select
          v-model="searchModel.mealType"
          placeholder="请选择餐点类型"
          clearable
          class="search-select"
        >
          <el-option
            v-for="type in mealTypes"
            :key="type"
            :label="type"
            :value="type"
          />
        </el-select>
        <el-input
          v-model="searchModel.name"
          placeholder="请输入食谱名称"
          prefix-icon="el-icon-search"
          clearable
          class="search-input"
        />
        <el-button
          type="primary"
          icon="el-icon-search"
          class="search-btn"
          @click="getRecipeList"
        >查询</el-button>
        <el-button
          type="primary"
          icon="el-icon-plus"
          class="add-btn"
          @click="openEditUi(null)"
        >新增食谱</el-button>
        <el-button
          type="success"
          icon="el-icon-download"
          class="export-btn"
          @click="handleExport"
        >导出Excel</el-button>
        <el-upload
          action=""
          :show-file-list="false"
          :before-upload="beforeUpload"
          :http-request="importRecipes"
          accept=".xlsx,.xls"
          class="import-upload"
        >
          <el-button
            type="primary"
            icon="el-icon-upload2"
            class="import-btn"
          >导入Excel</el-button>
        </el-upload>
      </div>
    </div>

    <!-- 食谱列表 -->
    <div class="table-container">
      <el-table
        :data="recipeList"
        stripe
        border
        highlight-current-row
        class="recipe-table"
      >
        <el-table-column
          prop="id"
          label="ID"
          width="70"
          align="center"
        />
        <el-table-column
          prop="name"
          label="食谱名称"
          min-width="150"
        />
        <el-table-column
          prop="imageUrl"
          label="食谱图片"
          min-width="100"
          align="center"
        >
          <template slot-scope="scope">
            <el-image
              v-if="scope.row.imageUrl"
              style="width: 100px; height: 100px; object-fit: cover"
              :src="scope.row.imageUrl"
              :preview-src-list="[scope.row.imageUrl]"
              fit="cover"
            />
            <span v-else>暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="mealType"
          label="餐点类型"
          width="100"
          align="center"
        >
          <template slot-scope="scope">
            <el-tag :type="getMealTypeTag(scope.row.mealType)">
              {{ scope.row.mealType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="ingredients"
          label="主要食材"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="steps"
          label="制作步骤"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          label="操作"
          width="180"
          align="center"
          fixed="right"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              icon="el-icon-edit"
              size="small"
              class="edit-btn"
              @click="openEditUi(scope.row.id)"
            >编辑</el-button>
            <el-button
              type="danger"
              icon="el-icon-delete"
              size="small"
              class="delete-btn"
              @click="deleteRecipe(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="searchModel.pageNo"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="searchModel.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 食谱编辑对话框 -->
    <el-dialog
      :title="title"
      :visible.sync="dialogFormVisible"
      width="700px"
      :close-on-click-modal="false"
      class="recipe-dialog"
      @close="clearForm"
    >
      <div class="dialog-content">
        <el-form ref="recipeFormRef" :model="recipeForm" :rules="rules" label-width="100px">
          <el-form-item label="食谱名称" prop="name">
            <el-input v-model="recipeForm.name" placeholder="请输入食谱名称" />
          </el-form-item>

          <el-form-item label="餐点类型" prop="mealType">
            <el-select v-model="recipeForm.mealType" placeholder="请选择餐点类型" style="width: 100%">
              <el-option label="早餐" value="早餐" />
              <el-option label="午餐" value="午餐" />
              <el-option label="晚餐" value="晚餐" />
            </el-select>
          </el-form-item>

          <el-form-item label="食谱图片" prop="imageUrl">
            <el-upload
              class="avatar-uploader"
              action="https://localhost:8080/upload/image"
              :show-file-list="false"
              :on-change="handleImageChange"
              :auto-upload="false"
              accept="image/*"
            >
              <img v-if="recipeForm.imageUrl" :src="recipeForm.imageUrl" class="recipe-image">
              <i v-else class="el-icon-plus avatar-uploader-icon" />
            </el-upload>
          </el-form-item>

          <el-form-item label="食材清单" prop="ingredients">
            <el-input
              v-model="recipeForm.ingredients"
              type="textarea"
              placeholder="请输入食材清单，用逗号分隔"
              rows="3"
            />
          </el-form-item>

          <el-form-item label="制作步骤" prop="steps">
            <el-input
              v-model="recipeForm.steps"
              type="textarea"
              placeholder="请输入制作步骤，每一步骤换行"
              rows="5"
            />
          </el-form-item>

          <el-form-item label="营养信息">
            <el-input
              v-model="recipeForm.nutritionInfo"
              type="textarea"
              placeholder="请输入营养信息"
              rows="3"
            />
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button class="cancel-btn" @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" class="confirm-btn" @click="saveRecipe">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import recipeApi from '@/api/recipe'

export default {
  name: 'RecipeManagement',
  data() {
    return {
      mealTypes: ['早餐', '午餐', '晚餐'],
      recipeForm: {},
      recipeList: [],
      dialogFormVisible: false,
      title: '',
      total: 0,
      searchModel: {
        pageNo: 1,
        pageSize: 10,
        mealType: '',
        name: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入食谱名称', trigger: 'blur' }
        ],
        mealType: [
          { required: true, message: '请选择餐点类型', trigger: 'change' }
        ],
        ingredients: [
          { required: true, message: '请输入食材清单', trigger: 'blur' }
        ],
        steps: [
          { required: true, message: '请输入制作步骤', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getRecipeList()
  },
  methods: {
    getMealTypeTag(mealType) {
      const typeMap = {
        '早餐': 'success',
        '午餐': 'warning',
        '晚餐': 'danger'
      }
      return typeMap[mealType] || ''
    },

    getRecipeList() {
      recipeApi.getRecipeList(this.searchModel).then(response => {
        this.recipeList = response.data.rows
        this.total = response.data.total
      })
    },

    handleSizeChange(pageSize) {
      this.searchModel.pageSize = pageSize
      this.getRecipeList()
    },

    handleCurrentChange(pageNo) {
      this.searchModel.pageNo = pageNo
      this.getRecipeList()
    },

    openEditUi(id) {
      if (id == null) {
        this.title = '新增食谱'
        this.recipeForm = {
          mealType: '早餐'
        }
      } else {
        this.title = '修改食谱'
        recipeApi.getRecipeById(id).then(response => {
          this.recipeForm = response.data
        })
      }
      this.dialogFormVisible = true
    },

    saveRecipe() {
      this.$refs.recipeFormRef.validate(valid => {
        if (valid) {
          recipeApi.saveRecipe(this.recipeForm).then(response => {
            this.$message({
              message: response.message,
              type: 'success'
            })
            this.dialogFormVisible = false
            this.getRecipeList()
          })
        } else {
          console.log('error submit!!')
          return false
        }
      })
    },

    deleteRecipe(recipe) {
      this.$confirm(`确认删除 ${recipe.name} 这个食谱吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        recipeApi.deleteRecipeById(recipe.id).then(response => {
          this.$message({
            type: 'success',
            message: response.message
          })
          this.getRecipeList()
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },

    clearForm() {
      this.recipeForm = {}
      this.$refs.recipeFormRef.clearValidate()
    },

    handleImageChange(file) {
      const isImage = file.raw.type.startsWith('image/')
      const isLt2M = file.size / 1024 / 1024 < 2

      if (!isImage) {
        this.$message.error('只能上传图片文件')
        return false
      }

      if (!isLt2M) {
        this.$message.error('图片大小不能超过2MB')
        return false
      }

      // 创建FormData对象
      const formData = new FormData()
      formData.append('file', file.raw)

      // 显示上传进度
      this.uploadLoading = true

      // 使用正确的axios实例
      return this.$axios.post('/upload/image', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }).then(response => {
        const imageUrl = response.data.data || response.data.message
        if (!imageUrl) {
          throw new Error('未获取到图片URL')
        }

        // 确保URL格式正确
        let fullUrl = imageUrl
        if (!imageUrl.startsWith('http')) {
          fullUrl = `${process.env.VUE_APP_BASE_API}${imageUrl.startsWith('/') ? '' : '/'}${imageUrl}`
        }

        this.recipeForm.imageUrl = fullUrl
        return fullUrl
      }).catch(error => {
        this.$message.error('图片上传失败: ' + error.message)
        throw error
      })
    },

    handleExport() {
      window.location.href = `${process.env.VUE_APP_BASE_API}/recipe/export?mealType=${this.searchModel.mealType || ''}`
    },

    //
    // handleExport() {
    //   // 使用window.location.href直接触发下载
    //   window.location.href = `${process.env.VUE_APP_BASE_API}/recipe/export?mealType=${this.searchModel.mealType || ''}&name=${this.searchModel.name || ''}`;
    //
    //   // 或者使用axios方式（推荐）
    //   recipeApi.exportRecipe(this.searchModel).then(response => {
    //     // 创建Blob对象
    //     const blob = new Blob([response.data], { type: 'application/vnd.ms-excel' });
    //
    //     // 创建下载链接
    //     const downloadElement = document.createElement('a');
    //     const href = window.URL.createObjectURL(blob);
    //     downloadElement.href = href;
    //     downloadElement.download = `食谱数据_${new Date().getTime()}.xlsx`;
    //     document.body.appendChild(downloadElement);
    //
    //     // 触发点击
    //     downloadElement.click();
    //
    //     // 释放资源
    //     document.body.removeChild(downloadElement);
    //     window.URL.revokeObjectURL(href);
    //
    //     this.$message.success('导出成功');
    //   }).catch(error => {
    //     this.$message.error('导出失败: ' + error.message);
    //   });
    // },

    // 导入Excel前检查
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' ||
        file.type === 'application/vnd.ms-excel'
      const isLt5M = file.size / 1024 / 1024 < 5

      if (!isExcel) {
        this.$message.error('只能上传Excel文件')
      }
      if (!isLt5M) {
        this.$message.error('文件大小不能超过5MB')
      }
      return isExcel && isLt5M
    },

    // 导入Excel
    importRecipes({ file }) {
      const formData = new FormData()
      formData.append('file', file)

      this.$confirm('确定导入Excel文件吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        recipeApi.importRecipes(formData).then(response => {
          this.$message({
            message: response.message,
            type: 'success'
          })
          this.getRecipeList()
        }).catch(error => {
          this.$message.error('导入失败: ' + error.message)
        })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消导入'
        })
      })
    }
  }
}
</script>

<style scoped>
.recipe-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.search-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.search-header {
  margin-bottom: 20px;
}

.search-title {
  border-bottom: 2px solid #67C23A;
  padding-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: inline-flex;
  align-items: center;
}

.search-title i {
  color: #67C23A;
  margin-right: 8px;
}

.search-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.search-select {
  width: 150px;
  margin-right: 15px;
  margin-bottom: 10px;
}

.search-input {
  width: 200px;
  margin-right: 15px;
  margin-bottom: 10px;
}

.search-btn, .add-btn {
  margin-right: 15px;
  margin-bottom: 10px;
}

.table-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.recipe-table {
  margin-bottom: 20px;
}

.recipe-table >>> .el-table__header-wrapper th {
  background-color: #f0f9eb !important;
  color: #67C23A;
  font-weight: 600;
}

.pagination-container {
  display: flex;
  justify-content: center;
}

.recipe-dialog >>> .el-dialog__header {
  background-color: #67C23A;
  padding: 15px 20px;
  border-radius: 8px 8px 0 0;
}

.recipe-dialog >>> .el-dialog__title {
  color: #ffffff;
}

.recipe-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: #ffffff;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #67C23A;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  line-height: 148px;
  text-align: center;
}

.recipe-image {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}

.export-btn, .import-btn {
  margin-right: 15px;
  margin-bottom: 10px;
}

.import-upload {
  display: inline-block;
}

@media (max-width: 768px) {
  .search-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .search-select,
  .search-input,
  .search-btn,
  .export-btn,
  .import-btn,
  .add-btn {
    width: 100%;
    margin-right: 0;
  }
}
</style>
