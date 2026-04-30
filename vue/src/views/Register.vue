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
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-box {
    width: 500px;
    padding: 40px;
    background: white;
    border-radius: 12px;
}

h2 {
    text-align: center;
    margin-bottom: 30px;
    color: #333;
}
</style>
