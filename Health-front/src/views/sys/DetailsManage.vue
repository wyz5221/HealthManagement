<!-- 搜索框和添加按钮，搜索框，以及用户名和手机号的输入框 -->
<template>
  <div class="detail-container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <div class="search-header">
        <div class="search-title">
          <i class="el-icon-document" />
          <span>运动详情查询</span>
        </div>
      </div>
      <div class="search-content">
        <el-input
          v-model="searchModel.sportType"
          placeholder="请输入运动类型"
          prefix-icon="el-icon-bicycle"
          clearable
          class="search-input"
        />
        <el-button
          type="primary"
          icon="el-icon-search"
          class="search-btn"
          @click="getDetailList"
        >查询</el-button>
      </div>
    </div>

    <!-- 运动详情列表区域 -->
    <div class="table-container">
      <div class="table-header">
        <div class="table-title">
          <i class="el-icon-notebook-1" />
          <span>运动详情列表</span>
          <span class="table-count">共 {{ total }} 条记录</span>
        </div>
        <el-button
          type="primary"
          icon="el-icon-plus"
          class="add-btn"
          @click="openEditUi(null)"
        >新增详情</el-button>
      </div>
      <el-table
        :data="detailList"
        stripe
        border
        highlight-current-row
        class="detail-table"
      >

        <el-table-column
          prop="id"
          label="ID"
          width="70"
          align="center"
        />
        <el-table-column
          prop="sportType"
          label="运动类型"
          min-width="120"
          align="center"
        >
          <template slot-scope="scope">
            <div class="sport-type-cell">
              <i class="el-icon-trophy" />
              <span>{{ scope.row.sportType }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="disease"
          label="禁忌疾病"
          min-width="120"
          align="center"
        >
          <template slot-scope="scope">
            <div class="disease-cell">
              <i class="el-icon-warning" />
              <span>{{ scope.row.disease }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="method"
          label="运动方法"
          min-width="180"
          align="center"
        >
          <template slot-scope="scope">
            <div class="method-cell">
              <i class="el-icon-guide" />
              <span>{{ scope.row.method }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="notes"
          label="注意事项"
          min-width="180"
          align="center"
        >
          <template slot-scope="scope">
            <div class="notes-cell">
              <i class="el-icon-info" />
              <span>{{ scope.row.notes }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="180"
          align="center"
          fixed="right"
        >
          <template slot-scope="scope">
            <div class="operation-cell">
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
                @click="deleteDetail(scope.row)"
              >删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页功能 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="searchModel.pageNo"
          :page-sizes="[5, 10, 20, 30]"
          :page-size="searchModel.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 运动详情编辑信息弹出框 -->
    <el-dialog
      :title="title"
      :visible.sync="dialogFormVisible"
      width="600px"
      :close-on-click-modal="false"
      center
      class="detail-dialog"
      @close="clearForm"
    >
      <div class="dialog-content">
        <el-form ref="detailFormRef" :model="detailForm" label-width="100px">
          <el-form-item label="运动类型" prop="sportType">
            <el-input v-model="detailForm.sportType" placeholder="请输入运动类型">
              <i slot="prefix" class="el-icon-trophy" />
            </el-input>
          </el-form-item>
          <el-form-item label="运动图片" prop="imageUrl">
            <el-upload
              class="avatar-uploader"
              action="https://localhost:8080/upload/image"
              :show-file-list="false"
              :on-change="handleImageChange"
              :auto-upload="false"
              accept="image/*"
            >
              <img v-if="detailForm.imageUrl" :src="detailForm.imageUrl" class="sport-image">
              <i v-else class="el-icon-plus avatar-uploader-icon" />
            </el-upload>
          </el-form-item>

          <el-form-item label="禁忌疾病" prop="disease">
            <el-input v-model="detailForm.disease" placeholder="请输入禁忌疾病">
              <i slot="prefix" class="el-icon-warning" />
            </el-input>
          </el-form-item>

          <el-form-item label="运动方法" prop="method">
            <el-input
              v-model="detailForm.method"
              type="textarea"
              rows="3"
              placeholder="请输入运动方法"
            />
          </el-form-item>

          <el-form-item label="注意事项" prop="notes">
            <el-input
              v-model="detailForm.notes"
              type="textarea"
              rows="3"
              placeholder="请输入注意事项"
            />
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button class="cancel-btn" @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" class="confirm-btn" @click="saveDetail">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import sportApi from '@/api/Function_Menu'
export default {
  data() {
    return {
      detailForm: {}, // 初始化为一个空对象
      detailList: [],
      // 左边宽度
      formLabelWidth: '135px',
      // 设置默认值不可见
      dialogFormVisible: false,
      title: '',
      total: 0,
      searchModel: {
        pageNo: 1,
        // 默认显示数量
        pageSize: 10
      },
      // 表单规则配置

      rules: {
        sportType: [
          { required: true, message: '请输入运动类型', trigger: 'blur' }
        ]
      }
    }
  },

  // 加载时就查询一次
  created() {
    this.getDetailList()
  },

  methods: {
    saveDetail() {
      let isOk = true
      // 触发表单的验证
      this.$refs.detailFormRef.validate((valid) => {
        // 这边只有校验失败的时候才会进来,在外面定义一个 isok,校验失败会将他改成 false
        isOk = valid
      })

      if (isOk) {
        // 提交验证给后台
        console.log(this.detailtForm)
        sportApi.saveDetail(this.detailForm).then((response) => {
          // 成功提示
          this.$message({
            message: response.message,
            type: 'success'
          })
          // 关闭对话框
          this.dialogFormVisible = false
          // 刷新表格数据
          this.getDetailList()
        })
      } else {
        console.log('error submit!!')
        return false
      }
    },

    // 清理表单数据
    clearForm() {
      this.detailForm = {
      }
      // 清除表单校验的提示信息
      this.$refs.detailFormRef.clearValidate()
    },
    handleSizeChange(pageSize) {
      // 数据更新
      this.searchModel.pageSize = pageSize
      this.getDetailList()
    },
    handleCurrentChange(pageNo) {
      this.searchModel.pageNo = pageNo
      this.getDetailList()
    },

    // 图片上传处理
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

        this.detailForm.imageUrl = fullUrl
        return fullUrl
      }).catch(error => {
        this.$message.error('图片上传失败: ' + error.message)
        throw error
      })
    },

    // 用于查询用户列表
    getDetailList() {
      sportApi.getDetailList(this.searchModel).then((response) => {
        this.detailList = response.data.rows
        this.total = response.data.total
      })
    },

    openEditUi(id) {
      console.log(id)
      if (id == null) {
        this.title = '新增运动详情'
      } else {
        this.title = '修改运动详情'
        // 根据id查询用户数据
        sportApi.getDetailById(id).then((response) => {
          this.detailForm = response.data
          console.log(this.detailForm)
        })
      }
      this.dialogFormVisible = true
    },

    deleteDetail(detail) {
      this.$confirm(`确认删除 ${detail.sportType} 这个运动详情吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          sportApi.deleteDetailById(detail.id).then((response) => {
            this.$message({
              type: 'success',
              message: response.message
            })
            this.getDetailList()
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    }
  }
}
</script>
<style scoped>
.detail-container {
  padding: 20px;
  min-height: calc(100vh - 120px);
  background-color: #f5f7fa;
  }

/* 搜索区域样式 */
.search-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
  transition: all 0.3s;
  width: 100%;
}

.search-container:hover {
  box-shadow: 0 4px 15px 0 rgba(0, 0, 0, 0.15);
}

.search-header {
  margin-bottom: 20px;
}

.search-title {
  border-bottom: 2px solid #1976D2;
  padding-bottom: 10px;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: inline-block;
}

.search-title i {
  color: #1976D2;
  margin-right: 5px;
}

.search-content {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.search-input {
  width: 240px;
  margin-right: 15px;
  margin-bottom: 10px;
}

.search-input >>> .el-input__inner {
  border-radius: 20px;
}

.search-btn {
    border-radius: 20px;
  padding: 10px 25px;
  font-weight: 500;
}

/* 表格区域样式 */
.table-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
  transition: all 0.3s;
    width: 100%;
}

.table-container:hover {
  box-shadow: 0 4px 15px 0 rgba(0, 0, 0, 0.15);
  }

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
}

.table-title i {
  color: #1976D2;
  margin-right: 5px;
  font-size: 18px;
}

.table-count {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
  margin-left: 10px;
}

.add-btn {
  border-radius: 20px;
  padding: 10px 20px;
  }

.detail-table {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
}

.detail-table >>> .el-table__header-wrapper th {
  background-color: #f0f5ff !important;
  color: #1976D2;
  font-weight: 600;
  padding: 12px 0;
  }

.detail-table >>> .el-table__row {
  transition: all 0.3s;
  }

.detail-table >>> .el-table__row:hover {
  background-color: #f0f5ff !important;
}

.sport-type-cell,
.disease-cell,
.method-cell,
.notes-cell {
    display: flex;
    align-items: center;
    justify-content: center;
}

.sport-type-cell i,
.disease-cell i,
.method-cell i,
.notes-cell i {
  margin-right: 5px;
  color: #1976D2;
}

.disease-cell i {
  color: #E6A23C;
}

.notes-cell i {
  color: #F56C6C;
}

.operation-cell {
    display: flex;
  justify-content: center;
  gap: 10px;
  }

.edit-btn,
.delete-btn {
  padding: 6px 12px;
  border-radius: 4px;
  }

/* 分页区域样式 */
.pagination-container {
    display: flex;
  justify-content: center;
  margin-top: 20px;
}

.el-pagination >>> .el-pagination__total,
.el-pagination >>> .el-pagination__sizes,
.el-pagination >>> .el-pagination__jump {
  margin-right: 15px;
  }

/* 对话框样式 */
.detail-dialog >>> .el-dialog__header {
  background-color: #1976D2;
  padding: 15px 20px;
  border-radius: 8px 8px 0 0;
}

.detail-dialog >>> .el-dialog__title {
  font-size: 18px;
  font-weight: bold;
  color: #ffffff;
  }

.detail-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: #ffffff;
}

.dialog-content {
  padding: 20px 0;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 148px;
  height: 148px;
  line-height: 148px;
  text-align: center;
}

.sport-image {
  width: 148px;
  height: 148px;
  display: block;
  object-fit: cover;
}

.el-form-item >>> .el-input__inner {
  border-radius: 4px;
  padding-left: 35px;
  }

.el-form-item >>> .el-input__prefix {
  left: 10px;
  color: #1976D2;
}

.el-form-item >>> .el-textarea__inner {
    border-radius: 4px;
  padding: 10px;
  min-height: 80px;
  resize: vertical;
}

.dialog-footer {
  text-align: center;
}

.cancel-btn {
  border-radius: 20px;
  padding: 10px 25px;
}

.confirm-btn {
  border-radius: 20px;
  padding: 10px 25px;
  }

/* 按钮样式 */
.el-button--primary {
  background-color: #1976D2;
  border-color: #1976D2;
  transition: all 0.3s;
  }

.el-button--primary:hover,
.el-button--primary:focus {
  background-color: #1565C0;
  border-color: #1565C0;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.3);
  }

.el-button--danger {
  background-color: #e74c3c;
  border-color: #e74c3c;
  transition: all 0.3s;
}

.el-button--danger:hover,
.el-button--danger:focus {
  background-color: #c0392b;
  border-color: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
  }
  </style>
