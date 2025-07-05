<template>
  <el-dialog
    v-model="visible"
    title="连线操作帮助"
    :width="dialogWidth"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    class="help-dialog"
  >
    <div class="help-content">
      <div class="help-section">
        <h3>
          <el-icon class="section-icon"><Connection /></el-icon>
          连线操作步骤
        </h3>
        <ol class="help-steps">
          <li>
            <strong>开始连线</strong>
            <p>鼠标悬停在源节点上，连接点会自动显示</p>
            <p>点击源节点的输出连接点（右侧圆点）开始连线</p>
          </li>
          <li>
            <strong>拖拽连线</strong>
            <p>按住鼠标左键，拖动到目标节点</p>
            <p>连线过程中会看到虚线预览</p>
          </li>
          <li>
            <strong>完成连线</strong>
            <p>将鼠标拖拽到目标节点的输入连接点（左侧圆点）</p>
            <p>松开鼠标左键完成连线</p>
          </li>
        </ol>
      </div>

      <div class="help-section">
        <h3>
          <el-icon class="section-icon"><Document /></el-icon>
          连线规则
        </h3>
        <div class="help-rules">
          <div class="rule-item valid">
            <el-icon><CircleCheck /></el-icon>
            <div>
              <strong>有效连线</strong>
              <p>输出端口 → 输入端口</p>
            </div>
          </div>
          <div class="rule-item invalid">
            <el-icon><CircleClose /></el-icon>
            <div>
              <strong>无效连线</strong>
              <p>输入端口 → 输入端口</p>
              <p>输出端口 → 输出端口</p>
              <p>自己连接自己</p>
            </div>
          </div>
        </div>
      </div>

      <div class="help-section">
        <h3>
          <el-icon class="section-icon"><Warning /></el-icon>
          常见问题
        </h3>
        <div class="faq-list">
          <div class="faq-item">
            <h4>Q: 为什么连线失败？</h4>
            <div class="faq-answer">
              <p><strong>A:</strong> 可能的原因：</p>
              <ul>
                <li>没有在连接点上松开鼠标</li>
                <li>连接方向错误（输入→输出 或 输出→输入）</li>
                <li>尝试连接到自己</li>
                <li>已存在相同的连接</li>
              </ul>
            </div>
          </div>
          
          <div class="faq-item">
            <h4>Q: 连接点看不到怎么办？</h4>
            <div class="faq-answer">
              <p><strong>A:</strong> 连接点会在以下情况显示：</p>
              <ul>
                <li>鼠标悬停在节点上</li>
                <li>正在进行连线操作时</li>
                <li>连接点有较大的感应区域，可以更容易点击</li>
              </ul>
            </div>
          </div>
          
          <div class="faq-item">
            <h4>Q: 如何删除连线？</h4>
            <div class="faq-answer">
              <p><strong>A:</strong> 删除连线的方法：</p>
              <ul>
                <li>点击连线选中后，按Delete键</li>
                <li>在右侧属性面板中点击"删除连接"</li>
                <li>在调试面板的连线列表中点击"删除"</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="help-section">
        <h3>
          <el-icon class="section-icon"><Tools /></el-icon>
          调试技巧
        </h3>
        <div class="debug-tips">
          <div class="tip-item">
            <el-icon class="tip-icon"><Monitor /></el-icon>
            <div>
              <strong>使用调试面板</strong>
              <p>点击右上角的调试按钮，可以查看连线状态和详细信息</p>
            </div>
          </div>
          <div class="tip-item">
            <el-icon class="tip-icon"><View /></el-icon>
            <div>
              <strong>视觉反馈</strong>
              <p>连线时连接点会显示不同颜色：绿色表示可连接，红色表示不可连接</p>
            </div>
          </div>
          <div class="tip-item">
            <el-icon class="tip-icon"><InfoFilled /></el-icon>
            <div>
              <strong>错误提示</strong>
              <p>连线失败时会显示具体的错误原因和解决建议</p>
            </div>
          </div>
        </div>
      </div>

      <div class="help-section">
        <h3>
          <el-icon class="section-icon"><Operation /></el-icon>
          快捷键
        </h3>
        <div class="shortcuts">
          <div class="shortcut-item">
            <kbd>Delete</kbd>
            <span>删除选中的连线或节点</span>
          </div>
          <div class="shortcut-item">
            <kbd>Ctrl + S</kbd>
            <span>保存工作流</span>
          </div>
          <div class="shortcut-item">
            <kbd>Ctrl + Z</kbd>
            <span>撤销操作</span>
          </div>
          <div class="shortcut-item">
            <kbd>Escape</kbd>
            <span>取消当前连线操作</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="help-footer">
        <el-button @click="visible = false">我知道了</el-button>
        <el-button type="primary" @click="openTutorial">
          <el-icon><VideoPlay /></el-icon>
          观看教程
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'
import { 
  Connection, 
  Document, 
  Warning, 
  Tools, 
  Monitor, 
  View, 
  InfoFilled, 
  Operation, 
  VideoPlay,
  CircleCheck,
  CircleClose
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 定义props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

// 定义emits
const emit = defineEmits(['update:modelValue'])

// 控制对话框显示
const visible = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emit('update:modelValue', value)
  }
})

// 响应式对话框宽度
const dialogWidth = computed(() => {
  if (window.innerWidth < 768) return '95%'
  if (window.innerWidth < 1200) return '80%'
  return '70%'
})

// 打开教程
const openTutorial = () => {
  ElMessage.info('教程功能正在开发中...')
}
</script>

<style scoped>
.help-content {
  max-height: 70vh;
  overflow-y: auto;
  padding: 0 4px;
}

.help-section {
  margin-bottom: 32px;
}

.help-section:last-child {
  margin-bottom: 0;
}

.help-section h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 8px;
}

.section-icon {
  font-size: 20px;
  color: #409eff;
}

.help-steps {
  margin: 0;
  padding-left: 0;
  counter-reset: step-counter;
}

.help-steps li {
  position: relative;
  margin-bottom: 20px;
  padding-left: 60px;
  list-style: none;
  counter-increment: step-counter;
}

.help-steps li::before {
  content: counter(step-counter);
  position: absolute;
  left: 0;
  top: 0;
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
  font-size: 16px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.help-steps li strong {
  display: block;
  margin-bottom: 6px;
  font-size: 16px;
  color: #303133;
}

.help-steps li p {
  margin: 4px 0;
  color: #606266;
  line-height: 1.6;
}

.help-rules {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.rule-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  border-radius: 8px;
  border: 1px solid;
}

.rule-item.valid {
  background: #f6ffed;
  border-color: #b7eb8f;
}

.rule-item.valid .el-icon {
  color: #52c41a;
  font-size: 20px;
}

.rule-item.invalid {
  background: #fff2f0;
  border-color: #ffccc7;
}

.rule-item.invalid .el-icon {
  color: #f56c6c;
  font-size: 20px;
}

.rule-item strong {
  display: block;
  margin-bottom: 4px;
  color: #303133;
}

.rule-item p {
  margin: 2px 0;
  color: #606266;
  font-size: 14px;
}

.faq-list {
  space-y: 16px;
}

.faq-item {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
}

.faq-item h4 {
  margin: 0 0 8px 0;
  color: #303133;
  font-size: 16px;
}

.faq-answer p {
  margin: 8px 0;
  color: #606266;
  line-height: 1.6;
}

.faq-answer ul {
  margin: 8px 0;
  padding-left: 20px;
}

.faq-answer li {
  margin: 4px 0;
  color: #606266;
  line-height: 1.5;
}

.debug-tips {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.tip-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.tip-item strong {
  display: block;
  margin-bottom: 4px;
  color: #303133;
}

.tip-item p {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.shortcuts {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 6px;
}

.shortcut-item kbd {
  display: inline-block;
  padding: 4px 8px;
  font-size: 12px;
  line-height: 1;
  color: #666;
  vertical-align: middle;
  background-color: #fafbfc;
  border: 1px solid #d1d5da;
  border-radius: 3px;
  box-shadow: inset 0 -1px 0 #d1d5da;
  min-width: 60px;
  text-align: center;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
}

.shortcut-item span {
  color: #606266;
  font-size: 14px;
}

.help-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 滚动条样式 */
.help-content::-webkit-scrollbar {
  width: 6px;
}

.help-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.help-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.help-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .help-rules {
    grid-template-columns: 1fr;
  }
  
  .help-steps li {
    padding-left: 50px;
  }
  
  .help-steps li::before {
    width: 32px;
    height: 32px;
    font-size: 14px;
  }
  
  .help-section h3 {
    font-size: 16px;
  }
  
  .shortcut-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .shortcut-item kbd {
    min-width: auto;
  }
}

/* 深色主题样式 */
:deep(.el-dialog) {
  --el-dialog-bg-color: #fff;
}

:deep(.el-dialog__header) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px 8px 0 0;
  padding: 20px;
}

:deep(.el-dialog__title) {
  color: white;
  font-weight: 600;
}

:deep(.el-dialog__close) {
  color: white;
}

:deep(.el-dialog__close:hover) {
  color: #f0f0f0;
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__footer) {
  padding: 16px 20px;
  border-top: 1px solid #e4e7ed;
}
</style> 