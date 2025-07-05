import request from './index'
import type { 
  ApiResponse, 
  OperatorTemplate, 
  TemplateTestInput,
  ExecutorResult
} from '@/types/api'

/**
 * 算子模板相关API
 */
export const templateApi = {
  /**
   * 获取所有算子模板（包含详细信息和参数）
   */
  getTemplates: async (): Promise<ApiResponse<OperatorTemplate[]>> => {
    // 先获取所有模板
    const templatesResponse: ApiResponse<OperatorTemplate[]> = await request.get('/operator-templates')
    if (!templatesResponse.success) {
      return templatesResponse
    }
    
    // 为每个模板获取详细信息
    const templatesWithDetails = await Promise.all(
      templatesResponse.data.map(async (template: OperatorTemplate) => {
        try {
          const detailsResponse: ApiResponse<{ params: any }> = await request.get(`/operator-templates/${template.id}/details`)
          if (detailsResponse.success && detailsResponse.data) {
            return {
              ...template,
              params: detailsResponse.data.params || {}
            }
          }
          return {
            ...template,
            params: {}
          }
        } catch (error) {
          console.warn(`获取模板 ${template.id} 详情失败:`, error)
          return {
            ...template,
            params: {}
          }
        }
      })
    )
    
    return {
      ...templatesResponse,
      data: templatesWithDetails
    }
  },

  /**
   * 根据ID获取算子模板
   */
  getTemplateById: (id: number): Promise<ApiResponse<OperatorTemplate>> => {
    return request.get(`/operator-templates/${id}`)
  },

  /**
   * 根据类别获取算子模板
   */
  getTemplatesByCategory: (categoryId: number): Promise<ApiResponse<OperatorTemplate[]>> => {
    return request.get(`/operator-templates/category/${categoryId}`)
  },

  /**
   * 根据模板编码获取算子模板
   */
  getTemplateByCode: (templateCode: string): Promise<ApiResponse<OperatorTemplate>> => {
    return request.get(`/operator-templates/code/${templateCode}`)
  },

  /**
   * 创建算子模板
   */
  createTemplate: (template: Omit<OperatorTemplate, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<OperatorTemplate>> => {
    return request.post('/operator-templates', template)
  },

  /**
   * 更新算子模板
   */
  updateTemplate: (id: number, template: Partial<OperatorTemplate>): Promise<ApiResponse<OperatorTemplate>> => {
    return request.put(`/operator-templates/${id}`, template)
  },

  /**
   * 删除算子模板
   */
  deleteTemplate: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/operator-templates/${id}`)
  },

  /**
   * 启用/禁用算子模板
   */
  toggleTemplateStatus: (id: number): Promise<ApiResponse<OperatorTemplate>> => {
    return request.put(`/operator-templates/${id}/toggle-status`)
  },

  /**
   * 测试算子模板执行
   */
  testTemplate: (id: number, inputData: TemplateTestInput): Promise<ApiResponse<ExecutorResult>> => {
    return request.post(`/operator-templates/${id}/test`, inputData)
  },

  /**
   * 获取算子模板的参数定义
   */
  getTemplateParams: (id: number): Promise<ApiResponse<any>> => {
    return request.get(`/operator-templates/${id}/params`)
  },

  /**
   * 获取算子模板详细信息（包括参数）
   */
  getTemplateDetails: (id: number): Promise<ApiResponse<{ params: any }>> => {
    return request.get(`/operator-templates/${id}/details`)
  }
} 