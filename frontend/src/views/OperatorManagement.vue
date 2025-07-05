<template>
  <div class="operator-management">
    <div class="page-header">
      <div class="header-left">
        <h2>算子管理</h2>
        <p>管理算子类别和模板</p>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="addCategory">
          <el-icon><Plus /></el-icon>
          新建类别
        </el-button>
        <el-button type="success" @click="addTemplate">
          <el-icon><Plus /></el-icon>
          新建模板
        </el-button>
      </div>
    </div>

    <div class="page-content">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <!-- 算子类别管理 -->
        <el-tab-pane label="算子类别" name="categories">
          <div class="search-bar">
            <el-input
              v-model="categorySearch"
              placeholder="搜索类别名称"
              style="width: 300px"
              clearable
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>

          <el-table :data="filteredCategories" v-loading="categoriesLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="categoryCode" label="类别编码" width="150" />
            <el-table-column prop="categoryName" label="类别名称" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'danger'">
                  {{ row.status ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sortOrder" label="排序" width="80" />
            <el-table-column label="操作" width="250">
              <template #default="{ row }">
                <el-button size="small" @click="editCategory(row)">
                  编辑
                </el-button>
                <el-button size="small" type="warning" @click="toggleCategoryStatus(row)">
                  {{ row.status ? '禁用' : '启用' }}
                </el-button>
                <el-button size="small" type="danger" @click="deleteCategory(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 算子模板管理 -->
        <el-tab-pane label="算子模板" name="templates">
          <div class="search-bar">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-input
                  v-model="templateSearch"
                  placeholder="搜索模板名称"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </el-col>
              <el-col :span="6">
                <el-select
                  v-model="selectedCategoryId"
                  placeholder="选择类别"
                  @change="loadTemplates"
                  clearable
                >
                  <el-option
                    v-for="category in categories"
                    :key="category.id"
                    :label="category.categoryName"
                    :value="category.id"
                  />
                </el-select>
              </el-col>
            </el-row>
          </div>

          <el-table :data="filteredTemplates" v-loading="templatesLoading">
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="templateCode" label="模板编码" width="150" />
            <el-table-column prop="templateName" label="模板名称" />
            <el-table-column prop="categoryName" label="所属类别" width="120" />
            <el-table-column prop="description" label="描述" />
            <el-table-column label="参数数量" width="100">
              <template #default="{ row }">
                <el-tag size="small" type="info">
                  {{ getParamCount(row.params) }}个
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status ? 'success' : 'danger'">
                  {{ row.status ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280">
              <template #default="{ row }">
                <el-button size="small" @click="viewTemplate(row)">
                  查看
                </el-button>
                <el-button size="small" @click="editTemplate(row)">
                  编辑
                </el-button>
                <el-button size="small" type="warning" @click="testTemplate(row)">
                  测试
                </el-button>
                <el-button size="small" type="danger" @click="deleteTemplate(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 类别编辑对话框 -->
    <CategoryDialog
      v-model="showCategoryDialog"
      :editing-category="editingCategory"
      @saved="loadCategories"
    />

    <!-- 模板编辑对话框 -->
    <TemplateDialog
      v-model="showTemplateDialog"
      :editing-template="editingTemplate"
      :categories="categories"
      @saved="loadTemplates"
    />

    <!-- 模板详情对话框 -->
    <TemplateDetailDialog
      v-model="showTemplateDetailDialog"
      :template="selectedTemplate"
    />

    <!-- 模板测试对话框 -->
    <TemplateTestDialog
      v-model="showTestDialog"
      :template="selectedTemplate"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { categoryApi } from '@/api/categories.ts'
import { templateApi } from '@/api/templates.ts'
import CategoryDialog from '@/components/CategoryDialog.vue'
import TemplateDialog from '@/components/TemplateDialog.vue'
import TemplateDetailDialog from '@/components/TemplateDetailDialog.vue'
import TemplateTestDialog from '@/components/TemplateTestDialog.vue'
import type { OperatorCategory, OperatorTemplate } from '@/types/api'

// 扩展模板类型，包含类别名称
interface TemplateWithCategory extends OperatorTemplate {
  categoryName: string
}

// 响应式数据
const activeTab = ref<'categories' | 'templates'>('categories')
const categoriesLoading = ref(false)
const templatesLoading = ref(false)

const categories = ref<OperatorCategory[]>([])
const templates = ref<TemplateWithCategory[]>([])
const categorySearch = ref('')
const templateSearch = ref('')
const selectedCategoryId = ref<number | ''>('')

const showCategoryDialog = ref(false)
const showTemplateDialog = ref(false)
const showTemplateDetailDialog = ref(false)
const showTestDialog = ref(false)

const editingCategory = ref<OperatorCategory | null>(null)
const editingTemplate = ref<OperatorTemplate | null>(null)
const selectedTemplate = ref<OperatorTemplate | null>(null)


// 计算属性
const filteredCategories = computed(() => {
  if (!categorySearch.value) return categories.value
  return categories.value.filter(cat => 
    cat.categoryName.toLowerCase().includes(categorySearch.value.toLowerCase()) ||
    cat.categoryCode.toLowerCase().includes(categorySearch.value.toLowerCase())
  )
})

const filteredTemplates = computed(() => {
  let result = templates.value
  
  if (templateSearch.value) {
    result = result.filter(template => 
      template.templateName.toLowerCase().includes(templateSearch.value.toLowerCase()) ||
      template.templateCode.toLowerCase().includes(templateSearch.value.toLowerCase())
    )
  }
  
  if (selectedCategoryId.value) {
    result = result.filter(template => template.categoryId === selectedCategoryId.value)
  }
  
  return result
})

// 方法
const handleTabChange = (tabName: string) => {
  if (tabName === 'categories') {
    loadCategories()
  } else if (tabName === 'templates') {
    loadTemplates()
  }
}

const loadCategories = async () => {
  try {
    categoriesLoading.value = true
    const response = await categoryApi.getCategories()
    if (response.success) {
      categories.value = response.data
    } else {
      ElMessage.error(response.message || '加载类别失败')
    }
  } catch (error) {
    console.error('加载类别失败:', error as Error)
    ElMessage.error('加载类别失败')
  } finally {
    categoriesLoading.value = false
  }
}

const loadTemplates = async () => {
  try {
    templatesLoading.value = true
    const response = await templateApi.getTemplates()
    if (response.success) {
      // 补充类别名称
      templates.value = response.data.map(template => {
        const category = categories.value.find(cat => cat.id === template.categoryId)
        return {
          ...template,
          categoryName: category ? category.categoryName : '未知类别'
        }
      })
    } else {
      ElMessage.error(response.message || '加载模板失败')
    }
  } catch (error) {
    console.error('加载模板失败:', error as Error)
    ElMessage.error('加载模板失败')
  } finally {
    templatesLoading.value = false
  }
}

const editCategory = (category: OperatorCategory) => {
  editingCategory.value = category
  showCategoryDialog.value = true
}

const addCategory = () => {
  editingCategory.value = null
  showCategoryDialog.value = true
}

const toggleCategoryStatus = async (category: OperatorCategory) => {
  try {
    const response = await categoryApi.toggleCategoryStatus(category.id!)
    if (response.success) {
      ElMessage.success(response.message)
      loadCategories()
    } else {
      ElMessage.error(response.message || '状态切换失败')
    }
  } catch (error) {
    console.error('状态切换失败:', error as Error)
    ElMessage.error('状态切换失败')
  }
}

const deleteCategory = async (category: OperatorCategory) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除类别"${category.categoryName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await categoryApi.deleteCategory(category.id!)
    if (response.success) {
      ElMessage.success('删除成功')
      loadCategories()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除类别失败:', error as Error)
      ElMessage.error('删除失败')
    }
  }
}

const addTemplate = () => {
  editingTemplate.value = null
  showTemplateDialog.value = true
}

const editTemplate = (template: OperatorTemplate) => {
  editingTemplate.value = template
  showTemplateDialog.value = true
}

const viewTemplate = (template: OperatorTemplate) => {
  selectedTemplate.value = template
  showTemplateDetailDialog.value = true
}

const deleteTemplate = async (template: OperatorTemplate) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除模板"${template.templateName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await templateApi.deleteTemplate(template.id!)
    if (response.success) {
      ElMessage.success('删除成功')
      loadTemplates()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败:', error as Error)
      ElMessage.error('删除失败')
    }
  }
}

const testTemplate = (template: OperatorTemplate) => {
  selectedTemplate.value = template
  showTestDialog.value = true
}

// 计算模板参数数量
const getParamCount = (params: any): number => {
  if (!params || typeof params !== 'object') {
    return 0
  }
  let totalCount = 0
  Object.values(params).forEach(paramData => {
    if (Array.isArray(paramData)) {
      // 如果是数组，统计数组长度
      totalCount += paramData.length
    } else if (paramData && typeof paramData === 'object') {
      // 如果是单个参数对象，计数为1
      totalCount += 1
    }
  })
  return totalCount
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.operator-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
}

.header-left h2 {
  margin: 0 0 8px 0;
  color: #303133;
}

.header-left p {
  margin: 0;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 12px;
}

.search-bar {
  margin-bottom: 20px;
}



@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .header-right {
    flex-direction: column;
    width: 100%;
  }
}
</style> 