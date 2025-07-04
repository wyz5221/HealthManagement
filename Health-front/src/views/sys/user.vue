<!-- 搜索框和添加按钮，搜索框，以及用户名和手机号的输入框 -->
<template>
  <div class="user-container">
    <!-- 搜索区域 -->
    <div class="search-container">
      <div class="search-header">
        <div class="search-title">
          <i class="el-icon-search" />
          <span>用户查询</span>
        </div>
      </div>
      <div class="search-content">
        <el-input
          v-model="searchModel.username"
          placeholder="请输入用户名"
          prefix-icon="el-icon-user"
          clearable
          class="search-input"
        />
        <el-input
          v-model="searchModel.phone"
          placeholder="请输入手机号"
          prefix-icon="el-icon-mobile-phone"
          clearable
          class="search-input"
        />
        <el-button
          type="primary"
          icon="el-icon-search"
          class="search-btn"
          @click="getUserList"
        >查询</el-button>
      </div>
    </div>

    <!-- 用户列表区域 -->
    <div class="table-container">
      <div class="table-header">
        <div class="table-title">
          <i class="el-icon-user" />
          <span>用户列表</span>
          <span class="table-count">共 {{ total }} 条记录</span>
        </div>

        <!-- 新增批量删除按钮 -->
        <el-button
          type="danger"
          icon="el-icon-delete"
          class="batch-delete-btn"
          :disabled="selectedUserIds.length === 0"
          @click="batchDelete"
        >批量删除</el-button>

        <el-button
          type="primary"
          icon="el-icon-plus"
          class="add-btn"
          @click="openEditUi(null)"
        >新增用户</el-button>

      </div>
      <el-table
        :data="userList"
        stripe
        border
        highlight-current-row
        class="user-table"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" /> <!-- 新增选择列 -->
        <el-table-column
          type="index"
          label="序号"
          width="80"
          align="center"
        />
        <el-table-column
          prop="id"
          label="用户ID"
          width="100"
          align="center"
        />
        <el-table-column
          prop="username"
          label="用户名"
          width="150"
          align="center"
        >
          <template slot-scope="scope">
            <div class="username-cell">
              <i class="el-icon-user-solid" />
              <span>{{ scope.row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="phone"
          label="电话"
          width="150"
          align="center"
        >
          <template slot-scope="scope">
            <div class="phone-cell">
              <i class="el-icon-phone" />
              <span>{{ scope.row.phone }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="email"
          label="电子邮件"
          align="center"
        >
          <template slot-scope="scope">
            <div class="email-cell">
              <i class="el-icon-message" />
              <span>{{ scope.row.email }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="200"
          align="center"
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
                @click="deleteUser(scope.row)"
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

    <!-- 用户编辑信息弹出框 -->
    <el-dialog
      :title="title"
      :visible.sync="dialogFormVisible"
      width="500px"
      :close-on-click-modal="false"
      center
      class="user-dialog"
      @close="clearForm"
    >
      <div class="dialog-content">
        <el-form ref="userFormRef" :model="userForm" :rules="rules" label-width="100px">
          <el-form-item
            label="用户名"
            prop="username"
          >
            <el-input v-model="userForm.username" placeholder="请输入用户名">
              <i slot="prefix" class="el-icon-user" />
            </el-input>
          </el-form-item>

          <el-form-item
            v-if="userForm.id == null || userForm.id == undefined"
            label="密码"
            prop="password"
          >
            <el-input
              v-model="userForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            >
              <i slot="prefix" class="el-icon-lock" />
            </el-input>
            <!-- 新增密码强度提示 -->
            <div v-if="userForm.password" class="password-strength">
              <span :class="{ 'strength-weak': passwordStrength === 'weak' }">弱</span>
              <span :class="{ 'strength-medium': passwordStrength === 'medium' }">中</span>
              <span :class="{ 'strength-strong': passwordStrength === 'strong' }">强</span>
            </div>
          </el-form-item>

          <el-form-item label="联系电话">
            <el-input
              v-model="userForm.phone"
              placeholder="请输入联系电话"
              @input="handlePhoneInput"
            >
              <i slot="prefix" class="el-icon-phone" />
            </el-input>
            <!-- 新增提示文本 -->
            <div v-if="phoneTips" class="phone-tips">{{ phoneTips }}</div>
          </el-form-item>

          <el-form-item
            label="电子邮件"
            prop="email"
          >
            <el-input v-model="userForm.email" placeholder="请输入电子邮件">
              <i slot="prefix" class="el-icon-message" />
            </el-input>
          </el-form-item>

          <el-form-item label="用户角色">
            <el-checkbox-group v-model="userForm.roleIdList" :max="1" class="role-checkbox-group">
              <el-checkbox
                v-for="role in roleList"
                :key="role.roleId"
                :label="role.roleId"
                class="role-checkbox"
              >{{ role.roleDesc }}</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button class="cancel-btn" @click="dialogFormVisible = false">取 消</el-button>
        <el-button class="reset-btn" @click="resetForm">重 置</el-button> <!-- 新增重置按钮 -->
        <el-button type="primary" class="confirm-btn" @click="saveUser">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import userApi from '@/api/userManage'
import roleApi from '@/api/roleManage'
export default {
  data() {
    // 自定义表单验证规则
    var checkEmail = (rule, value, callback) => {
      var reg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/
      if (!reg.test(value)) {
        return callback(new Error('输入的邮箱格式错误'))
      }
      callback()
    }

    var checkPhone = (rule, value, callback) => {
      // 处理空值
      if (!value) {
        return callback(new Error('请输入联系电话'))
      }
      // 转换为字符串并去空格
      const phoneStr = String(value).trim()
      // 长度校验
      if (phoneStr.length !== 11) {
        return callback(new Error('手机号应为11位数字'))
      }
      // 格式校验
      const reg = /^1[3-9]\d{9}$/
      if (!reg.test(phoneStr)) {
        return callback(new Error('请输入正确的手机号（以1开头，第二位为3-9）'))
      }
      callback()
    }

    return {
      // 左边宽度
      formLabelWidth: '135px',
      // 设置默认值不可见
      dialogFormVisible: false,
      userForm: {
        roleIdList: []
      },
      title: '',
      total: 0,
      roleList: [],
      searchModel: {
        pageNo: 1,
        // 默认显示数量
        pageSize: 10
      },
      // 新增选中用户ID数组
      selectedUserIds: [],
      phoneTips: '',
      userList: [],
      // 表单规则配置

      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          {
            min: 2,
            max: 20,
            message: '长度需要在 2 到 20 个字符',
            trigger: 'blur'
          }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          {
            min: 6,
            max: 20,
            message: '长度需要在 6 到 20 个字符',
            trigger: 'blur'
          }
        ],
        email: [
          { required: true, message: '请输入电子邮件', trigger: 'blur' },
          { validator: checkEmail, trigger: 'blur' }
        ],
        phone: [
          { required: true, message: '请输入联系电话', trigger: 'blur' },
          { validator: checkPhone, trigger: ['blur', 'change'] }
        ]
      }
    }
  },

  // 在data中添加计算属性
  computed: {
    passwordStrength() {
      const password = this.userForm.password || ''
      if (password.length < 6) return 'weak'
      if (/^[a-zA-Z]+$/.test(password) || /^\d+$/.test(password)) return 'weak'
      if (/^(?=.*[a-zA-Z])(?=.*\d).+$/.test(password)) return 'medium'
      if (/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&]).+$/.test(password)) return 'strong'
      return 'weak'
    }
  },

  created() {
    this.getUserList()
    this.getAllRoleList()
  },

  methods: {
    handlePhoneInput() {
      const phone = this.userForm.phone || ''
      const phoneStr = phone.trim()
      if (phoneStr.length === 0) {
        this.phoneTips = '请输入11位手机号'
        return
      }
      if (phoneStr.length < 11) {
        this.phoneTips = `已输入${phoneStr.length}位，还需${11 - phoneStr.length}位`
        return
      }
      const reg = /^1[3-9]\d{9}$/
      if (reg.test(phoneStr)) {
        this.phoneTips = '手机号格式正确'
      } else {
        this.phoneTips = '请输入正确的手机号（以1开头，第二位为3-9）'
      }
    },

    validatePhoneOnBlur() {
      // 失去焦点时触发正式校验
      this.$refs.userFormRef.validateField('phone')
    },

    getAllRoleList() {
      roleApi.getAllRoleList().then((response) => {
        this.roleList = response.data
      })
    },

    saveUser() {
      let isOk = true
      // 触发表单的验证
      this.$refs.userFormRef.validate((valid) => {
        // 这边只有校验失败的时候才会进来
        isOk = valid
      })
      if (isOk) {
        // 提交验证给后台
        userApi.saveUser(this.userForm).then((response) => {
          this.$message({
            message: response.message,
            type: 'success'
          })
          // 关闭对话框
          this.dialogFormVisible = false
          // 刷新表格数据
          this.getUserList()
        })
      } else {
        console.log('error submit!!')
        return false
      }
    },

    clearForm() {
      this.userForm = {
        roleIdList: []
      }
      // 清除表单校验的提示信息
      this.$refs.userFormRef.clearValidate()
    },

    handleSizeChange(pageSize) {
      // 数据更新
      this.searchModel.pageSize = pageSize
      this.getUserList()
    },

    handleCurrentChange(pageNo) {
      this.searchModel.pageNo = pageNo
      this.getUserList()
    },

    getUserList() {
      userApi.getUserList(this.searchModel).then((response) => {
        this.userList = response.data.rows
        this.total = response.data.total
      })
    },

    openEditUi(id) {
      if (id == null) {
        this.title = '新增用户'
      } else {
        this.title = '修改用户'
        // 根据id查询用户数据
        userApi.getUserById(id).then((response) => {
          this.userForm = response.data
        })
      }
      this.dialogFormVisible = true
    },

    resetForm() {
      // 如果是编辑模式，从原始数据恢复
      if (this.userForm.id) {
        userApi.getUserById(this.userForm.id).then((response) => {
          this.userForm = response.data
          this.$refs.userFormRef.clearValidate() // 清除校验提示
        })
      } else {
        // 如果是新增模式，恢复初始空值
        this.clearForm()
      }
    },

    deleteUser(user) {
      this.$confirm(`确认删除 ${user.username} 这个账户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          userApi.deleteUserById(user.id).then((response) => {
            this.$message({
              type: 'success',
              message: response.message
            })
            this.getUserList()
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },

    // 处理选中用户变化
    handleSelectionChange(selection) {
      this.selectedUserIds = selection.map(item => item.id)
    },

    // 批量删除方法
    batchDelete() {
      if (this.selectedUserIds.length === 0) {
        this.$message.warning('请先选择要删除的用户')
        return
      }

      this.$confirm(`确认删除选中的 ${this.selectedUserIds.length} 个用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        userApi.batchDeleteUser(this.selectedUserIds).then((response) => {
          this.$message({
            type: 'success',
            message: response.message
          })
          this.getUserList() // 刷新列表
          this.selectedUserIds = [] // 清空选中
        })
      }).catch(() => {
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
.user-container {
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

.user-table {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.user-table >>> .el-table__header-wrapper th {
  background-color: #f0f5ff !important;
  color: #1976D2;
  font-weight: 600;
  padding: 12px 0;
}

.user-table >>> .el-table__row {
  transition: all 0.3s;
}

.user-table >>> .el-table__row:hover {
  background-color: #f0f5ff !important;
}

.username-cell,
.phone-cell,
.email-cell {
  display: flex;
  align-items: center;
  justify-content: center;
}

.username-cell i,
.phone-cell i,
.email-cell i {
  margin-right: 5px;
  color: #1976D2;
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
.user-dialog >>> .el-dialog__header {
  background-color: #1976D2;
  padding: 15px 20px;
  border-radius: 8px 8px 0 0;
}

.user-dialog >>> .el-dialog__title {
  font-size: 18px;
  font-weight: bold;
  color: #ffffff;
}

.user-dialog >>> .el-dialog__headerbtn .el-dialog__close {
  color: #ffffff;
}

.dialog-content {
  padding: 20px 0;
}

.el-form-item >>> .el-input__inner {
  border-radius: 4px;
  padding-left: 35px;
}

.el-form-item >>> .el-input__prefix {
  left: 10px;
  color: #1976D2;
}

.role-checkbox-group {
  display: flex;
  flex-wrap: wrap;
}

.role-checkbox {
  margin-right: 15px;
  margin-bottom: 10px;
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

/* 在style中添加样式 */
.password-strength {
  display: flex;
  justify-content: space-between;
  margin-top: 5px;
  font-size: 12px;
}

.strength-weak { color: #F56C6C; }
.strength-medium { color: #E6A23C; }
.strength-strong { color: #67C23A; }

/* 批量操作按钮样式 */
.batch-operations {
  display: flex;
  gap: 10px;
}

.batch-delete-btn {
  margin-left: auto;
  border-radius: 20px;
  padding: 10px 20px;
}

.phone-tips {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

</style>
