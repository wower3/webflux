<template>
  <div class="home-container">
    <!-- 顶部导航 -->
    <header class="header">
      <div class="header-content">
        <div class="logo">
          <i class="fa fa-bar-chart"></i>
          <span>DataInsight</span>
        </div>
        <nav class="nav">
          <a href="#" class="nav-item active">首页</a>
          <a href="#" class="nav-item">产品</a>
          <a href="#" class="nav-item">解决方案</a>
          <a href="#" class="nav-item">文档</a>
        </nav>
        <div class="header-actions">
          <button class="btn-secondary">登录</button>
          <button class="btn-primary">免费试用</button>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <!-- Hero 区域 -->
      <section class="hero">
        <div class="hero-content">
          <h1 class="hero-title">智能数据分析平台</h1>
          <p class="hero-subtitle">
            通过 AI 驱动的对话式分析，快速洞察数据价值
          </p>
          <div class="hero-actions">
            <button class="btn-primary btn-large" @click="openChat">
              <i class="fa fa-comments"></i>
              开始分析
            </button>
            <button class="btn-outline btn-large">
              <i class="fa fa-play-circle"></i>
              观看演示
            </button>
          </div>
        </div>
        <div class="hero-visual">
          <div class="data-card">
            <div class="card-header">
              <span class="card-title">实时数据分析</span>
              <span class="card-badge">Live</span>
            </div>
            <div class="card-body">
              <div class="stat-grid">
                <div class="stat-item">
                  <div class="stat-value">12,458</div>
                  <div class="stat-label">总数据量</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value up">+23.5%</div>
                  <div class="stat-label">增长率</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">8.2s</div>
                  <div class="stat-label">响应时间</div>
                </div>
                <div class="stat-item">
                  <div class="stat-value">99.9%</div>
                  <div class="stat-label">可用性</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 特性区域 -->
      <section class="features">
        <h2 class="section-title">核心功能</h2>
        <div class="feature-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-line-chart"></i>
            </div>
            <h3 class="feature-title">实时图表生成</h3>
            <p class="feature-desc">支持折线图、柱状图、饼图等多种图表类型，实时渲染</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-bolt"></i>
            </div>
            <h3 class="feature-title">流式响应</h3>
            <p class="feature-desc">采用 SSE 技术，实现打字机效果的实时数据传输</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-shield"></i>
            </div>
            <h3 class="feature-title">数据安全</h3>
            <p class="feature-desc">企业级数据加密，保障您的数据隐私安全</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <i class="fa fa-plug"></i>
            </div>
            <h3 class="feature-title">易于集成</h3>
            <p class="feature-desc">提供 RESTful API，轻松集成到现有系统</p>
          </div>
        </div>
      </section>

      <!-- 数据展示区域 -->
      <section class="data-preview">
        <h2 class="section-title">数据预览</h2>
        <div class="preview-grid">
          <div class="preview-card">
            <div class="preview-header">
              <h4>销售趋势</h4>
              <span class="trend up">+15.3%</span>
            </div>
            <div class="preview-chart">
              <div class="mini-bar">
                <div class="bar" style="height: 40%"></div>
                <div class="bar" style="height: 60%"></div>
                <div class="bar" style="height: 45%"></div>
                <div class="bar" style="height: 80%"></div>
                <div class="bar" style="height: 70%"></div>
                <div class="bar" style="height: 90%"></div>
                <div class="bar" style="height: 85%"></div>
              </div>
            </div>
          </div>
          <div class="preview-card">
            <div class="preview-header">
              <h4>用户分布</h4>
              <span class="count">8,432</span>
            </div>
            <div class="preview-chart">
              <div class="mini-donut">
                <svg viewBox="0 0 36 36">
                  <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                        fill="none" stroke="#e5e7eb" stroke-width="3"/>
                  <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                        fill="none" stroke="#667eea" stroke-width="3"
                        stroke-dasharray="70, 100"/>
                </svg>
              </div>
            </div>
          </div>
          <div class="preview-card">
            <div class="preview-header">
              <h4>转化率</h4>
              <span class="rate">4.8%</span>
            </div>
            <div class="preview-chart">
              <div class="mini-line">
                <svg viewBox="0 0 100 40">
                  <polyline points="0,35 20,25 40,30 60,15 80,20 100,5"
                            fill="none" stroke="#10a37f" stroke-width="2"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <p>&copy; 2024 DataInsight. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { defineEmits } from 'vue'

const emit = defineEmits<{
  openChat: []
}>()

const openChat = () => {
  emit('openChat')
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f8fafc;
}

/* 顶部导航 */
.header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.logo i {
  color: #667eea;
  font-size: 24px;
}

.nav {
  display: flex;
  gap: 32px;
}

.nav-item {
  color: #6b7280;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.2s;
}

.nav-item:hover,
.nav-item.active {
  color: #667eea;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 按钮样式 */
.btn-primary {
  background: #667eea;
  color: #fff;
  border: none;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #5a67d8;
}

.btn-secondary {
  background: #fff;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  border-color: #9ca3af;
  background: #f9fafb;
}

.btn-outline {
  background: transparent;
  color: #374151;
  border: 1px solid #d1d5db;
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-outline:hover {
  border-color: #667eea;
  color: #667eea;
}

.btn-large {
  padding: 12px 28px;
  font-size: 16px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

/* 主内容 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
}

/* Hero 区域 */
.hero {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  align-items: center;
  padding: 80px 0;
}

.hero-title {
  font-size: 48px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.2;
  margin-bottom: 20px;
}

.hero-subtitle {
  font-size: 18px;
  color: #6b7280;
  margin-bottom: 32px;
  max-width: 480px;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

/* 数据卡片 */
.data-card {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.card-badge {
  background: #d1fae5;
  color: #065f46;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f9fafb;
  border-radius: 12px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-value.up {
  color: #10a37f;
}

.stat-label {
  font-size: 12px;
  color: #6b7280;
}

/* 特性区域 */
.features {
  padding: 80px 0;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  color: #1f2937;
  text-align: center;
  margin-bottom: 48px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.feature-card {
  background: #fff;
  padding: 32px 24px;
  border-radius: 12px;
  text-align: center;
  transition: transform 0.2s, box-shadow 0.2s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.feature-desc {
  font-size: 14px;
  color: #6b7280;
  line-height: 1.6;
}

/* 数据预览 */
.data-preview {
  padding: 80px 0;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.preview-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.preview-header h4 {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.trend.up {
  color: #10a37f;
  font-size: 14px;
  font-weight: 600;
}

.count, .rate {
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.preview-chart {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-bar {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  height: 60px;
}

.bar {
  width: 24px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  border-radius: 4px;
}

.mini-donut svg {
  width: 80px;
  height: 80px;
}

.mini-line svg {
  width: 100%;
  height: 60px;
}

/* 页脚 */
.footer {
  background: #1f2937;
  padding: 24px 0;
  margin-top: 60px;
}

.footer-content {
  text-align: center;
  color: #9ca3af;
  font-size: 14px;
}
</style>
