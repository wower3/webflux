// API 接口 - 非流式版本
import type { ApiResponse, ChatRequest } from '@/types'
import axios from 'axios'

const BASE_URL = 'http://localhost:8001'

// 创建 axios 实例
const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 发送普通聊天请求
 */
export async function sendChat(request: ChatRequest): Promise<ApiResponse> {
  const response = await api.post('/api/chat', request)
  return response.data
}

/**
 * 回显模式
 */
export async function sendEcho(message: string): Promise<ApiResponse> {
  const response = await api.get('/api/chat/echo', {
    params: { message }
  })
  return response.data
}

/**
 * 测试文件模式
 */
export async function sendTest(file: string = 'test_content.txt'): Promise<ApiResponse> {
  const response = await api.get('/api/chat/test', {
    params: { file }
  })
  return response.data
}

/**
 * 消息处理模式
 */
export async function sendProcess(request: ChatRequest): Promise<ApiResponse> {
  const response = await api.post('/api/chat/process', request)
  return response.data
}
