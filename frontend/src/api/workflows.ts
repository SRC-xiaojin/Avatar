import request from './index'
import type { ApiResponse, Workflow, WorkflowExecution, WorkflowConnection } from '@/types/api'

/**
 * 工作流相关API
 */
export const workflowApi = {
  /**
   * 获取所有工作流
   */
  getWorkflows: (): Promise<ApiResponse<Workflow[]>> => {
    return request.get('/workflows')
  },

  /**
   * 根据ID获取工作流
   */
  getWorkflowById: (id: number): Promise<ApiResponse<Workflow>> => {
    return request.get(`/workflows/${id}`)
  },

  /**
   * 创建工作流
   */
  createWorkflow: (workflow: Omit<Workflow, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<Workflow>> => {
    return request.post('/workflows', workflow)
  },

  /**
   * 更新工作流
   */
  updateWorkflow: (id: number, workflow: Partial<Workflow>): Promise<ApiResponse<Workflow>> => {
    return request.put(`/workflows/${id}`, workflow)
  },

  /**
   * 删除工作流
   */
  deleteWorkflow: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/workflows/${id}`)
  },

  /**
   * 执行工作流
   */
  executeWorkflow: (id: number, inputData: Record<string, any>): Promise<ApiResponse<WorkflowExecution>> => {
    return request.post(`/workflows/${id}/execute`, inputData)
  },

  /**
   * 验证工作流
   */
  validateWorkflow: (id: number): Promise<ApiResponse<{ valid: boolean; message?: string }>> => {
    return request.post(`/workflows/${id}/validate`)
  },

  /**
   * 获取工作流执行历史
   */
  getWorkflowHistory: (id: number): Promise<ApiResponse<WorkflowExecution[]>> => {
    return request.get(`/workflows/${id}/history`)
  },

  /**
   * 测试工作流
   */
  testWorkflow: (id: number, inputData: Record<string, any>): Promise<ApiResponse<any>> => {
    return request.post(`/workflows/${id}/test`, inputData)
  }
}

/**
 * 工作流连接相关API
 */
export const connectionApi = {
  /**
   * 获取工作流的所有连接
   */
  getConnectionsByWorkflow: (workflowId: number): Promise<ApiResponse<WorkflowConnection[]>> => {
    return request.get(`/workflow-connections/workflow/${workflowId}`)
  },

  /**
   * 根据ID获取工作流连接
   */
  getConnectionById: (id: number): Promise<ApiResponse<WorkflowConnection>> => {
    return request.get(`/workflow-connections/${id}`)
  },

  /**
   * 创建工作流连接
   */
  createConnection: (connection: Omit<WorkflowConnection, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<WorkflowConnection>> => {
    return request.post('/workflow-connections', connection)
  },

  /**
   * 批量创建工作流连接
   */
  createConnections: (connections: Omit<WorkflowConnection, 'id' | 'createTime' | 'updateTime'>[]): Promise<ApiResponse<WorkflowConnection[]>> => {
    return request.post('/workflow-connections/batch', connections)
  },

  /**
   * 更新工作流连接
   */
  updateConnection: (id: number, connection: Partial<WorkflowConnection>): Promise<ApiResponse<WorkflowConnection>> => {
    return request.put(`/workflow-connections/${id}`, connection)
  },

  /**
   * 删除工作流连接
   */
  deleteConnection: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/workflow-connections/${id}`)
  },

  /**
   * 删除工作流的所有连接
   */
  deleteConnectionsByWorkflow: (workflowId: number): Promise<ApiResponse<void>> => {
    return request.delete(`/workflow-connections/workflow/${workflowId}`)
  }
} 