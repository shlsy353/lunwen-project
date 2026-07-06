<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

function getHomePath(role: string) {
  switch (role) {
    case 'ADMIN':
      return '/admin'
    case 'TEACHER':
    case 'LEADER':
      return '/teacher/dashboard'
    case 'STUDENT':
      return '/student/dashboard'
    default:
      return '/'
  }
}

function saveLoginSession(data: Record<string, any>) {
  localStorage.setItem('token', data.token)
  localStorage.setItem('role', data.role)
  localStorage.setItem('username', data.username)
  localStorage.setItem('user', JSON.stringify(data))
}

const handleLogin = async () => {
  if (!loginForm.username.trim() || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const res = await request.post('/user/login', {
      username: loginForm.username.trim(),
      password: loginForm.password
    })

    saveLoginSession(res.data)

    if (loginForm.rememberMe) {
      localStorage.setItem('remembered_username', loginForm.username.trim())
    } else {
      localStorage.removeItem('remembered_username')
    }

    ElMessage.success('登录成功')
    router.push(getHomePath(res.data.role))
  } catch (error) {
    console.error('Login error:', error)
  } finally {
    loading.value = false
  }
}

function handleGithubLogin() {
  window.location.href = '/api/user/oauth/github/login'
}

function handleOAuthCallback() {
  const token = route.query.token
  const error = route.query.error

  if (typeof error === 'string' && error) {
    ElMessage.error(decodeURIComponent(error))
    router.replace('/login')
    return
  }

  if (typeof token !== 'string' || !token) return

  const data = {
    token,
    role: String(route.query.role || 'STUDENT'),
    username: String(route.query.username || ''),
    name: String(route.query.name || ''),
    avatar: String(route.query.avatar || ''),
    id: route.query.id ? Number(route.query.id) : undefined
  }

  saveLoginSession(data)
  ElMessage.success('GitHub 登录成功')
  router.replace(getHomePath(data.role))
}

onMounted(() => {
  const remembered = localStorage.getItem('remembered_username')
  if (remembered) {
    loginForm.username = remembered
    loginForm.rememberMe = true
  }
  handleOAuthCallback()
})
</script>

<template>
  <main class="login-page">
    <section class="login-shell" aria-label="登录">
      <header class="login-heading">
        <h1>高校学科竞赛管理系统</h1>
        <p>一站式管理竞赛发布、报名、评分与成果归档。</p>
      </header>

      <div class="login-card">
        <button type="button" class="oauth-button" @click="handleGithubLogin">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="currentColor"
              d="M12 .5a12 12 0 0 0-3.79 23.39c.6.11.82-.26.82-.58v-2.03c-3.34.73-4.04-1.42-4.04-1.42-.55-1.39-1.34-1.76-1.34-1.76-1.09-.75.08-.73.08-.73 1.2.08 1.84 1.24 1.84 1.24 1.07 1.83 2.8 1.3 3.49.99.11-.78.42-1.3.76-1.6-2.66-.3-5.47-1.33-5.47-5.93 0-1.31.47-2.38 1.24-3.22-.12-.3-.54-1.52.12-3.18 0 0 1.01-.32 3.3 1.23a11.4 11.4 0 0 1 6 0c2.29-1.55 3.3-1.23 3.3-1.23.66 1.66.24 2.88.12 3.18.77.84 1.24 1.91 1.24 3.22 0 4.61-2.81 5.62-5.49 5.92.43.37.81 1.1.81 2.22v3.29c0 .32.22.7.83.58A12 12 0 0 0 12 .5Z"
            />
          </svg>
          继续使用 GitHub
        </button>

        <div class="divider">
          <span>或者</span>
        </div>

        <el-form :model="loginForm" class="login-form">
          <el-form-item>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入您的用户名"
              size="large"
              clearable
              autocomplete="username"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入您的密码"
              size="large"
              show-password
              clearable
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-button
            class="submit-button"
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            继续登录
          </el-button>

          <div class="form-meta">
            <label class="remember">
              <input v-model="loginForm.rememberMe" type="checkbox" />
              <span>记住账号</span>
            </label>
            <button type="button" class="text-button" @click="ElMessage.info('请联系管理员重置密码')">
              忘记密码？
            </button>
          </div>
        </el-form>

        <p class="policy-copy">
          继续下去，即表示你同意平台使用规范，并确认妥善保管个人账号信息。
        </p>

        <p class="register-line">
          没有账号？
          <router-link to="/register">立即注册</router-link>
        </p>
      </div>
    </section>
  </main>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.login-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  place-items: start center;
  overflow: hidden;
  padding: 146px 24px 48px;
  color: #0d0d0d;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Microsoft YaHei", Arial, sans-serif;
  background:
    radial-gradient(circle at 22% 12%, rgba(244, 225, 198, 0.62) 0%, rgba(244, 225, 198, 0.22) 25%, transparent 44%),
    radial-gradient(circle at 78% 18%, rgba(215, 225, 236, 0.54) 0%, rgba(215, 225, 236, 0.18) 28%, transparent 48%),
    radial-gradient(circle at 50% 92%, rgba(237, 228, 213, 0.72) 0%, rgba(237, 228, 213, 0.20) 34%, transparent 62%),
    linear-gradient(180deg, #fffdf8 0%, #fffaf1 46%, #fbfaf6 100%);
}

.login-page::before {
  content: "";
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(rgba(88, 83, 76, 0.13) 0.65px, transparent 0.65px),
    radial-gradient(rgba(88, 83, 76, 0.20) 1.25px, transparent 1.25px);
  background-position: 0 0, 11px 11px;
  background-size: 4px 4px, 22px 22px;
  opacity: 0.72;
  pointer-events: none;
}

.login-page::after {
  content: "";
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(255, 253, 248, 0.92) 0%, rgba(255, 253, 248, 0.56) 34%, rgba(255, 253, 248, 0.92) 100%),
    linear-gradient(180deg, rgba(255, 253, 248, 0.12) 0%, rgba(255, 253, 248, 0.86) 100%);
  mix-blend-mode: normal;
  pointer-events: none;
}

.login-shell {
  position: relative;
  z-index: 1;
  width: min(100%, 568px);
}

.login-heading {
  margin-bottom: 42px;
  text-align: center;
}

.login-heading h1 {
  margin: 0;
  color: #050505;
  font-size: 44px;
  line-height: 1.12;
  font-weight: 800;
  letter-spacing: 0;
}

.login-heading p {
  margin: 14px 0 0;
  color: #5f5f5f;
  font-size: 16px;
  line-height: 1.55;
}

.login-card {
  width: 100%;
  padding: 35px 36px 38px;
  border: 1px solid #ebe7df;
  border-radius: 40px;
  background: rgba(255, 255, 255, 0.64);
  box-shadow: 0 20px 70px rgba(17, 17, 17, 0.035);
  backdrop-filter: blur(2px);
}

.oauth-button {
  width: 100%;
  height: 56px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 11px;
  border: 1px solid #c9c6bf;
  border-radius: 12px;
  color: #30302f;
  background: rgba(255, 255, 255, 0.74);
  font-size: 18px;
  font-weight: 500;
  cursor: pointer;
  transition: background .16s ease, border-color .16s ease;
}

.oauth-button:hover {
  border-color: #a8a49c;
  background: #fff;
}

.oauth-button svg {
  width: 20px;
  height: 20px;
  flex: 0 0 auto;
}

.divider {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 52px;
  color: #8b8984;
  font-size: 15px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-input__wrapper) {
  min-height: 56px;
  padding: 0 16px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 0 0 1px #e9e6e0 inset;
  transition: box-shadow .16s ease, background .16s ease;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d4d0c8 inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 1.5px #111 inset;
}

.login-form :deep(.el-input__inner) {
  height: 56px;
  color: #111;
  font-size: 18px;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #909090;
}

.submit-button {
  width: 100%;
  height: 58px;
  margin-top: 0;
  border: 0;
  border-radius: 12px;
  background: #111111;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  transition: background .16s ease;
}

.submit-button:hover,
.submit-button:focus {
  background: #1f1f1f;
}

.form-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin: 16px 2px 0;
}

.remember {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #7b776f;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
}

.remember input {
  width: 15px;
  height: 15px;
  accent-color: #111;
}

.text-button {
  border: 0;
  padding: 0;
  color: #68635b;
  background: transparent;
  font: inherit;
  font-size: 14px;
  cursor: pointer;
  transition: color .16s ease;
}

.text-button:hover {
  color: #111;
}

.policy-copy {
  max-width: 470px;
  margin: 20px auto 0;
  color: #858585;
  font-size: 14px;
  line-height: 1.6;
  text-align: center;
}

.register-line {
  margin: 18px 0 0;
  color: #7b776f;
  font-size: 14px;
  text-align: center;
}

.register-line a {
  color: #111;
  text-decoration: underline;
  text-underline-offset: 3px;
  font-weight: 600;
}

@media (max-width: 640px) {
  .login-page {
    place-items: start center;
    padding: 68px 18px 30px;
  }

  .login-heading {
    margin-bottom: 28px;
  }

  .login-heading h1 {
    font-size: 31px;
  }

  .login-heading p {
    font-size: 14px;
  }

  .login-card {
    padding: 26px 18px 28px;
    border-radius: 30px;
  }

  .oauth-button,
  .submit-button {
    font-size: 16px;
  }

  .login-form :deep(.el-input__inner) {
    font-size: 16px;
  }

  .form-meta {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }
}
</style>
