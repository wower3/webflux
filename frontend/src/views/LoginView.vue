<template>
  <div class="login-page">
    <div class="login-card">
      <h2>AI 数据分析助手</h2>
      <p class="subtitle">请登录以继续</p>

      <div v-if="isRegister" class="form-group">
        <label>用户名</label>
        <input v-model="username" type="text" placeholder="请输入用户名" @keyup.enter="handleRegister" />
      </div>

      <div class="form-group">
        <label>密码</label>
        <input v-model="password" type="password" placeholder="请输入密码" @keyup.enter="handleSubmit" />
      </div>

      <button class="submit-btn" @click="handleSubmit" :disabled="loading || !username || !password">
        {{ loading ? '请求中...' : (isRegister ? '注册' : '登录') }}
      </button>

      <p class="switch-mode" @click="isRegister = !isRegister">
        {{ isRegister ? '已有账号？去登录' : '没有账号？去注册' }}
      </p>

      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')
const isRegister = ref(false)
const loading = ref(false)
const error = ref('')

const handleSubmit = async () => {
  if (loading.value || !username.value || !password.value) return

  loading.value = true
  error.value = ''

  try {
    const endpoint = isRegister.value ? '/api/auth/register' : '/api/auth/login'
    const res = await fetch(endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    const data = await res.json()

    if (res.ok) {
      localStorage.setItem('chat_token', data.token)
      localStorage.setItem('chat_username', data.username)
      router.push('/')
    } else {
      error.value = data.message || '操作失败'
    }
  } catch (e) {
    error.value = '网络错误'
  } finally {
    loading.value = false
  }
}

const handleRegister = () => {
  handleSubmit()
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: #fff;
  padding: 40px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  width: 380px;
  text-align: center;
}

.login-card h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #1f2937;
}

.subtitle {
  color: #6b7280;
  margin: 0 0 32px;
  font-size: 14px;
}

.form-group {
  margin-bottom: 20px;
  text-align: left;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.15);
}

.submit-btn {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.submit-btn:not(:disabled):hover {
  opacity: 0.9;
}

.switch-mode {
  margin-top: 20px;
  font-size: 14px;
  color: #667eea;
  cursor: pointer;
}

.switch-mode:hover {
  text-decoration: underline;
}

.error-msg {
  margin-top: 16px;
  color: #ef4444;
  font-size: 14px;
}
</style>
