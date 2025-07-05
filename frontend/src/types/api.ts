// 通用API响应类型
export interface ApiResponse<T = any> {
  success: boolean;
  data: T;
  message?: string;
  code?: string;
}

// 分页响应类型
export interface PageResponse<T> extends ApiResponse<T[]> {
  total: number;
  page: number;
  size: number;
}

// 算子模板类型
export interface OperatorTemplate {
  id?: number;
  categoryId: number;
  categoryName?: string;
  templateCode: string;
  templateName: string;
  description?: string;
  executorClass?: string;
  executorMethod?: string;
  configSchema?: string;
  inputSchema?: string;
  outputSchema?: string;
  status: boolean;
  isAsync?: boolean;
  timeoutSeconds?: number;
  createTime?: string;
  updateTime?: string;
  params?: TemplateParam[];
}

// 模板参数类型
export interface TemplateParam {
  id?: number;
  tempId?: string;
  templateId?: number;
  paramKey: string;
  paramName: string;
  paramType: 'string' | 'number' | 'boolean' | 'object' | 'array';
  paramCategory?: string;
  isRequired: boolean;
  defaultValue?: string;
  validationRules?: string;
  description?: string;
  createTime?: string;
  updateTime?: string;
}

// 算子类别类型
export interface OperatorCategory {
  id?: number;
  categoryName: string;
  categoryCode: string;
  description?: string;
  parentId?: number;
  sortOrder?: number;
  status: boolean;
  createTime?: string;
  updateTime?: string;
}

// 工作流类型
export interface Workflow {
  id?: number;
  workflowName: string;
  workflowCode: string;
  description?: string;
  status: string;
  nodeCount?: number;
  createdAt?: string;
  updatedAt?: string;
  createTime?: string;
  updateTime?: string;
}

// 工作流节点类型
export interface WorkflowNode {
  id?: number;
  workflowId: number;
  nodeCode: string;
  nodeName: string;
  templateId: number;
  positionX: number;
  positionY: number;
  nodeConfig?: string;
  status: boolean;
  createTime?: string;
  updateTime?: string;
}

// 工作流连接类型
export interface WorkflowConnection {
  id?: number;
  workflowId: number;
  sourceNodeId: number;
  targetNodeId: number;
  connectionType?: string;
  createTime?: string;
  updateTime?: string;
}

// 节点参数类型
export interface WorkflowNodeParam {
  id?: number;
  nodeId: number;
  paramKey: string;
  paramValue: string;
  createTime?: string;
  updateTime?: string;
}

// 工作流执行类型
export interface WorkflowExecution {
  id?: number;
  workflowId: number;
  executionStatus: 'pending' | 'running' | 'success' | 'failed';
  inputData?: string;
  outputData?: string;
  errorMessage?: string;
  startTime?: string;
  endTime?: string;
}

// 节点执行类型
export interface NodeExecution {
  id?: number;
  executionId: number;
  nodeId: number;
  executionStatus: 'pending' | 'running' | 'success' | 'failed';
  inputData?: string;
  outputData?: string;
  errorMessage?: string;
  startTime?: string;
  endTime?: string;
}

// 算子测试输入类型
export interface TemplateTestInput {
  inputData?: Record<string, any>;
  params?: Record<string, any>;
}

// 执行器状态枚举
export type ExecutorStatus = 
  | 'SUCCESS'
  | 'FAILED' 
  | 'TIMEOUT'
  | 'CANCELLED'
  | 'RUNNING'
  | 'PENDING'
  | 'SKIPPED'
  | 'UNKNOWN'

// 执行器结果类型
export interface ExecutorResult {
  // 执行状态
  status: ExecutorStatus;
  
  // 执行是否成功
  success: boolean;
  
  // 输出数据 - 支持Map和List结构（对应后端的FlexibleBean）
  outputData: Record<string, any> | Array<any>;
  
  // 错误信息
  errorMessage?: string;
  
  // 错误代码
  errorCode?: string;
  
  // 执行开始时间
  startTime?: string;
  
  // 执行结束时间
  endTime?: string;
  
  // 执行耗时（毫秒）
  executionTimeMs?: number;
  
  // 节点ID
  nodeId?: number;
  
  // 节点名称
  nodeName?: string;
  
  // 模板ID
  templateId?: number;
  
  // 模板名称
  templateName?: string;
  
  // 执行器类名
  executorClass?: string;
  
  // 执行器方法名
  executorMethod?: string;
  
  // 执行日志
  executionLog?: string;
  
  // 扩展属性
  metadata?: Record<string, any>;
}

// 算子类型定义
export interface Operator {
  id?: number;
  operatorName: string;
  operatorType: string;
  operatorCode: string;
  description?: string;
  configSchema?: string;
  inputSchema?: string;
  outputSchema?: string;
  status: boolean;
  createTime?: string;
  updateTime?: string;
}

export interface OperatorType {
  code: string;
  name: string;
  description?: string;
} 