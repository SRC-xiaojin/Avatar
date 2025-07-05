<template>
  <div 
    class="monaco-editor-container" 
    :class="{ readonly: readonly }"
    :style="{ width: editorWidth }"
  >
    <div class="editor-toolbar" v-if="showToolbar">
      <div class="toolbar-left">
        <span class="editor-title">{{ title }}</span>
      </div>
      <div class="toolbar-right">
        <div class="readonly-indicator" v-if="readonly">
          <el-tag size="small" type="info">只读模式</el-tag>
        </div>
        <el-button-group size="small">
          <el-dropdown 
            v-if="!readonly"
            trigger="click"
            @command="handleFormatCommand"
          >
            <el-button title="格式化代码">
              <el-icon><DocumentChecked /></el-icon>
              <el-icon><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="format-document">
                  <el-icon><DocumentChecked /></el-icon>
                  格式化整个文档
                </el-dropdown-item>
                <el-dropdown-item command="format-selection" :disabled="!hasSelection">
                  <el-icon><Edit /></el-icon>
                  格式化选中代码
                </el-dropdown-item>
                <el-dropdown-item divided command="format-settings">
                  <el-icon><Setting /></el-icon>
                  格式化设置
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button @click="validateContent" title="验证代码">
            <el-icon><Select /></el-icon>
          </el-button>
          <el-button 
            @click="toggleFullscreen" 
            :title="readonly ? '全屏查看' : '全屏编辑'"
          >
            <el-icon><FullScreen /></el-icon>
          </el-button>
        </el-button-group>
      </div>
    </div>
    
    <div 
      ref="editorContainer" 
      class="monaco-editor"
      :style="{ height: editorHeight }"
    ></div>
    
    <div class="editor-status" v-if="showStatus">
      <span class="status-info">
        <el-icon><Document /></el-icon>
        行数: {{ lineCount }} | 字符数: {{ charCount }}
      </span>
      <span v-if="validationMessage" class="validation-message" :class="validationClass">
        <el-icon><InfoFilled /></el-icon>
        {{ validationMessage }}
      </span>
    </div>

    <!-- 全屏模态框 -->
    <el-dialog
      v-model="fullscreenVisible"
      title="代码编辑器"
      :width="'95%'"
      :top="'3vh'"
      :before-close="closeFullscreen"
      destroy-on-close
      class="fullscreen-editor-dialog"
    >
      <div 
        ref="fullscreenEditorContainer" 
        class="monaco-editor fullscreen-editor"
        style="height: 75vh;"
      ></div>
      
      <template #footer>
        <el-button @click="closeFullscreen">{{ readonly ? '关闭' : '取消' }}</el-button>
        <el-button 
          v-if="!readonly" 
          type="primary" 
          @click="saveFullscreenContent"
        >
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 格式化设置对话框 -->
    <el-dialog
      v-model="formatSettingsVisible"
      title="代码格式化设置"
      width="500px"
      :before-close="closeFormatSettings"
    >
      <el-form :model="formatSettings" label-width="120px">
        <el-form-item label="缩进方式">
          <el-radio-group v-model="formatSettings.insertSpaces">
            <el-radio :label="true">空格</el-radio>
            <el-radio :label="false">Tab</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="缩进大小">
          <el-input-number 
            v-model="formatSettings.tabSize" 
            :min="1" 
            :max="8" 
            size="small"
          />
        </el-form-item>
        
        <el-form-item label="自动检测缩进">
          <el-switch v-model="formatSettings.detectIndentation" />
        </el-form-item>
        
        <el-form-item label="删除行尾空格">
          <el-switch v-model="formatSettings.trimTrailingWhitespace" />
        </el-form-item>
        
        <el-form-item label="文件末尾换行">
          <el-switch v-model="formatSettings.insertFinalNewline" />
        </el-form-item>
        
        <template v-if="['javascript', 'typescript'].includes(props.language)">
          <el-divider content-position="left">JavaScript/TypeScript</el-divider>
          
          <el-form-item label="使用分号">
            <el-switch v-model="formatSettings.semicolons" />
          </el-form-item>
          
          <el-form-item label="使用单引号">
            <el-switch v-model="formatSettings.singleQuote" />
          </el-form-item>
          
          <el-form-item label="尾随逗号">
            <el-select v-model="formatSettings.trailingComma" size="small">
              <el-option label="无" value="none" />
              <el-option label="ES5" value="es5" />
              <el-option label="所有" value="all" />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      
      <template #footer>
        <el-button @click="closeFormatSettings">取消</el-button>
        <el-button @click="resetFormatSettings">重置默认</el-button>
        <el-button type="primary" @click="applyFormatSettings">应用设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick, Ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentChecked, Select, FullScreen, Document, InfoFilled, ArrowDown, Edit, Setting } from '@element-plus/icons-vue'
import * as monaco from 'monaco-editor'
import loader from '@monaco-editor/loader'

interface Props {
  modelValue: string
  language: string
  theme: string
  height: string
  width: string
  title: string
  placeholder: string
  showToolbar: boolean
  showStatus: boolean
  readonly: boolean
  options: Record<string, any>
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
  (e: 'focus'): void
  (e: 'blur'): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  language: 'json',
  theme: 'vs',
  height: '200px',
  width: '100%',
  title: '代码编辑器',
  placeholder: '请输入内容...',
  showToolbar: true,
  showStatus: true,
  readonly: false,
  options: () => ({})
})

const emit = defineEmits<Emits>()

// 编辑器相关
const editorContainer = ref<HTMLElement>()
const fullscreenEditorContainer = ref<HTMLElement>()
let editor: monaco.editor.IStandaloneCodeEditor | null = null
let fullscreenEditor: monaco.editor.IStandaloneCodeEditor | null = null

// 状态相关
const editorHeight = ref(props.height)
const editorWidth = ref(props.width)
const lineCount = ref(0)
const charCount = ref(0)
const validationMessage = ref('')
const validationClass = ref('')
const fullscreenVisible = ref(false)
const hasSelection = ref(false)
const formatSettingsVisible = ref(false)
let fullscreenContent = ''

// 格式化设置
const formatSettings = ref({
  insertSpaces: true,
  tabSize: 2,
  detectIndentation: true,
  trimTrailingWhitespace: true,
  insertFinalNewline: false,
  semicolons: true, // JavaScript/TypeScript
  singleQuote: true, // JavaScript/TypeScript
  trailingComma: 'es5' // JavaScript/TypeScript
})

// 默认编辑器选项
const defaultOptions: monaco.editor.IStandaloneEditorConstructionOptions = {
  automaticLayout: true,
  scrollBeyondLastLine: false,
  minimap: { enabled: false },
  fontSize: 14,
  lineNumbers: 'on' as monaco.editor.LineNumbersType,
  roundedSelection: false,
  scrollbar: {
    verticalScrollbarSize: 8,
    horizontalScrollbarSize: 8
  },
  folding: true,
  wordWrap: 'on' as monaco.editor.IEditorOptions['wordWrap'],
  tabSize: 2,
  insertSpaces: true,
  formatOnPaste: true,
  formatOnType: true
}

// 只读模式的额外选项
const readonlyOptions: Partial<monaco.editor.IStandaloneEditorConstructionOptions> = {
  contextmenu: false, // 禁用右键菜单
  quickSuggestions: false, // 禁用快速建议
  parameterHints: { enabled: false }, // 禁用参数提示
  suggestOnTriggerCharacters: false, // 禁用触发字符建议
  acceptSuggestionOnEnter: 'off' as any, // 禁用回车接受建议
  tabCompletion: 'off' as any, // 禁用Tab补全
  wordBasedSuggestions: false, // 禁用基于单词的建议
  quickSuggestionsDelay: 10000, // 延迟建议
  links: false, // 禁用链接检测
  colorDecorators: false, // 禁用颜色装饰器
  selectionHighlight: false, // 禁用选择高亮
  occurrencesHighlight: false, // 禁用出现次数高亮
  cursorStyle: 'line' as any, // 光标样式
  hideCursorInOverviewRuler: true, // 隐藏概览标尺中的光标
  overviewRulerBorder: false // 隐藏概览标尺边框
}

// 初始化Monaco Editor
const initEditor = async () => {
  try {
    // 配置Monaco Editor
    loader.config({
      paths: {
        vs: 'https://cdn.jsdelivr.net/npm/monaco-editor@0.44.0/min/vs'
      }
    })

    const monacoInstance = await loader.init()
    
    // 创建编辑器实例
    const editorOptions = {
      value: props.modelValue || '',
      language: props.language,
      theme: props.theme,
      readOnly: props.readonly,
      ...defaultOptions,
      ...(props.readonly ? readonlyOptions : {}),
      ...props.options
    }
    
    if (!editorContainer.value) {
      throw new Error('编辑器容器未找到')
    }
    
    editor = monacoInstance.editor.create(editorContainer.value, editorOptions)

    // 监听内容变化
    editor.onDidChangeModelContent(() => {
      if (editor) {
        const value = editor.getValue()
        emit('update:modelValue', value)
        emit('change', value)
        updateStatus()
      }
    })

    // 监听选择变化
    editor.onDidChangeCursorSelection(() => {
      updateSelectionState()
    })

    // 监听焦点事件
    editor.onDidFocusEditorText(() => {
      emit('focus')
    })

    editor.onDidBlurEditorText(() => {
      emit('blur')
    })

    // 初始更新状态
    updateStatus()

    // 设置占位符
    if (!props.modelValue && props.placeholder) {
      showPlaceholder()
    }

  } catch (error) {
    console.error('Monaco Editor初始化失败:', error as Error)
    ElMessage.error('代码编辑器初始化失败')
  }
}

// 显示占位符
const showPlaceholder = () => {
  if (editor && !editor.getValue()) {
    editor.setValue('')
    // 可以在这里添加占位符逻辑
  }
}

// 更新状态信息
const updateStatus = () => {
  if (editor) {
    const model = editor.getModel()
    if (model) {
      lineCount.value = model.getLineCount()
      charCount.value = model.getValueLength()
    }
  }
}

// 检测选中状态
const updateSelectionState = () => {
  if (editor) {
    const selection = editor.getSelection()
    hasSelection.value = selection ? !selection.isEmpty() : false
  }
}

// 处理格式化命令
const handleFormatCommand = async (command: string) => {
  switch (command) {
    case 'format-document':
      await formatDocument()
      break
    case 'format-selection':
      await formatSelection()
      break
    case 'format-settings':
      showFormatSettings()
      break
  }
}

// 格式化整个文档
const formatDocument = async () => {
  if (!editor) return
  
  try {
    // 显示格式化进度
    const loadingMessage = ElMessage({
      message: '正在格式化代码...',
      type: 'info',
      duration: 0,
      showClose: false
    })

    // 应用格式化设置
    await applyFormatSettings()
    
    // 执行格式化
    const action = editor.getAction('editor.action.formatDocument')
    if (action) {
      await action.run()
      
      // 如果是JSON，进行额外的美化处理
      if (props.language === 'json') {
        await formatJsonContent()
      }
      
      loadingMessage.close()
      ElMessage.success('代码格式化成功')
    } else {
      loadingMessage.close()
      // 如果没有内置格式化器，使用自定义格式化
      await customFormat()
    }
  } catch (error) {
    console.error('格式化失败:', error as Error)
    ElMessage.error('代码格式化失败: ' + (error as Error).message)
  }
}

// 格式化选中内容
const formatSelection = async () => {
  if (!editor || !hasSelection.value) return
  
  try {
    const action = editor.getAction('editor.action.formatSelection')
    if (action) {
      await action.run()
      ElMessage.success('选中代码格式化成功')
    } else {
      ElMessage.warning('当前语言不支持选择格式化')
    }
  } catch (error) {
    console.error('选择格式化失败:', error as Error)
    ElMessage.error('选择格式化失败')
  }
}

// 应用格式化设置
const applyFormatSettings = async () => {
  if (!editor) return
  
  const model = editor.getModel()
  if (model) {
    // 更新编辑器选项
    editor.updateOptions({
      insertSpaces: formatSettings.value.insertSpaces,
      tabSize: formatSettings.value.tabSize,
      detectIndentation: formatSettings.value.detectIndentation
    })
    
    // 应用模型选项
    model.updateOptions({
      insertSpaces: formatSettings.value.insertSpaces,
      tabSize: formatSettings.value.tabSize,
      trimAutoWhitespace: formatSettings.value.trimTrailingWhitespace
    })
  }
}

// JSON内容特殊格式化
const formatJsonContent = async () => {
  if (!editor || props.language !== 'json') return
  
  try {
    const content = editor.getValue()
    if (!content.trim()) return
    
    const parsed = JSON.parse(content)
    const formatted = JSON.stringify(parsed, null, formatSettings.value.tabSize)
    
    if (formatted !== content) {
      editor.setValue(formatted)
    }
  } catch (error) {
    // JSON格式错误时不处理
    console.warn('JSON格式化跳过，内容可能不是有效的JSON')
  }
}

// 自定义格式化（当没有内置格式化器时）
const customFormat = async () => {
  if (!editor) return
  
  const content = editor.getValue()
  let formattedContent = content
  
  try {
    switch (props.language) {
      case 'json':
        const parsed = JSON.parse(content)
        formattedContent = JSON.stringify(parsed, null, formatSettings.value.tabSize)
        break
      case 'xml':
        formattedContent = formatXml(content)
        break
      case 'css':
        formattedContent = formatCss(content)
        break
      default:
        formattedContent = formatGeneric(content)
    }
    
    if (formattedContent !== content) {
      editor.setValue(formattedContent)
      ElMessage.success('代码格式化成功')
    } else {
      ElMessage.info('代码已经是格式化的')
    }
  } catch (error) {
    ElMessage.error('自定义格式化失败: ' + (error as Error).message)
  }
}

// XML格式化
const formatXml = (xml: string): string => {
  const formatted = xml.replace(/(>)(<)(\/*)/g, '$1\n$2$3')
  let pad = 0
  return formatted.split('\n').map(line => {
    let indent = 0
    if (line.match(/.+<\/\w[^>]*>$/)) {
      indent = 0
    } else if (line.match(/^<\/\w/)) {
      if (pad !== 0) {
        pad -= 1
      }
    } else if (line.match(/^<\w[^>]*[^\/]>.*$/)) {
      indent = 1
    } else {
      indent = 0
    }
    
    const spaces = '  '.repeat(pad)
    pad += indent
    return spaces + line
  }).join('\n')
}

// CSS格式化
const formatCss = (css: string): string => {
  return css
    .replace(/\s*{\s*/g, ' {\n  ')
    .replace(/;\s*/g, ';\n  ')
    .replace(/\s*}\s*/g, '\n}\n')
    .replace(/,\s*/g, ',\n')
    .replace(/\n\s*\n/g, '\n')
    .trim()
}

// 通用格式化
const formatGeneric = (content: string): string => {
  return content
    .split('\n')
    .map(line => line.trim())
    .join('\n')
    .replace(/\n\s*\n/g, '\n')
}

// 显示格式化设置对话框
const showFormatSettings = () => {
  formatSettingsVisible.value = true
}

// 关闭格式化设置对话框
const closeFormatSettings = () => {
  formatSettingsVisible.value = false
}

// 重置格式化设置
const resetFormatSettings = () => {
  formatSettings.value = {
    insertSpaces: true,
    tabSize: 2,
    detectIndentation: true,
    trimTrailingWhitespace: true,
    insertFinalNewline: false,
    semicolons: true,
    singleQuote: true,
    trailingComma: 'es5'
  }
}

// 增强原有的formatContent方法（保持向后兼容）
const formatContent = async () => {
  await formatDocument()
}

// 验证JSON内容
const validateContent = () => {
  if (!editor) return
  
  const content = editor.getValue()
  if (!content.trim()) {
    validationMessage.value = '内容为空'
    validationClass.value = 'warning'
    return
  }

  if (props.language === 'json') {
    try {
      JSON.parse(content)
      validationMessage.value = 'JSON格式正确'
      validationClass.value = 'success'
      ElMessage.success('JSON验证通过')
    } catch (error: unknown) {
      validationMessage.value = `JSON格式错误: ${(error as Error).message}`
      validationClass.value = 'error'
      ElMessage.error('JSON格式验证失败')
    }
  } else {
    validationMessage.value = '语法检查通过'
    validationClass.value = 'success'
    ElMessage.success('语法验证通过')
  }

  // 3秒后清除验证消息
  setTimeout(() => {
    validationMessage.value = ''
    validationClass.value = ''
  }, 3000)
}

// 切换全屏模式
const toggleFullscreen = async () => {
  if (!editor) return
  
  fullscreenContent = editor.getValue()
  fullscreenVisible.value = true
  
  await nextTick()
  initFullscreenEditor()
}

// 初始化全屏编辑器
const initFullscreenEditor = async () => {
  try {
    const monacoInstance = await loader.init()
    
    const fullscreenOptions = {
      value: fullscreenContent,
      language: props.language,
      theme: props.theme,
      readOnly: props.readonly,
      ...defaultOptions,
      ...(props.readonly ? readonlyOptions : {}),
      ...props.options
    }
    
    fullscreenEditor = monacoInstance.editor.create(fullscreenEditorContainer.value!, fullscreenOptions)
  } catch (error) {
    console.error('全屏编辑器初始化失败:', error)
    ElMessage.error('全屏编辑器初始化失败')
  }
}

// 保存全屏内容
const saveFullscreenContent = () => {
  if (fullscreenEditor && editor) {
    const content = fullscreenEditor.getValue()
    editor.setValue(content)
    emit('update:modelValue', content)
    emit('change', content)
  }
  closeFullscreen()
}

// 关闭全屏模式
const closeFullscreen = () => {
  if (fullscreenEditor) {
    fullscreenEditor.dispose()
    fullscreenEditor = null
  }
  fullscreenVisible.value = false
}

// 监听props变化
watch(() => props.modelValue, (newValue) => {
  if (editor && editor.getValue() !== newValue) {
    editor.setValue(newValue || '')
    updateStatus()
  }
})

watch(() => props.language, (newLanguage) => {
  if (editor) {
    const model = editor.getModel()
    if (model) {
      monaco.editor.setModelLanguage(model, newLanguage)
    }
  }
})

watch(() => props.height, (newHeight) => {
  editorHeight.value = newHeight
  if (editor) {
    nextTick(() => {
      editor?.layout()
    })
  }
})

watch(() => props.width, (newWidth) => {
  editorWidth.value = newWidth
  if (editor) {
    nextTick(() => {
      editor?.layout()
    })
  }
})

// 生命周期
onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  if (editor) {
    editor.dispose()
  }
  if (fullscreenEditor) {
    fullscreenEditor.dispose()
  }
})

// 暴露方法给父组件
defineExpose({
  formatContent,
  formatDocument,
  formatSelection,
  validateContent,
  focus: () => editor?.focus(),
  setValue: (value: string) => editor?.setValue(value || ''),
  getValue: () => editor?.getValue() || ''
})
</script>

<style scoped>
.monaco-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #ffffff;
}

.monaco-editor-container.readonly {
  border-color: #c0c4cc;
  background: #f8f9fa;
}

.monaco-editor-container.readonly .editor-toolbar {
  background: linear-gradient(135deg, #e8eaed 0%, #dde0e4 100%);
  border-bottom-color: #c0c4cc;
}

.monaco-editor-container.readonly .editor-title {
  color: #6c757d;
}

.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.editor-title {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.readonly-indicator {
  display: flex;
  align-items: center;
}

.monaco-editor {
  width: 100%;
}

.editor-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  background: #fafbfc;
  border-top: 1px solid #e4e7ed;
  font-size: 12px;
  color: #909399;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 4px;
}

.validation-message {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: 500;
}

.validation-message.success {
  color: #67c23a;
}

.validation-message.warning {
  color: #e6a23c;
}

.validation-message.error {
  color: #f56c6c;
}

.fullscreen-editor {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

/* 全屏对话框样式 */
:deep(.fullscreen-editor-dialog) {
  .el-dialog__body {
    padding: 8px 16px;
  }
  
  .el-dialog {
    margin: 0 auto;
    max-width: none;
  }
  
  .el-dialog__header {
    padding: 16px 20px 12px;
    border-bottom: 1px solid #e4e7ed;
  }
  
  .el-dialog__title {
    font-weight: 600;
    font-size: 16px;
  }
  
  .el-dialog__footer {
    padding: 12px 20px 16px;
    border-top: 1px solid #e4e7ed;
    text-align: right;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .editor-toolbar {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .toolbar-right {
    justify-content: center;
  }
  
  .editor-status {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  /* 移动端全屏对话框 */
  :deep(.fullscreen-editor-dialog) {
    .el-dialog {
      width: 98% !important;
      margin: 1vh auto;
    }
  }
}

/* 大屏幕优化 */
@media (min-width: 1920px) {
  :deep(.fullscreen-editor-dialog) {
    .el-dialog {
      width: 90% !important;
      max-width: 1600px;
    }
  }
}

/* 超大屏幕优化 */
@media (min-width: 2560px) {
  :deep(.fullscreen-editor-dialog) {
    .el-dialog {
      width: 85% !important;
      max-width: 2000px;
    }
  }
}
</style> 