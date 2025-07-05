import request from './index'
import type { ApiResponse, OperatorCategory } from '@/types/api'

/**
 * 算子类别相关API
 */
export const categoryApi = {
  /**
   * 获取所有算子类别
   */
  getCategories: (): Promise<ApiResponse<OperatorCategory[]>> => {
    return request.get('/operator-categories')
  },

  /**
   * 根据ID获取算子类别
   */
  getCategoryById: (id: number): Promise<ApiResponse<OperatorCategory>> => {
    return request.get(`/operator-categories/${id}`)
  },

  /**
   * 创建算子类别
   */
  createCategory: (category: Omit<OperatorCategory, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<OperatorCategory>> => {
    return request.post('/operator-categories', category)
  },

  /**
   * 更新算子类别
   */
  updateCategory: (id: number, category: Partial<OperatorCategory>): Promise<ApiResponse<OperatorCategory>> => {
    return request.put(`/operator-categories/${id}`, category)
  },

  /**
   * 删除算子类别
   */
  deleteCategory: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/operator-categories/${id}`)
  },

  /**
   * 启用/禁用算子类别
   */
  toggleCategoryStatus: (id: number): Promise<ApiResponse<OperatorCategory>> => {
    return request.put(`/operator-categories/${id}/toggle-status`)
  }
} 