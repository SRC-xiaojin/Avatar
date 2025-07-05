import request from './index'
import type { ApiResponse, TemplateParam } from '@/types/api'

/**
 * 算子参数相关API
 */
export const paramApi = {
  /**
   * 根据模板ID获取参数列表
   */
  getParamsByTemplateId: (templateId: number): Promise<ApiResponse<TemplateParam[]>> => {
    return request.get(`/operator-template-params/template/${templateId}`)
  },

  /**
   * 根据ID获取参数
   */
  getParamById: (id: number): Promise<ApiResponse<TemplateParam>> => {
    return request.get(`/operator-template-params/${id}`)
  },

  /**
   * 创建参数
   */
  createParam: (param: Omit<TemplateParam, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<TemplateParam>> => {
    return request.post('/operator-template-params', param)
  },

  /**
   * 更新参数
   */
  updateParam: (id: number, param: Partial<TemplateParam>): Promise<ApiResponse<TemplateParam>> => {
    return request.put(`/operator-template-params/${id}`, param)
  },

  /**
   * 删除参数
   */
  deleteParam: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/operator-template-params/${id}`)
  },

  /**
   * 批量保存模板参数
   */
  batchSaveParams: (templateId: number, params: TemplateParam[]): Promise<ApiResponse<TemplateParam[]>> => {
    return request.post(`/operator-template-params/template/${templateId}/batch`, params)
  }
} 