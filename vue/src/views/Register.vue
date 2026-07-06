<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { User, Lock, School, Key } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)

const form = reactive({
    username: '',
    password: '',
    confirmPassword: '',
    name: '',
    studentId: '',
    school: '',
    role: 'STUDENT',
    gender: 1
})

const handleRegister = async () => {
    if (!form.username || !form.password) return ElMessage.warning('请填写必填项')
    if (form.password !== form.confirmPassword) return ElMessage.warning('两次密码不一致')

    loading.value = true
    try {
        await request.post('/user/register', {
            username: form.username,
            password: form.password,
            name: form.name,
            studentId: form.studentId,
            school: form.school,
            role: 'STUDENT',
            gender: form.gender
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
    } catch (e) {
        // Error handled in interceptor typically
    } finally {
        loading.value = false
    }
}
</script>

<template>
    <div class="register-container">
        <div class="register-box">
            <h2>学生注册</h2>
            <el-form :model="form" label-width="80px">
                <el-form-item label="用户名">
                    <el-input v-model="form.username" placeholder="登录账号" />
                </el-form-item>
                <el-form-item label="密码">
                    <el-input v-model="form.password" type="password" show-password />
                </el-form-item>
                <el-form-item label="确认密码">
                    <el-input v-model="form.confirmPassword" type="password" show-password />
                </el-form-item>
                <el-form-item label="姓名">
                    <el-input v-model="form.name" placeholder="真实姓名" />
                </el-form-item>
                <el-form-item label="学号">
                    <el-input v-model="form.studentId" placeholder="学校学号" />
                </el-form-item>
                <el-form-item label="学校">
                    <el-input v-model="form.school" placeholder="所在学校" />
                </el-form-item>
                <el-form-item label="性别">
                    <el-radio-group v-model="form.gender">
                        <el-radio :value="1">男</el-radio>
                        <el-radio :value="0">女</el-radio>
                    </el-radio-group>
                </el-form-item>
                <el-button type="primary" :loading="loading" @click="handleRegister"
                    style="width: 100%; margin-top: 20px;">注册</el-button>
                <div style="margin-top: 10px; text-align: center;">
                    <router-link to="/login">已有账号？去登录</router-link>
                </div>
            </el-form>
        </div>
    </div>
</template>

<style scoped>
.register-container {
    position: relative;
    display: flex;
    justify-content: center;
    align-items: flex-start;
    min-height: 100vh;
    overflow: hidden;
    padding: 96px 20px 40px;
    background:
        radial-gradient(circle at 22% 12%, rgba(244, 225, 198, 0.62) 0%, rgba(244, 225, 198, 0.22) 25%, transparent 44%),
        radial-gradient(circle at 78% 18%, rgba(215, 225, 236, 0.54) 0%, rgba(215, 225, 236, 0.18) 28%, transparent 48%),
        radial-gradient(circle at 50% 92%, rgba(237, 228, 213, 0.72) 0%, rgba(237, 228, 213, 0.20) 34%, transparent 62%),
        linear-gradient(180deg, #fffdf8 0%, #fffaf1 46%, #fbfaf6 100%);
}

.register-container::before {
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

.register-box {
    position: relative;
    z-index: 1;
    width: min(100%, 520px);
    padding: 38px 40px 40px;
    border: 1px solid #ebe7df;
    border-radius: 36px;
    background: rgba(255, 255, 255, 0.68);
    box-shadow: 0 20px 70px rgba(17, 17, 17, 0.035);
    backdrop-filter: blur(2px);
}

h2 {
    text-align: center;
    margin-bottom: 30px;
    color: #111;
    font-size: 30px;
    font-weight: 800;
    letter-spacing: 0;
}

.register-box :deep(.el-form-item__label) {
    color: #69645d;
    font-weight: 700;
}

.register-box :deep(.el-input__wrapper) {
    min-height: 44px;
}

.register-box :deep(.el-button) {
    height: 48px;
}

.register-box a {
    color: #111;
    font-weight: 700;
    text-decoration: underline;
    text-underline-offset: 3px;
}

@media (max-width: 640px) {
    .register-container {
        padding: 48px 16px 28px;
    }

    .register-box {
        padding: 28px 18px 30px;
        border-radius: 30px;
    }
}
</style>
