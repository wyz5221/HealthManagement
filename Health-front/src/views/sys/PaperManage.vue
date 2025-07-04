<template>
  <div class="app-container">
    <el-card>
      <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
        <span style="font-size: 18px; font-weight: bold;">测试题录入</span>
        <el-button type="primary" @click="openAddDialog">新增试卷</el-button>
      </div>
      <el-table :data="paperList" style="width: 100%">
        <el-table-column prop="name" label="试卷名称" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="状态">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="changeStatus(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template slot-scope="scope">
            <el-button size="mini" @click="editPaper(scope.row)">编辑</el-button>
            <el-button size="mini" type="danger" @click="deletePaper(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑试卷弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="800px">
      <div v-if="step === 1">
        <el-form :model="paperForm" label-width="80px">
          <el-form-item label="试卷名称">
            <el-input v-model="paperForm.name" />
          </el-form-item>
        </el-form>
        <div style="text-align: right;">
          <el-button type="primary" @click="nextStep">下一步</el-button>
        </div>
      </div>
      <div v-else>
        <el-form :model="questionForm" label-width="80px">
          <el-form-item v-for="(item, idx) in questionForm" :key="idx" :label="'题目' + (idx+1)">
            <div style="display: flex; align-items: flex-start; gap: 10px;">
              <el-input 
                v-model="item.content" 
                placeholder="请输入题目内容" 
                style="flex: 1;"
                type="textarea"
                :rows="2"
              />
              <div style="display: flex; flex-direction: column; align-items: center; gap: 5px; min-width: 80px;">
                <el-switch
                  v-model="item.isReverse"
                  :active-value="1"
                  :inactive-value="0"
                  active-text="反向"
                  inactive-text="正向"
                  style="margin-bottom: 5px;"
                />
                <span style="font-size: 12px; color: #666;">
                  {{ item.isReverse ? '反向计分' : '正向计分' }}
                </span>
              </div>
            </div>
          </el-form-item>
        </el-form>
        <div style="text-align: right;">
          <el-button @click="step=1">上一步</el-button>
          <el-button type="primary" @click="submitPaper">提交</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPaperList, addPaper, changePaperStatus, deletePaper, addQuestions, getQuestions } from '@/api/paperManage'
export default {
  name: 'PaperManage',
  data() {
    return {
      paperList: [],
      dialogVisible: false,
      dialogTitle: '新增试卷',
      step: 1,
      paperForm: { name: '' },
      questionForm: Array.from({length: 20}, () => ({ content: '', isReverse: 0 })),
      currentPaperId: null
    }
  },
  created() {
    this.loadPapers()
  },
  methods: {
    loadPapers() {
      getPaperList().then(res => { this.paperList = res.data || [] })
    },
    openAddDialog() {
      this.dialogTitle = '新增试卷'
      this.dialogVisible = true
      this.step = 1
      this.paperForm = { name: '' }
      this.questionForm = Array.from({length: 20}, () => ({ content: '', isReverse: 0 }))
      this.currentPaperId = null
    },
    nextStep() {
      if (!this.paperForm.name) return this.$message.error('请输入试卷名称')
      if (this.dialogTitle === '编辑试卷') {
        this.step = 2
      } else {
        addPaper(this.paperForm).then(res => {
          this.currentPaperId = res.data || res
          this.step = 2
        })
      }
    },
    submitPaper() {
      if (this.questionForm.some(q => !q.content)) return this.$message.error('请填写所有题目')
      addQuestions(this.currentPaperId, this.questionForm).then(() => {
        this.$message.success(this.dialogTitle === '编辑试卷' ? '修改成功' : '新增成功')
        this.dialogVisible = false
        this.loadPapers()
      })
    },
    changeStatus(row) {
      changePaperStatus(row.id, row.status).then(() => {
        this.$message.success('状态已更新')
      })
    },
    deletePaper(id) {
      this.$confirm('确定删除该试卷吗？').then(() => {
        deletePaper(id).then(() => {
          this.$message.success('删除成功')
          this.loadPapers()
        })
      })
    },
    editPaper(row) {
      this.dialogTitle = '编辑试卷'
      this.dialogVisible = true
      this.step = 1
      this.paperForm = { ...row }
      this.currentPaperId = row.id
      getQuestions(row.id).then(res => {
        this.questionForm = res.data || []
        // 确保每个题目都有isReverse字段
        this.questionForm.forEach(q => {
          if (q.isReverse === undefined) {
            q.isReverse = 0
          }
        })
        while (this.questionForm.length < 20) {
          this.questionForm.push({ content: '', isReverse: 0 })
        }
      })
    }
  }
}
</script> 