// WriteAPI - 写作服务

// 写作 API 类型定义
export interface WriteRequest {
    text: string
    instruction: 'polish' | 'expand' | 'continue' | 'fix_grammar'
    context?: string
}

// 新版写作请求（支持文体和模板）
export interface WritingProcessRequest {
    text: string
    genre: string
    action: string
    context?: string
    templateId?: number
}

// 文体类型
export interface GenreOption {
    code: string
    name: string
    description: string
}

// 动作类型
export interface ActionOption {
    code: string
    name: string
    description: string
}

// 提示词模板
export interface PromptTemplate {
    id: number
    name: string
    genre: string
    genreName: string
    action: string
    actionName: string
    promptContent: string
    isBuiltin: boolean
    userId: number | null
    description: string
    sortOrder: number
    enabled: boolean
    createTime: string
    updateTime: string
}

// 创建模板请求
export interface CreateTemplateRequest {
    name: string
    genre: string
    action: string
    promptContent: string
    description?: string
    sortOrder?: number
    enabled: boolean
}

// 写作 API 服务 - 对接 Python 后端 /api/v1/write/process
export const writeAPI = {
    /**
     * 处理写作请求 (流式响应)
     * Python Backend: POST /api/v1/write/process
     * @param params 请求参数
     * @param onChunk 每次接收到内容时的回调
     * @param onDone 完成时的回调
     * @param onError 错误时的回调
     */
    async processStream(
        params: WriteRequest,
        onChunk: (text: string) => void,
        onDone: () => void,
        onError: (error: Error) => void
    ): Promise<void> {
        const token = localStorage.getItem('token') || ''

        try {
            // 直接使用 Python AI 服务地址
            const response = await fetch('http://localhost:8000/api/v1/write/process', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(params)
            })

            if (!response.ok) {
                throw new Error(`API Error: ${response.status}`)
            }

            const reader = response.body?.getReader()
            const decoder = new TextDecoder()

            if (!reader) {
                throw new Error('Failed to get response reader')
            }

            let buffer = ''

            while (true) {
                const { done, value } = await reader.read()
                if (done) break

                buffer += decoder.decode(value, { stream: true })
                const lines = buffer.split('\n')
                buffer = lines.pop() || ''

                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        const data = line.slice(5).trim()
                        if (data === '[DONE]') {
                            onDone()
                            return
                        }
                        // 处理转义的换行符
                        const text = data.replace(/\\n/g, '\n')
                        onChunk(text)
                    }
                }
            }

            // 处理剩余 buffer
            if (buffer.startsWith('data:')) {
                const data = buffer.slice(5).trim()
                if (data !== '[DONE]') {
                    const text = data.replace(/\\n/g, '\n')
                    onChunk(text)
                }
            }
            onDone()
        } catch (e) {
            onError(e instanceof Error ? e : new Error(String(e)))
        }
    }
}

export default writeAPI


// 模板管理 API
export const templateAPI = {
    /**
     * 获取文体类型列表
     */
    async getGenres(): Promise<GenreOption[]> {
        const token = localStorage.getItem('token') || ''
        const response = await fetch('http://localhost:8080/api/writing/templates/genres', {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        const data = await response.json()
        return data.data
    },

    /**
     * 获取动作类型列表
     */
    async getActions(): Promise<ActionOption[]> {
        const token = localStorage.getItem('token') || ''
        const response = await fetch('http://localhost:8080/api/writing/templates/actions', {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        const data = await response.json()
        return data.data
    },

    /**
     * 查询模板列表
     */
    async listTemplates(genre?: string, action?: string): Promise<PromptTemplate[]> {
        const token = localStorage.getItem('token') || ''
        const params = new URLSearchParams()
        if (genre) params.append('genre', genre)
        if (action) params.append('action', action)
        
        const url = `http://localhost:8080/api/writing/templates${params.toString() ? '?' + params.toString() : ''}`
        const response = await fetch(url, {
            headers: { 'Authorization': `Bearer ${token}` }
        })
        const data = await response.json()
        return data.data
    },

    /**
     * 创建自定义模板
     */
    async createTemplate(request: CreateTemplateRequest): Promise<PromptTemplate> {
        const token = localStorage.getItem('token') || ''
        const response = await fetch('http://localhost:8080/api/writing/templates', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify(request)
        })
        const data = await response.json()
        return data.data
    },

    /**
     * 删除自定义模板
     */
    async deleteTemplate(id: number): Promise<void> {
        const token = localStorage.getItem('token') || ''
        await fetch(`http://localhost:8080/api/writing/templates/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        })
    },

    /**
     * 使用模板进行写作辅助（流式响应）
     */
    async processWithTemplate(
        request: WritingProcessRequest,
        onChunk: (text: string) => void,
        onDone: () => void,
        onError: (error: Error) => void
    ): Promise<void> {
        const token = localStorage.getItem('token') || ''

        try {
            const response = await fetch('http://localhost:8080/api/writing/process-with-template', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(request)
            })

            if (!response.ok) {
                throw new Error(`API Error: ${response.status}`)
            }

            const reader = response.body?.getReader()
            const decoder = new TextDecoder()

            if (!reader) {
                throw new Error('Failed to get response reader')
            }

            let buffer = ''

            while (true) {
                const { done, value } = await reader.read()
                if (done) break

                buffer += decoder.decode(value, { stream: true })
                const lines = buffer.split('\n')
                buffer = lines.pop() || ''

                for (const line of lines) {
                    if (line.startsWith('data:')) {
                        const data = line.slice(5).trim()
                        if (data === '[DONE]') {
                            onDone()
                            return
                        }
                        const text = data.replace(/\\n/g, '\n')
                        onChunk(text)
                    }
                }
            }

            if (buffer.startsWith('data:')) {
                const data = buffer.slice(5).trim()
                if (data !== '[DONE]') {
                    const text = data.replace(/\\n/g, '\n')
                    onChunk(text)
                }
            }
            onDone()
        } catch (e) {
            onError(e instanceof Error ? e : new Error(String(e)))
        }
    }
}
