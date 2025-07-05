import request from './index'
import type { ApiResponse, WorkflowNode, WorkflowNodeParam } from '@/types/api'

/**
 * 工作流节点相关API
 */
export const nodeApi = {
  /**
   * 获取工作流的所有节点
   */
  getNodesByWorkflow: (workflowId: number): Promise<ApiResponse<WorkflowNode[]>> => {
    return request.get(`/workflow-nodes/workflow/${workflowId}`)
  },

  /**
   * 根据ID获取工作流节点
   */
  getNodeById: (id: number): Promise<ApiResponse<WorkflowNode>> => {
    return request.get(`/workflow-nodes/${id}`)
  },

  /**
   * 创建工作流节点
   */
  createNode: (node: Omit<WorkflowNode, 'id' | 'createTime' | 'updateTime'>): Promise<ApiResponse<WorkflowNode>> => {
    return request.post('/workflow-nodes', node)
  },

  /**
   * 更新工作流节点
   */
  updateNode: (id: number, node: Partial<WorkflowNode>): Promise<ApiResponse<WorkflowNode>> => {
    return request.put(`/workflow-nodes/${id}`, node)
  },

  /**
   * 删除工作流节点
   */
  deleteNode: (id: number): Promise<ApiResponse<void>> => {
    return request.delete(`/workflow-nodes/${id}`)
  },

  /**
   * 更新节点位置
   */
  updateNodePosition: (id: number, position: { positionX: number; positionY: number }): Promise<ApiResponse<WorkflowNode>> => {
    return request.put(`/workflow-nodes/${id}/position`, position)
  },

  /**
   * 获取节点的参数配置
   */
  getNodeParams: (id: number): Promise<ApiResponse<WorkflowNodeParam[]>> => {
    return request.get(`/workflow-nodes/${id}/params`)
  },

  /**
   * 更新节点的参数配置
   */
  updateNodeParams: (id: number, params: WorkflowNodeParam[]): Promise<ApiResponse<WorkflowNodeParam[]>> => {
    return request.put(`/workflow-nodes/${id}/params`, params)
  }
} 