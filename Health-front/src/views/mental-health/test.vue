<template>
  <div class="app-container">
    <el-card class="box-card" shadow="hover">
      <div slot="header" class="clearfix header">
        <div class="title-container">
          <i class="el-icon-data-analysis"></i>
          <span class="title">抑郁自评量表（SDS）测试</span>
        </div>
        <el-button type="text" @click="resetTest" icon="el-icon-refresh-right">重置</el-button>
      </div>

      <div class="test-intro" v-if="currentStep === 0">
        <div class="intro-card">
          <div class="intro-header">
            <i class="el-icon-info-circle"></i>
            <span>测试说明</span>
          </div>
          <div class="intro-content">
            <p>以下是抑郁自评量表（SDS，Self-Rating Depression Scale）的国际标准题目，共20题，采用4级评分法。</p>
            <p>请根据最近一周内的实际感受进行选择。</p>
          </div>
        </div>

        <div class="criteria-card">
          <div class="criteria-header">
            <i class="el-icon-medal"></i>
            <span>评分标准</span>
          </div>
          <div class="criteria-content">
            <div class="criteria-grid">
              <div class="criteria-item">
                <div class="criteria-badge score-1">1分</div>
                <div class="criteria-text">没有或很少时间（＜1天）</div>
              </div>
              <div class="criteria-item">
                <div class="criteria-badge score-2">2分</div>
                <div class="criteria-text">小部分时间（1-2天）</div>
              </div>
              <div class="criteria-item">
                <div class="criteria-badge score-3">3分</div>
                <div class="criteria-text">相当多时间（3-4天）</div>
              </div>
              <div class="criteria-item">
                <div class="criteria-badge score-4">4分</div>
                <div class="criteria-text">绝大部分或全部时间（5-7天）</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 试题选择模块 -->
        <div class="paper-selection-container">
          <div class="paper-selection-header">
            <i class="el-icon-document"></i>
            <span>选择测试试卷</span>
          </div>
          <div class="paper-selection-content">
            <el-radio-group v-model="selectedPaperId" @change="handlePaperChange" class="paper-radio-group">
              <el-radio
                v-for="paper in paperList"
                :key="paper.id"
                :label="paper.id"
                class="paper-radio-item"
                :disabled="paper.status === 0">
                <div class="paper-info">
                  <div class="paper-name">{{ paper.name }}</div>
                  <div class="paper-status">
                    <el-tag
                      :type="paper.status === 1 ? 'success' : 'danger'"
                      size="mini"
                      effect="dark">
                      {{ paper.status === 1 ? '启用' : '停用' }}
                    </el-tag>
                  </div>
                </div>
              </el-radio>
            </el-radio-group>

            <div v-if="paperList.length === 0" class="no-paper-tip">
              <i class="el-icon-warning"></i>
              <span>暂无可用的测试试卷，请联系管理员</span>
            </div>
          </div>
        </div>

        <div class="action-container">
          <el-button
            type="primary"
            size="large"
            @click="startTest"
            icon="el-icon-caret-right"
            :disabled="!selectedPaperId || !canStartTest">
            {{ selectedPaperId ? '开始测试' : '请先选择试卷' }}
          </el-button>
        </div>
      </div>

      <div v-else-if="currentStep > 0 && currentStep <= questions.length" class="test-content">
        <div class="progress-bar-container">
          <div class="progress-bar-wrapper">
            <el-progress
              :percentage="progressPercentage"
              :color="progressColor"
              :stroke-width="12"
              :format="progressFormat">
            </el-progress>
          </div>
          <div class="progress-info">
            <span class="current-question">第 {{ currentStep }} 题</span>
            <span class="total-questions">共 {{ questions.length }} 题</span>
          </div>
        </div>

        <el-carousel
          ref="questionCarousel"
          :autoplay="false"
          indicator-position="none"
          :initial-index="currentStep-1"
          @change="handleCarouselChange"
          :loop="false">
          <el-carousel-item v-for="(question, index) in questions" :key="index">
            <div class="question-card" :class="{'answered': testForm.answers[index] !== null}">
              <div class="question-header">
                <div class="question-number">{{ index + 1 }}</div>
                <div class="question-text">{{ question.text }}</div>
              </div>

              <div class="options-container">
                <div
                  v-for="score in 4"
                  :key="score"
                  class="option-item"
                  :class="[
                    `option-score-${score}`,
                    testForm.answers[index] === score ? 'option-selected' : ''
                  ]"
                  @click="selectAnswer(index, score)"
                >
                  <div :class="['criteria-badge', `score-${score}`]">{{ score }}分</div>
                  <div class="criteria-text">
                    <template v-if="score === 1">没有或很少时间（＜1天）</template>
                    <template v-else-if="score === 2">小部分时间（1-2天）</template>
                    <template v-else-if="score === 3">相当多时间（3-4天）</template>
                    <template v-else>绝大部分或全部时间（5-7天）</template>
                  </div>
                  <div class="check-icon" v-if="testForm.answers[index] === score">
                    <i class="el-icon-check"></i>
                  </div>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>

        <div class="navigation-buttons">
          <el-button
            icon="el-icon-arrow-left"
            @click="prevQuestion"
            :disabled="currentStep <= 1">
            上一题
          </el-button>

          <div class="center-info">
            <div class="nav-page-info">{{ currentStep }} / {{ questions.length }}</div>
          </div>

          <el-button
            v-if="currentStep < questions.length"
            type="primary"
            @click="nextQuestion"
            :disabled="testForm.answers[currentStep-1] === null"
            icon="el-icon-arrow-right"
            iconPosition="right">
            下一题
          </el-button>

          <el-button
            v-else
            type="success"
            @click="submitTest"
            :loading="loading"
            icon="el-icon-check">
            提交测试
          </el-button>
        </div>
      </div>

      <div v-else-if="currentStep === questions.length + 1" class="test-summary">
        <div class="summary-header">
          <i class="el-icon-circle-check"></i>
          <span>测试完成</span>
        </div>

        <div class="summary-content">
          <div class="completion-icon">
            <i class="el-icon-finished"></i>
          </div>
          <div class="completion-text">
            <h3>您已回答完所有问题</h3>
            <p>点击下方按钮提交测试结果并查看分析</p>
          </div>

          <div class="summary-stats">
            <div class="stat-item">
              <div class="stat-label">总题数</div>
              <div class="stat-value">{{ questions.length }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">已回答</div>
              <div class="stat-value">{{ answeredQuestions }}</div>
            </div>
          </div>

          <div class="review-hint">
            <i class="el-icon-info-circle"></i>
            <span>您可以使用上一题按钮回顾并修改您的回答</span>
          </div>
        </div>

        <div class="action-container">
          <el-button icon="el-icon-arrow-left" @click="prevQuestion">回顾上一题</el-button>
          <el-button type="success" @click="submitTest" :loading="loading" icon="el-icon-check">提交测试</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import FunctionApi from '@/api/Function_Menu'
import userApi from "@/api/userManage"
import { getInfo } from '@/api/user'
import { getToken } from '@/utils/auth'
import { getPaperList, getQuestions } from '@/api/paperManage'

export default {
  name: 'MentalHealthTest',
  data() {
    return {
      loading: false,
      userId: null,
      currentStep: 0, // 0: intro, 1-20: questions, 21: summary
      testForm: {
        answers: [],
      },
      // 试卷相关数据
      paperList: [],
      selectedPaperId: null,
      questions: [],
      // 默认的SDS题目（当没有选择试卷时使用）
      defaultQuestions: [
        { text: '我感到情绪沮丧，闷闷不乐', isReverse: false },
        { text: '我感到早晨心情最好', isReverse: true },
        { text: '我要哭或想哭', isReverse: false },
        { text: '我夜间睡眠不好', isReverse: false },
        { text: '我吃饭像平时一样多', isReverse: true },
        { text: '我的性功能正常', isReverse: true },
        { text: '我感到体重减轻', isReverse: false },
        { text: '我为便秘烦恼', isReverse: false },
        { text: '我的心跳比平时快', isReverse: false },
        { text: '我无故感到疲劳', isReverse: false },
        { text: '我的头脑像往常一样清楚', isReverse: true },
        { text: '我做事情像平时一样不感到困难', isReverse: true },
        { text: '我坐卧不安，难以保持平静', isReverse: false },
        { text: '我对未来感到有希望', isReverse: true },
        { text: '我比平时更容易激怒', isReverse: false },
        { text: '我觉得决定什么事很容易', isReverse: true },
        { text: '我感到自己是有用的和不可缺少的人', isReverse: true },
        { text: '我的生活很有意义', isReverse: true },
        { text: '假若我死了别人会过得更好', isReverse: false },
        { text: '我仍旧喜爱自己平时喜爱的东西', isReverse: true }
      ]
    }
  },
  computed: {
    answeredQuestions() {
      return this.testForm.answers.filter(answer => answer !== null).length
    },
    progressPercentage() {
      return Math.floor((this.currentStep / this.questions.length) * 100)
    },
    progressColor() {
      if (this.progressPercentage < 30) return '#909399'
      if (this.progressPercentage < 70) return '#e6a23c'
      if (this.progressPercentage < 100) return '#409EFF'
      return '#67c23a'
    },
    canStartTest() {
      return this.selectedPaperId && this.questions.length > 0
    }
  },
  watch: {
    currentStep(newVal) {
      if (newVal > 0 && newVal <= this.questions.length) {
        this.$nextTick(() => {
          if (this.$refs.questionCarousel) {
            this.$refs.questionCarousel.setActiveItem(newVal - 1)
          }
        })
      }
    }
  },
  created() {
    this.getUserId()
    this.loadPaperList()
  },
  methods: {
    progressFormat() {
      return `${this.progressPercentage}%`
    },
    // 加载试卷列表
    async loadPaperList() {
      try {
        const response = await getPaperList()
        if (response.code === 20000) {
          this.paperList = response.data || []
          // 如果有启用的试卷，默认选择第一个
          const enabledPapers = this.paperList.filter(paper => paper.status === 1)
          if (enabledPapers.length > 0) {
            this.selectedPaperId = enabledPapers[0].id
            await this.handlePaperChange(this.selectedPaperId)
          }
        }
      } catch (error) {
        console.error('加载试卷列表失败:', error)
        this.$message.error('加载试卷列表失败')
      }
    },
    // 处理试卷选择变化
    async handlePaperChange(paperId) {
      if (!paperId) {
        this.questions = []
        this.testForm.answers = []
        return
      }

      try {
        const response = await getQuestions(paperId)
        if (response.code === 20000) {
          const questionsData = response.data || []
          // 将题目数据转换为需要的格式，正确设置isReverse字段
          this.questions = questionsData.map((q, index) => ({
            text: q.content,
            isReverse: q.isReverse === 1, // 根据数据库中的isReverse字段设置
            sort: q.sort
          }))
          // 初始化答案数组
          this.testForm.answers = new Array(this.questions.length).fill(null)
        } else {
          // 如果获取题目失败，使用默认题目
          this.questions = [...this.defaultQuestions]
          this.testForm.answers = new Array(this.questions.length).fill(null)
        }
      } catch (error) {
        console.error('加载题目失败:', error)
        // 使用默认题目
        this.questions = [...this.defaultQuestions]
        this.testForm.answers = new Array(this.questions.length).fill(null)
        this.$message.warning('加载题目失败，使用默认题目')
      }
    },
    startTest() {
      if (!this.canStartTest) {
        this.$message.warning('请先选择试卷')
        return
      }
      this.currentStep = 1
    },
    selectAnswer(index, score) {
      this.$set(this.testForm.answers, index, score)

      // 如果用户已经回答，且不是最后一题，自动前进到下一题
      if (this.currentStep < this.questions.length) {
        setTimeout(() => {
          this.nextQuestion()
        }, 300)
      }
    },
    nextQuestion() {
      if (this.currentStep < this.questions.length + 1) {
        this.currentStep++
      }
    },
    prevQuestion() {
      if (this.currentStep > 1) {
        this.currentStep--
      }
    },
    handleCarouselChange(index) {
      this.currentStep = index + 1
    },
    async getUserId() {
      try {
        // 先调用getInfo接口确保后端loginUser已设置
        const token = getToken()
        if (token) {
          const infoResponse = await getInfo(token)
          if (infoResponse.code === 20000 && infoResponse.data.id) {
            this.userId = infoResponse.data.id
            return
          }
        }

        // 如果getInfo接口没有返回id，再尝试调用getUserId接口
        const response = await userApi.getUserId()
        if (response.code === 20000) {
          this.userId = response.data.userId
        } else {
          this.$message.error('获取用户信息失败，请重新登录')
        }
      } catch (error) {
        console.error('获取用户信息错误:', error)
        this.$message.error('获取用户信息失败，请重新登录')
      }
    },
    calculateScore() {
      let totalScore = 0

      this.testForm.answers.forEach((answer, index) => {
        if (answer === null) return

        // 根据isReverse字段决定计分方式
        if (this.questions[index].isReverse) {
          // 反向计分：1->4, 2->3, 3->2, 4->1
          totalScore += 5 - answer
        } else {
          // 正向计分：直接使用原始得分
          totalScore += answer
        }
      })

      // 根据SDS计算方法，总分乘以1.25
      return Math.floor(totalScore * 1.25)
    },

    submitTest() {
      // 检查是否有未回答的问题
      if (this.answeredQuestions < this.questions.length) {
        const unansweredCount = this.questions.length - this.answeredQuestions
        this.$message.warning(`还有 ${unansweredCount} 题未回答，请完成所有问题再提交`)
        return
      }

      this.loading = true

      this.submitTestData()
    },

    async submitTestData() {
      try {
        // 检查用户ID
        if (!this.userId) {
          await this.getUserId()
          if (!this.userId) {
            this.$message.error('获取用户信息失败，请重新登录')
            this.loading = false
            return
          }
        }

        // 计算得分
        const totalScore = this.calculateScore()

        // 准备提交的数据
        const testData = {
          userId: this.userId,
          totalScore: totalScore,
          answers: JSON.stringify(this.testForm.answers)
        }

        // 提交测试结果
        const response = await FunctionApi.saveMentalHealthTest(testData)

        if (response.code === 20000) {
          this.$message.success('测试完成')
          // 跳转到结果页面
          this.$router.push(`/mental-health/result/${response.data.id}`)
        } else {
          this.$message.error(response.msg || '提交失败')
        }
      } catch (error) {
        console.error('提交测试出错:', error)
        this.$message.error('提交失败，请稍后重试')
      } finally {
        this.loading = false
      }
    },

    resetTest() {
      this.testForm.answers = new Array(this.questions.length).fill(null)
      this.currentStep = 0
    }
  }
}
</script>

<style scoped lang="scss">
.box-card {
  border-radius: 12px;
  margin-bottom: 20px;
  transition: all 0.3s;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  border: none;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
}

.title-container {
  display: flex;
  align-items: center;

  i {
    font-size: 22px;
    margin-right: 12px;
    color: #409EFF;
  }
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

/* 测试介绍页 */
.test-intro {
  padding: 20px 0;
  max-width: 800px;
  margin: 0 auto;
}

.intro-card, .criteria-card {
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}

.intro-header, .criteria-header {
  background: linear-gradient(90deg, #f6f8fb, #f0f4f9);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
  border-bottom: 1px solid #ebeef5;

  i {
    color: #409EFF;
    margin-right: 10px;
    font-size: 20px;
  }
}

.intro-content, .criteria-content {
  padding: 20px;
}

.intro-content p {
  margin: 0 0 12px;
  line-height: 1.8;
  color: #606266;
  font-size: 15px;
}

.intro-content p:last-child {
  margin-bottom: 0;
}

.criteria-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.criteria-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background-color: #f8f9fa;
  transition: all 0.3s;
  border: 1px solid #e4e7ed;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-3px);
  }
}

.criteria-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 15px;
  margin-right: 12px;
  min-width: 45px;
  text-align: center;
  flex-shrink: 0;
}

.criteria-text {
  font-size: 15px;
  color: #606266;
  flex: 1;
}

.score-1 {
  background-color: #e1f3d8;
  color: #67c23a;
}

.score-2 {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.score-3 {
  background-color: #fef0f0;
  color: #f56c6c;
}

.score-4 {
  background-color: #f0f2f5;
  color: #606266;
}

.action-container {
  display: flex;
  justify-content: center;
  padding-top: 20px;

  .el-button {
    padding: 15px 40px;
    font-size: 16px;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
}

/* 测试内容页 */
.test-content {
  padding: 20px 0;
  max-width: 1000px;
  margin: 0 auto;
}

.progress-bar-container {
  margin-bottom: 30px;
}

.progress-bar-wrapper {
  margin-bottom: 10px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #909399;

  .current-question {
    font-weight: 600;
    color: #409EFF;
  }
}

/* 轮播样式 */
.el-carousel {
  margin-bottom: 30px;
}

.el-carousel__container {
  height: auto !important;
  min-height: 750px;
}

.question-card {
  position: relative;
  background-color: white;
  border-radius: 12px;
  padding: 30px 20px;
  box-shadow: 0 1px 15px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  border-left: 5px solid #dcdfe6;
  overflow-y: auto;
  height: 100%;

  &.answered {
    border-left-color: #409EFF;
  }
}

.question-header {
  display: flex;
  align-items: center;
  margin-bottom: 40px;
}

.question-number {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  background-color: #409EFF;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 20px;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.3);
  margin-right: 20px;
  flex-shrink: 0;
}

.question-text {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
  line-height: 1.6;
}

.options-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-top: 20px;
  width: 100%;
  margin-bottom: 0px;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background-color: #f8f9fa;
  border: 1px solid #e4e7ed;
  position: relative;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transform: translateY(-3px);
  }

  &.option-selected {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    border-color: #409EFF;
  }

  &.option-selected.option-score-1 {
    background-color: rgba(103, 194, 58, 0.1);
  }

  &.option-selected.option-score-2 {
    background-color: rgba(230, 162, 60, 0.1);
  }

  &.option-selected.option-score-3 {
    background-color: rgba(245, 108, 108, 0.1);
  }

  &.option-selected.option-score-4 {
    background-color: rgba(144, 147, 153, 0.1);
  }
}

.check-icon {
  color: #409EFF;
  font-size: 18px;
  margin-left: auto;
}

.navigation-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  margin-top: 30px;

  .el-button {
    padding: 12px 24px;
    border-radius: 8px;
  }
}

.center-info {
  text-align: center;
}

.nav-page-info {
  font-size: 16px;
  font-weight: 600;
  color: #606266;
  padding: 6px 16px;
  background-color: #f5f7fa;
  border-radius: 20px;
}

/* 测试总结页 */
.test-summary {
  padding: 30px 0;
  max-width: 600px;
  margin: 0 auto;
}

.summary-header {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 600;
  color: #67c23a;
  margin-bottom: 30px;

  i {
    font-size: 28px;
    margin-right: 10px;
  }
}

.summary-content {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 30px;
  text-align: center;
  margin-bottom: 30px;
  border: 1px solid #ebeef5;
}

.completion-icon {
  font-size: 60px;
  color: #67c23a;
  margin-bottom: 20px;
}

.completion-text h3 {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
}

.completion-text p {
  font-size: 16px;
  color: #606266;
  margin-bottom: 30px;
}

.summary-stats {
  display: flex;
  justify-content: center;
  gap: 40px;
  margin-bottom: 30px;
}

.stat-item {
  text-align: center;

  .stat-label {
    font-size: 14px;
    color: #909399;
    margin-bottom: 5px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: bold;
    color: #409EFF;
  }
}

.review-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e6a23c;
  font-size: 14px;
  background-color: rgba(230, 162, 60, 0.1);
  padding: 10px;
  border-radius: 6px;

  i {
    margin-right: 8px;
    font-size: 16px;
  }
}

/* 试卷选择部分美化 */
.paper-selection-container {
  margin: 30px 0;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #ebeef5;
  overflow: hidden;
}

.paper-selection-header {
  background: linear-gradient(90deg, #f6f8fb, #f0f4f9);
  padding: 16px 20px;
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #303133;
  border-bottom: 1px solid #ebeef5;

  i {
    color: #409EFF;
    margin-right: 10px;
    font-size: 20px;
  }
}

.paper-selection-content {
  padding: 20px;
}

.paper-radio-group {
  width: 100%;
}

.paper-radio-item {
  display: block;
  margin-bottom: 12px;
  padding: 16px;
  border-radius: 8px;
  background-color: #fff;
  border: 1px solid #e4e7ed;
  transition: all 0.3s;
  width: 100%;

  &:hover {
    border-color: #409EFF;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
  }

  &.is-checked {
    border-color: #409EFF;
    background-color: rgba(64, 158, 255, 0.05);
  }
}

.paper-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.paper-name {
  font-size: 15px;
  color: #303133;
  font-weight: 500;
}

.paper-status {
  margin-left: 10px;
}

.no-paper-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #e6a23c;
  font-size: 14px;
  padding: 20px;

  i {
    margin-right: 8px;
    font-size: 16px;
  }
}

.start-btn {
  width: 200px;
  height: 48px;
  font-size: 16px;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
  }
}

@media (max-width: 768px) {
  .criteria-grid {
    grid-template-columns: 1fr;
  }

  .options-container {
    grid-template-columns: 1fr;
  }

  .question-card {
    padding: 20px 15px;
  }

  .question-text {
    font-size: 18px;
  }

  .navigation-buttons {
    flex-direction: column;
    gap: 15px;

    .el-button {
      width: 100%;
    }
  }

  .center-info {
    order: -1;
    margin-bottom: 15px;
  }

  .action-container {
    flex-direction: column;
    gap: 15px;

    .el-button {
      width: 100%;
    }
  }

  .summary-stats {
    gap: 20px;
  }

  .paper-selection-container {
    margin: 20px 0;
  }
}
</style>
