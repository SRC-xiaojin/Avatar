import request from './index'
import type { ApiResponse, Operator, OperatorType } from '@/types/api'

/**
 * 算子相关API
 */
export const operatorApi = {
  /**
   * 获取所有算子
   */
  getOperators: (): Promise<ApiResponse<Operator[]>> => {
    return request.get('/operators')
  },

  /**
   * 根据ID获取算子
   */
  getOperatorById: (id: number): Promise<ApiResponse<Operator>> => {
    return request.get(`/operators/${id}`)
  },

  /**
   * 根据类型获取算子
   */
  getOperatorsByType: (type: string): Promise<ApiResponse<Operator[]>> => {
    return request.get(`/operators/type/${type}`)
  },

  /**
   * 搜索算子
   */
  searchOperators: (keyword: string): Promise<ApiResponse<Operator[]>> => {
    return request.get('/operators/search', {
      params: { keyword }
    })
  },

  /**
   * 创建算子
   */
  createOperator: (operator: Omit<Operator, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<Operator>> => {
    return request.post('/operators', operator)
  },

  /**
   * 更新算子
   */
  updateOperator: (id: number, operator: Partial<Operator>): Promise<ApiResponse<Operator>> => {
    return request.put(`/operators/${id}`, operator)
  },

  /**
   * 删除算子
   */
  deleteOperator: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/operators/${id}`)
  },

  /**
   * 验证算子配置
   */
  validateOperator: (id: number): Promise<ApiResponse<{ valid: boolean; message?: string }>> => {
    return request.post(`/operators/${id}/validate`)
  },

  /**
   * 执行算子
   */
  executeOperator: (id: number, inputData: Record<string, any>): Promise<ApiResponse<any>> => {
    return request.post(`/operators/${id}/execute`, inputData)
  },

  /**
   * 获取算子类型列表
   */
  getOperatorTypes: (): Promise<ApiResponse<OperatorType[]>> => {
    return request.get('/operators/types')
  }
} 