<template>
  <div class="jolt-test-container">
    <div class="page-header">
      <h1>JOLT数据转换测试</h1>
      <p class="page-description">测试和验证JOLT数据转换规则</p>
    </div>

    <el-row :gutter="24">
      <!-- 左侧：测试样例 -->
      <el-col :span="8">
        <el-card shadow="hover" header="测试样例">
          <el-tabs v-model="activeTab" @tab-click="handleTabClick">
            <el-tab-pane label="预定义样例" name="samples">
              <div class="samples-list">
                <div v-if="samplesLoading" class="loading-container">
                  <el-skeleton :rows="3" animated />
                </div>
                <div v-else>
                  <div 
                    v-for="sample in samples" 
                    :key="sample.id"
                    class="sample-item"
                    :class="{ 'active': selectedSample?.id === sample.id }"
                    @click="selectSample(sample)"
                  >
                    <div class="sample-name">{{ sample.name }}</div>
                    <div class="sample-description">{{ sample.description }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="自定义测试" name="custom">
              <div class="custom-test-form">
                <el-form label-position="top">
                  <el-form-item label="测试名称">
                    <el-input v-model="customTest.name" placeholder="输入测试名称" />
                  </el-form-item>
                  <el-form-item label="描述">
                    <el-input 
                      v-model="customTest.description" 
                      type="textarea" 
                      :rows="2" 
                      placeholder="输入测试描述" 
                    />
                  </el-form-item>
                  <el-button type="primary" @click="useCustomTest" style="width: 100%;">
                    使用自定义测试
                  </el-button>
                </el-form>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>

      <!-- 中间：输入数据和JOLT规则 -->
      <el-col :span="8">
        <el-card shadow="hover" header="输入配置">
          <el-form label-position="top">
            <el-form-item label="输入数据">
              <el-input
                v-model="inputDataText"
                type="textarea"
                :rows="8"
                placeholder="请输入JSON格式的数据"
                style="font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;"
              />
              <div class="form-actions">
                <el-button size="small" @click="formatInputData">格式化JSON</el-button>
                <el-button size="small" @click="validateInputData">验证JSON</el-button>
              </div>
            </el-form-item>
            
            <el-form-item label="JOLT转换规则">
              <el-input
                v-model="joltSpecText"
                type="textarea"
                :rows="8"
                placeholder="请输入JOLT转换规则"
                style="font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;"
              />
              <div class="form-actions">
                <el-button size="small" @click="formatJoltSpec">格式化JSON</el-button>
                <el-button size="small" @click="validateJoltSpec">验证规则</el-button>
              </div>
            </el-form-item>
            
            <div class="test-actions">
              <el-button 
                type="primary" 
                @click="runTransform" 
                :loading="transforming"
                style="width: 100%;"
              >
                <el-icon><VideoPlay /></el-icon>
                执行转换
              </el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：转换结果 -->
      <el-col :span="8">
        <el-card shadow="hover" header="转换结果">
          <div v-if="!transformResult" class="no-result">
            <el-empty description="暂无转换结果" />
          </div>
          
          <div v-else class="result-container">
            <div class="result-status">
              <el-tag 
                :type="transformResult.success ? 'success' : 'danger'" 
                size="large"
              >
                {{ transformResult.success ? '转换成功' : '转换失败' }}
              </el-tag>
              <span v-if="transformResult.executionTime" class="execution-time">
                耗时: {{ transformResult.executionTime }}ms
              </span>
            </div>
            
            <el-tabs v-if="transformResult.success">
              <el-tab-pane label="转换结果">
                <div class="result-data">
                  <pre class="json-display">{{ formatJson(transformResult.transformedData) }}</pre>
                </div>
              </el-tab-pane>
              
              <el-tab-pane label="对比结果" v-if="selectedSample && selectedSample.expectedOutput">
                <div class="comparison-result">
                  <div class="comparison-status">
                    <el-tag 
                      :type="comparisonResult?.isMatch ? 'success' : 'warning'"
                      v-if="comparisonResult"
                    >
                      {{ comparisonResult.isMatch ? '结果匹配' : '结果不匹配' }}
                    </el-tag>
                  </div>
                  
                  <div class="comparison-data">
                    <div class="expected-result">
                      <h4>期望结果</h4>
                      <pre class="json-display">{{ formatJson(selectedSample.expectedOutput) }}</pre>
                    </div>
                    <div class="actual-result">
                      <h4>实际结果</h4>
                      <pre class="json-display">{{ formatJson(transformResult.transformedData) }}</pre>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
            
            <div v-else class="error-result">
              <el-alert
                :title="transformResult.error || '转换失败'"
                type="error"
                :closable="false"
                show-icon
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 批量测试对话框 -->
    <el-dialog
      v-model="showBatchTestDialog"
      title="批量测试结果"
      width="80%"
      :before-close="closeBatchTestDialog"
    >
      <div v-if="batchTestResult">
        <div class="batch-summary">
          <el-card>
            <el-statistic-group direction="horizontal">
              <el-statistic title="总数" :value="batchTestResult.totalCount" />
              <el-statistic title="通过" :value="batchTestResult.passCount" />
              <el-statistic title="失败" :value="batchTestResult.failCount" />
              <el-statistic 
                title="通过率" 
                :value="batchTestResult.passRate" 
                suffix="%" 
                :precision="1"
              />
            </el-statistic-group>
          </el-card>
        </div>
        
        <div class="batch-results">
          <el-table :data="batchTestResult.results" style="width: 100%">
            <el-table-column prop="sampleName" label="测试样例" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.isMatch ? 'success' : 'danger'">
                  {{ row.isMatch ? '通过' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="executionTime" label="耗时(ms)" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-button size="small" @click="viewBatchTestDetail(row)">
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="closeBatchTestDialog">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 浮动操作按钮 -->
    <div class="floating-actions">
      <el-button 
        type="primary" 
        circle 
        @click="runBatchTest"
        :loading="batchTesting"
        title="批量测试所有样例"
      >
        <el-icon><Operation /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { VideoPlay, Operation } from '@element-plus/icons-vue'
import request from '@/api/index.ts'

interface Sample {
  id: number
  name: string
  description?: string
  inputData: any
  joltSpec: string
  expectedOutput?: any
}

interface TransformResult {
  success: boolean
  transformedData?: any
  executionTime?: number
  error?: string
}

interface ComparisonResult {
  isMatch: boolean
  expected?: any
  actual?: any
  error?: string
}

interface BatchTestResult {
  passRate: number
  totalCount: number
  passCount: number
  failCount: number
  results: BatchTestDetail[]
}

interface BatchTestDetail {
  sampleId: number
  sampleName: string
  isMatch: boolean
  inputData: any
  joltSpec: string
  expectedOutput: any
  actualOutput: any
  executionTime: number
}

interface CustomTest {
  name: string
  description: string
}

interface TabClickEvent {
  name: string
}

// 响应式数据
const activeTab = ref<'samples' | 'custom'>('samples')
const samplesLoading = ref(false)
const transforming = ref(false)
const batchTesting = ref(false)

const samples = ref<Sample[]>([])
const selectedSample = ref<Sample | null>(null)
const inputDataText = ref('')
const joltSpecText = ref('')
const transformResult = ref<TransformResult | null>(null)
const comparisonResult = ref<ComparisonResult | null>(null)

const showBatchTestDialog = ref(false)
const batchTestResult = ref<BatchTestResult | null>(null)

const customTest = reactive<CustomTest>({
  name: '',
  description: ''
})

// 计算属性
const currentTestName = computed(() => {
  if (activeTab.value === 'samples' && selectedSample.value) {
    return selectedSample.value.name
  } else if (activeTab.value === 'custom') {
    return customTest.name || '自定义测试'
  }
  return '未选择测试'
})

// 生命周期
onMounted(() => {
  loadSamples()
})

// 方法
const loadSamples = async () => {
  samplesLoading.value = true
  try {
    const response = await request.get('/jolt-test/samples')
    if (response.success) {
      samples.value = response.data
      if (samples.value.length > 0) {
        selectSample(samples.value[0])
      }
    } else {
      ElMessage.error(response.message || '加载测试样例失败')
    }
  } catch (error) {
    console.error('加载测试样例失败:', error as Error)
    ElMessage.error('加载测试样例失败')
  } finally {
    samplesLoading.value = false
  }
}

const selectSample = (sample: Sample) => {
  selectedSample.value = sample
  inputDataText.value = formatJson(sample.inputData)
  joltSpecText.value = sample.joltSpec
  transformResult.value = null
  comparisonResult.value = null
}

const useCustomTest = () => {
  selectedSample.value = null
  inputDataText.value = '{\n  "key": "value"\n}'
  joltSpecText.value = '[\n  {\n    "operation": "shift",\n    "spec": {\n      "key": "newKey"\n    }\n  }\n]'
  transformResult.value = null
  comparisonResult.value = null
}

const handleTabClick = (tab: TabClickEvent) => {
  if (tab.name === 'custom') {
    useCustomTest()
  } else if (tab.name === 'samples' && samples.value.length > 0) {
    selectSample(samples.value[0])
  }
}

const formatJson = (obj: any): string => {
  if (typeof obj === 'string') {
    try {
      obj = JSON.parse(obj)
    } catch (e) {
      return obj
    }
  }
  return JSON.stringify(obj, null, 2)
}

const formatInputData = () => {
  try {
    const parsed = JSON.parse(inputDataText.value)
    inputDataText.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('JSON格式化成功')
  } catch (error) {
    ElMessage.error('JSON格式错误')
  }
}

const formatJoltSpec = () => {
  try {
    const parsed = JSON.parse(joltSpecText.value)
    joltSpecText.value = JSON.stringify(parsed, null, 2)
    ElMessage.success('JOLT规则格式化成功')
  } catch (error) {
    ElMessage.error('JOLT规则格式错误')
  }
}

const validateInputData = () => {
  try {
    JSON.parse(inputDataText.value)
    ElMessage.success('输入数据JSON格式正确')
  } catch (error) {
    ElMessage.error('输入数据JSON格式错误: ' + (error as Error).message)
  }
}

const validateJoltSpec = async () => {
  try {
    const response = await request.post('/jolt-test/validate', {
      joltSpec: joltSpecText.value
    })
    
    if (response.success) {
      const result = response.data
      if (result.valid) {
        ElMessage.success(`JOLT规则语法正确，包含${result.specCount}个转换步骤`)
      } else {
        ElMessage.error(`JOLT规则语法错误: ${result.message}`)
      }
    } else {
      ElMessage.error(response.message || 'JOLT规则验证失败')
    }
  } catch (error) {
    console.error('验证JOLT规则失败:', error as Error)
    ElMessage.error('验证JOLT规则失败')
  }
}

const runTransform = async () => {
  try {
    // 验证输入
    const inputData = JSON.parse(inputDataText.value)
    const joltSpec = joltSpecText.value
    
    transforming.value = true
    const response = await request.post('/jolt-test/transform', {
      inputData,
      joltSpec
    })
    
    if (response.success) {
      transformResult.value = response.data
      
      // 如果有选中的样例，进行结果比较
      if (selectedSample.value && selectedSample.value.expectedOutput) {
        compareResults()
      }
      
      ElMessage.success('JOLT转换执行成功')
    } else {
      transformResult.value = {
        success: false,
        error: response.message || '转换失败'
      }
      ElMessage.error(response.message || '转换失败')
    }
  } catch (error) {
    if (error instanceof SyntaxError) {
      ElMessage.error('输入数据格式错误，请检查JSON格式')
    } else {
      console.error('执行JOLT转换失败:', error as Error)
      ElMessage.error('执行转换失败')
    }
    transformResult.value = {
      success: false,
      error: '转换执行异常: ' + (error as Error).message
    }
  } finally {
    transforming.value = false
  }
}

const compareResults = () => {
  if (!transformResult.value?.success || !selectedSample.value?.expectedOutput) {
    return
  }
  
  try {
    const expected = JSON.stringify(selectedSample.value.expectedOutput)
    const actual = JSON.stringify(transformResult.value.transformedData)
    
    comparisonResult.value = {
      isMatch: expected === actual,
      expected: selectedSample.value.expectedOutput,
      actual: transformResult.value.transformedData
    }
  } catch (error) {
    console.error('比较结果失败:', error as Error)
    comparisonResult.value = {
      isMatch: false,
      error: '比较结果时发生错误'
    }
  }
}

const runBatchTest = async () => {
  batchTesting.value = true
  try {
    const response = await request.post('/jolt-test/samples/run-all')
    
    if (response.success) {
      batchTestResult.value = response.data
      showBatchTestDialog.value = true
      ElMessage.success(`批量测试完成，通过率: ${response.data.passRate?.toFixed(1)}%`)
    } else {
      ElMessage.error(response.message || '批量测试失败')
    }
  } catch (error) {
    console.error('批量测试失败:', error as Error)
    ElMessage.error('批量测试失败')
  } finally {
    batchTesting.value = false
  }
}

const viewBatchTestDetail = (testResult: BatchTestDetail) => {
  // 显示单个测试结果的详情
  selectedSample.value = {
    id: testResult.sampleId,
    name: testResult.sampleName,
    expectedOutput: testResult.expectedOutput
  }
  inputDataText.value = formatJson(testResult.inputData)
  joltSpecText.value = testResult.joltSpec
  transformResult.value = {
    success: true,
    transformedData: testResult.actualOutput,
    executionTime: testResult.executionTime
  }
  comparisonResult.value = {
    isMatch: testResult.isMatch,
    expected: testResult.expectedOutput,
    actual: testResult.actualOutput
  }
  
  activeTab.value = 'samples'
  closeBatchTestDialog()
}

const closeBatchTestDialog = () => {
  showBatchTestDialog.value = false
}
</script>

<style scoped>
.jolt-test-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-header h1 {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.page-description {
  margin: 8px 0 0 0;
  color: #606266;
  font-size: 14px;
}

.samples-list {
  max-height: 400px;
  overflow-y: auto;
}

.sample-item {
  padding: 12px;
  margin-bottom: 8px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  background: #ffffff;
}

.sample-item:hover {
  border-color: #409eff;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
}

.sample-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.sample-name {
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.sample-description {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
}

.custom-test-form {
  padding: 16px 0;
}

.form-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.test-actions {
  margin-top: 16px;
}

.no-result {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 300px;
}

.result-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-status {
  display: flex;
  align-items: center;
  gap: 12px;
}

.execution-time {
  font-size: 12px;
  color: #606266;
}

.result-data {
  max-height: 300px;
  overflow-y: auto;
}

.json-display {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

.comparison-result {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comparison-status {
  text-align: center;
}

.comparison-data {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.comparison-data h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.expected-result .json-display {
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
}

.actual-result .json-display {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
}

.error-result {
  margin-top: 16px;
}

.floating-actions {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 1000;
}

.batch-summary {
  margin-bottom: 24px;
}

.batch-results {
  max-height: 400px;
  overflow-y: auto;
}

.loading-container {
  padding: 16px;
}

/* 滚动条样式 */
.samples-list::-webkit-scrollbar,
.result-data::-webkit-scrollbar,
.batch-results::-webkit-scrollbar {
  width: 6px;
}

.samples-list::-webkit-scrollbar-track,
.result-data::-webkit-scrollbar-track,
.batch-results::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.samples-list::-webkit-scrollbar-thumb,
.result-data::-webkit-scrollbar-thumb,
.batch-results::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.samples-list::-webkit-scrollbar-thumb:hover,
.result-data::-webkit-scrollbar-thumb:hover,
.batch-results::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .jolt-test-container {
    padding: 16px;
  }
  
  .comparison-data {
    grid-template-columns: 1fr;
  }
}
</style> 