import request from './request'

export interface SensitiveWord {
  id: number
  word: string
  type: number
  enabled: number
  createTime: string
  updateTime: string
}

export interface SensitiveWordRequest {
  word: string
  type: number
  enabled?: number
}

export interface CheckTextRequest {
  text: string
}

export interface SensitiveWordMatch {
  word: string
  type: number
  startIndex: number
  endIndex: number
}

export interface CheckTextResponse {
  hasSensitive: boolean
  matches: SensitiveWordMatch[]
}

// 添加敏感词
export const addSensitiveWord = (data: SensitiveWordRequest) => {
  return request.post<SensitiveWord>('/sensitive/words', data)
}

// 更新敏感词
export const updateSensitiveWord = (id: number, data: SensitiveWordRequest) => {
  return request.put<SensitiveWord>(`/sensitive/words/${id}`, data)
}

// 删除敏感词
export const deleteSensitiveWord = (id: number) => {
  return request.delete(`/sensitive/words/${id}`)
}

// 批量删除敏感词
export const batchDeleteSensitiveWords = (ids: number[]) => {
  return request.delete('/sensitive/words/batch', { data: ids })
}

// 获取敏感词详情
export const getSensitiveWord = (id: number) => {
  return request.get<SensitiveWord>(`/sensitive/words/${id}`)
}

// 分页查询敏感词
export const pageSensitiveWords = (params: {
  current: number
  size: number
  keyword?: string
  type?: number
  enabled?: number
}) => {
  return request.get<{
    records: SensitiveWord[]
    total: number
    current: number
    size: number
  }>('/sensitive/words', { params })
}

// 检测文本中的敏感词
export const checkText = (data: CheckTextRequest) => {
  return request.post<CheckTextResponse>('/sensitive/check', data)
}

// 刷新敏感词缓存
export const refreshCache = () => {
  return request.post('/sensitive/refresh')
}
