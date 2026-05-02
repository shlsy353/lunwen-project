<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
  rememberMe: false
})

// Captcha
const captchaCode = ref('')
const captchaCanvas = ref<HTMLCanvasElement>()

function generateCaptcha() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  let code = ''
  for (let i = 0; i < 4; i++) {
    code += chars.charAt(Math.floor(Math.random() * chars.length))
  }
  captchaCode.value = code
  drawCaptcha(code)
}

function drawCaptcha(code: string) {
  const canvas = captchaCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const w = canvas.width
  const h = canvas.height

  ctx.fillStyle = '#f0f4ff'
  ctx.fillRect(0, 0, w, h)

  for (let i = 0; i < 6; i++) {
    ctx.strokeStyle = `rgb(${Math.random()*200},${Math.random()*200},${Math.random()*200})`
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(Math.random() * w, Math.random() * h)
    ctx.lineTo(Math.random() * w, Math.random() * h)
    ctx.stroke()
  }

  for (let i = 0; i < code.length; i++) {
    ctx.font = `bold ${22 + Math.random() * 6}px "Courier New"`
    ctx.fillStyle = `rgb(${30+Math.random()*60},${40+Math.random()*80},${100+Math.random()*100})`
    ctx.translate(14 + i * 22, 28)
    ctx.rotate((Math.random() - 0.5) * 0.4)
    ctx.fillText(code.charAt(i), 0, 0)
    ctx.setTransform(1, 0, 0, 1, 0, 0)
  }

  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = `rgba(${Math.random()*150},${Math.random()*150},${Math.random()*150},0.3)`
    ctx.beginPath()
    ctx.arc(Math.random() * w, Math.random() * h, 1 + Math.random() * 2, 0, Math.PI * 2)
    ctx.fill()
  }
}

function refreshCaptcha() {
  generateCaptcha()
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await request.post('/user/login', {
      username: loginForm.username,
      password: loginForm.password
    })
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('role', res.data.role)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('user', JSON.stringify(res.data))

    if (loginForm.rememberMe) {
      localStorage.setItem('remembered_username', loginForm.username)
    } else {
      localStorage.removeItem('remembered_username')
    }

    ElMessage.success('登录成功')

    switch (res.data.role) {
      case 'ADMIN':
        router.push('/admin')
        break
      case 'TEACHER':
        router.push('/teacher')
        break
      case 'LEADER':
        router.push('/teacher/dashboard')
        break
      case 'STUDENT':
        router.push('/student/dashboard')
        break
      default:
        router.push('/')
    }
  } catch (error) {
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  generateCaptcha()
  const remembered = localStorage.getItem('remembered_username')
  if (remembered) {
    loginForm.username = remembered
    loginForm.rememberMe = true
  }
})
</script>

<template>
  <div class="login-page">
    <!-- Left: Campus Image & Branding (65%) -->
    <div class="left-panel">
      <div class="left-gradient"></div>
      <div class="left-vignette"></div>
      <div class="left-content">
        <!-- Logo & Motto -->
        <div class="top-brand">
          <div class="logo-circle">
            <svg viewBox="0 0 48 48" width="26" height="26">
              <rect x="8" y="20" width="32" height="20" rx="2" fill="none" stroke="white" stroke-width="2"/>
              <polygon points="24,6 12,20 36,20" fill="none" stroke="white" stroke-width="2"/>
              <line x1="24" y1="20" x2="24" y2="32" stroke="white" stroke-width="2"/>
              <line x1="18" y1="32" x2="30" y2="32" stroke="white" stroke-width="2"/>
            </svg>
          </div>
          <div class="motto">
            <span class="motto-cn">明德求真 博学笃行</span>
            <span class="motto-en">MINGDE · QIUZHEN · BOXUE · DUXING</span>
          </div>
        </div>

        <!-- Center Title -->
        <div class="center-title">
          <h1>学校竞赛管理系统</h1>
          <p class="subtitle">规范竞赛管理 · 提升竞赛水平 · 激发创新活力</p>
        </div>

        <!-- Decorative Icons -->
        <div class="deco-icons">
          <svg viewBox="0 0 48 48" width="40" height="40" class="deco-icon" opacity="0.2">
            <path d="M16 8h16v6a8 8 0 0 1-16 0V8z" fill="none" stroke="white" stroke-width="1.8"/>
            <path d="M12 8h-4a2 2 0 0 0-2 2v2a4 4 0 0 0 4 4h2" fill="none" stroke="white" stroke-width="1.8"/>
            <path d="M36 8h4a2 2 0 0 1 2 2v2a4 4 0 0 1-4 4h-2" fill="none" stroke="white" stroke-width="1.8"/>
            <line x1="24" y1="24" x2="24" y2="34" stroke="white" stroke-width="1.8"/>
            <line x1="16" y1="34" x2="32" y2="34" stroke="white" stroke-width="1.8"/>
            <line x1="20" y1="40" x2="28" y2="40" stroke="white" stroke-width="1.8"/>
          </svg>
          <svg viewBox="0 0 48 48" width="34" height="34" class="deco-icon" opacity="0.15" style="margin-left: 90px; margin-top: -10px;">
            <path d="M8 28l8-4 8-8 12-4-4 12-8 8-4 8-4-8-8-4z" fill="none" stroke="white" stroke-width="1.8"/>
          </svg>
          <svg viewBox="0 0 48 48" width="36" height="36" class="deco-icon" opacity="0.18" style="margin-left: 40px; margin-top: 60px;">
            <circle cx="24" cy="18" r="8" fill="none" stroke="white" stroke-width="1.8"/>
            <polygon points="24,28 20,38 24,34 28,38" fill="none" stroke="white" stroke-width="1.8"/>
          </svg>
        </div>

        <!-- Bottom Features -->
        <div class="bottom-features">
          <div class="feature-item">
            <svg viewBox="0 0 24 24" width="16" height="16">
              <rect x="3" y="11" width="18" height="10" rx="2" fill="none" stroke="white" stroke-width="1.8"/>
              <path d="M7 11V7a5 5 0 0 1 10 0v4" fill="none" stroke="white" stroke-width="1.8"/>
            </svg>
            <span>安全可靠</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" width="16" height="16">
              <circle cx="12" cy="12" r="9" fill="none" stroke="white" stroke-width="1.8"/>
              <polyline points="12,7 12,12 16,14" fill="none" stroke="white" stroke-width="1.8"/>
            </svg>
            <span>高效便捷</span>
          </div>
          <div class="feature-item">
            <svg viewBox="0 0 24 24" width="16" height="16">
              <path d="M12 2L15 9H22L16 14L18 21L12 17L6 21L8 14L2 9H9Z" fill="none" stroke="white" stroke-width="1.8"/>
            </svg>
            <span>服务成长</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Right: Login Card (35%) -->
    <div class="right-panel">
      <div class="login-card">
        <div class="card-header">
          <div class="avatar-icon">
            <svg viewBox="0 0 24 24" width="24" height="24">
              <rect x="5" y="11" width="14" height="10" rx="2" fill="none" stroke="#409eff" stroke-width="1.8"/>
              <path d="M8 11V7a4 4 0 0 1 8 0v4" fill="none" stroke="#409eff" stroke-width="1.8"/>
              <circle cx="12" cy="16" r="1.5" fill="#409eff"/>
            </svg>
          </div>
          <h2>登录</h2>
          <p class="welcome-text">欢迎使用学校竞赛管理系统</p>
        </div>

        <el-form :model="loginForm" class="login-form">
          <el-form-item>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              clearable
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <svg viewBox="0 0 24 24" width="16" height="16" class="input-icon">
                  <circle cx="12" cy="8" r="4" fill="none" stroke="currentColor" stroke-width="1.8"/>
                  <path d="M4 22c0-4.4 3.6-8 8-8s8 3.6 8 8" fill="none" stroke="currentColor" stroke-width="1.8"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
              @keyup.enter="handleLogin"
            >
              <template #prefix>
                <svg viewBox="0 0 24 24" width="16" height="16" class="input-icon">
                  <rect x="5" y="11" width="14" height="10" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/>
                  <path d="M8 11V7a4 4 0 0 1 8 0v4" fill="none" stroke="currentColor" stroke-width="1.8"/>
                </svg>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <div class="captcha-row">
              <el-input
                v-model="loginForm.captcha"
                placeholder="请输入验证码"
                size="large"
                maxlength="4"
                class="captcha-input"
                @keyup.enter="handleLogin"
              />
              <canvas
                ref="captchaCanvas"
                width="100"
                height="38"
                class="captcha-img"
                @click="refreshCaptcha"
                title="点击刷新验证码"
              />
              <span class="captcha-hint" @click="refreshCaptcha">换一张</span>
            </div>
          </el-form-item>

          <div class="form-options">
            <label class="remember-me">
              <input type="checkbox" v-model="loginForm.rememberMe" />
              <span>记住我</span>
            </label>
            <a href="javascript:;" class="forgot-link" @click="ElMessage.info('请联系管理员重置密码')">忘记密码？</a>
          </div>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-btn"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="divider">
          <span class="divider-text">其他登录方式</span>
        </div>

        <div class="account-hints">
          <span>管理员：admin</span>
          <span class="sep">|</span>
          <span>教师：teacher1</span>
          <span class="sep">|</span>
          <span>学生：student1</span>
        </div>

        <div class="register-entry">
          没有账号？<router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>

      <div class="footer">
        <p>&copy; 2026 学校竞赛管理系统 版权所有</p>
        <p class="browser-hint">建议使用 Chrome / Edge / Firefox 浏览器访问</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-page {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", Roboto, sans-serif;
}

/* ================================
   LEFT PANEL (65%)
   ================================ */
.left-panel {
  flex: 0 0 65%;
  position: relative;
  background:
    url('https://images.unsplash.com/photo-1562774053-701939374585?w=1400&q=85&fm=jpg') center/cover no-repeat;
  overflow: hidden;
}

/* Gradient overlay: left dark → right lighter */
.left-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    112deg,
    rgba(16, 40, 82, 0.92) 0%,
    rgba(30, 65, 125, 0.72) 35%,
    rgba(42, 82, 152, 0.45) 65%,
    rgba(60, 100, 170, 0.25) 100%
  );
  pointer-events: none;
  z-index: 1;
}

/* Vignette effect */
.left-vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(
    ellipse 70% 60% at 50% 50%,
    transparent 30%,
    rgba(0, 0, 0, 0.35) 100%
  );
  pointer-events: none;
  z-index: 2;
}

.left-content {
  position: relative;
  z-index: 3;
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 44px 56px;
  color: white;
}

/* Top brand */
.top-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-circle {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 2px solid rgba(255,255,255,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(255,255,255,0.06);
  backdrop-filter: blur(4px);
}

.motto {
  display: flex;
  flex-direction: column;
}

.motto-cn {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  line-height: 1.4;
  text-shadow: 0 1px 8px rgba(0,0,0,0.25);
}

.motto-en {
  font-size: 9px;
  opacity: 0.45;
  letter-spacing: 2px;
  margin-top: 1px;
}

/* Center title */
.center-title {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding-bottom: 80px;
}

.center-title h1 {
  font-size: 48px;
  font-weight: 700;
  letter-spacing: 6px;
  text-shadow:
    0 2px 24px rgba(0,0,0,0.4),
    0 0 60px rgba(100, 160, 255, 0.15);
  background: linear-gradient(135deg, #ffffff 0%, #c8ddff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 14px;
  margin-top: 18px;
  opacity: 0.55;
  letter-spacing: 8px;
  font-weight: 300;
  text-shadow: 0 1px 12px rgba(0,0,0,0.2);
}

/* Decorative icons */
.deco-icons {
  position: absolute;
  right: 80px;
  top: 28%;
  pointer-events: none;
}

/* Bottom features */
.bottom-features {
  display: flex;
  justify-content: center;
  gap: 64px;
  padding-bottom: 6px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 7px;
  font-size: 13px;
  opacity: 0.7;
  letter-spacing: 1px;
  text-shadow: 0 1px 6px rgba(0,0,0,0.15);
}

/* ================================
   RIGHT PANEL (35%)
   ================================ */
.right-panel {
  flex: 0 0 35%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #eef0f4;
  padding: 40px 36px;
  min-width: 0;
}

/* Glass card — "浮"在页面上 */
.login-card {
  width: 100%;
  max-width: 400px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  padding: 32px 28px 24px;
  box-shadow:
    0 2px 4px rgba(0,0,0,0.02),
    0 8px 24px rgba(0,0,0,0.06),
    0 24px 60px rgba(0,0,0,0.04);
  border: 1px solid rgba(255,255,255,0.7);
  animation: cardFadeIn 0.5s ease-out;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(18px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Card header */
.card-header {
  text-align: center;
  margin-bottom: 28px;
}

.avatar-icon {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ecf5ff 0%, #d9ecff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  box-shadow: 0 2px 10px rgba(64,158,255,0.12);
}

.card-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #1d2129;
  margin-bottom: 4px;
  letter-spacing: 1px;
}

.welcome-text {
  font-size: 13px;
  color: #a9aeb8;
  letter-spacing: 0.3px;
}

/* Form */
.login-form {
  margin-top: 0;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding-left: 12px;
  background: #f5f7fa;
  box-shadow: none;
  transition: all 0.25s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  background: #f0f2f5;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: white;
  box-shadow: 0 0 0 2px #409eff;
}

.login-form :deep(.el-input__inner) {
  height: 44px;
  font-size: 14px;
}

.input-icon {
  color: rgba(153, 153, 153, 0.5);
  transition: color 0.25s;
}

.login-form :deep(.el-input__wrapper.is-focus) .input-icon {
  color: #409eff;
}

/* Captcha */
.captcha-row {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
}

.captcha-input {
  flex: 1;
  min-width: 0;
}

.captcha-img {
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid #e5e6eb;
  flex-shrink: 0;
  transition: opacity 0.2s, box-shadow 0.2s;
}

.captcha-img:hover {
  opacity: 0.85;
  box-shadow: 0 1px 6px rgba(0,0,0,0.06);
}

.captcha-hint {
  font-size: 13px;
  color: #409eff;
  cursor: pointer;
  white-space: nowrap;
  user-select: none;
  transition: color 0.2s;
}

.captcha-hint:hover {
  color: #1d7bcf;
}

/* Form options */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: -4px 0 20px;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #86909c;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s;
}

.remember-me:hover {
  color: #4e5969;
}

.remember-me input {
  accent-color: #409eff;
  width: 14px;
  height: 14px;
}

.forgot-link {
  font-size: 13px;
  color: #a9aeb8;
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #409eff;
}

/* Login button — 视觉焦点 */
.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, #2d7bce 0%, #409eff 50%, #5aafff 100%);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  letter-spacing: 3px;
  position: relative;
  overflow: hidden;
}

.login-btn::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 50%;
  background: linear-gradient(180deg, rgba(255,255,255,0.15) 0%, transparent 100%);
  border-radius: 10px 10px 0 0;
  pointer-events: none;
}

.login-btn:hover {
  background: linear-gradient(135deg, #3d8bdb 0%, #5aafff 60%, #72bdff 100%);
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(64,158,255,0.45);
}

.login-btn:active {
  transform: translateY(0) scale(0.985);
  box-shadow: 0 4px 12px rgba(64,158,255,0.3);
}

/* Divider */
.divider {
  display: flex;
  align-items: center;
  margin: 24px 0 16px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e8e9ed;
}

.divider-text {
  padding: 0 14px;
  font-size: 12px;
  color: #c9cdd4;
  white-space: nowrap;
  letter-spacing: 0.5px;
  margin-top: -1px;
}

/* Account hints (text only) */
.account-hints {
  text-align: center;
  font-size: 13px;
  color: #b0b4bc;
  margin-bottom: 22px;
  letter-spacing: 0.8px;
}

.sep {
  color: rgba(200, 204, 212, 0.4);
  margin: 0 6px;
  font-weight: 200;
}

/* Register entry */
.register-entry {
  text-align: center;
  font-size: 13px;
  color: #86909c;
  letter-spacing: 0.3px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s, border-bottom 0.2s;
  border-bottom: 1px solid transparent;
  padding-bottom: 1px;
}

.register-link:hover {
  color: #1d7bcf;
  border-bottom-color: currentColor;
}

/* Footer */
.footer {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #b0b4bc;
  line-height: 1.8;
  letter-spacing: 0.3px;
}

.browser-hint {
  font-size: 11px;
  color: #c9cdd4;
  margin-top: 2px;
}

/* ================================
   RESPONSIVE
   ================================ */
@media (max-width: 1024px) {
  .left-panel {
    flex: 0 0 55%;
  }
  .right-panel {
    flex: 0 0 45%;
  }
  .center-title h1 {
    font-size: 36px;
  }
  .subtitle {
    font-size: 13px;
    letter-spacing: 4px;
  }
}

@media (max-width: 768px) {
  .left-panel {
    display: none;
  }
  .right-panel {
    flex: 1;
    padding: 24px 16px;
    background: linear-gradient(135deg, #1a3a6b 0%, #2d5aa0 100%);
    justify-content: flex-start;
    padding-top: 60px;
  }
  .login-card {
    background: rgba(255,255,255,0.95);
    max-width: 100%;
  }
  .footer {
    color: rgba(255,255,255,0.5);
  }
  .browser-hint {
    color: rgba(255,255,255,0.35);
  }
}
</style>
