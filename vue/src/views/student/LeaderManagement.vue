<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Search, UserFilled, Check, Close, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const user = JSON.parse(localStorage.getItem('user') || '{}')
const loading = ref(false)
const myLeaders = ref<any[]>([])
const allTeachers = ref<any[]>([])
const searchTeacher = ref('')

const loadMyLeaders = async () => {
    const res = await request.get('/student-leader/my-leaders', { params: { studentId: user.id } }) as any
    if (res.code === 200) myLeaders.value = res.data
}

const loadTeachers = async () => {
    loading.value = true
    try {
        const res = await request.get('/user/page', {
            params: { pageNum: 1, pageSize: 100, role: 'TEACHER', username: searchTeacher.value }
        }) as any
        if (res.code === 200) {
            // Filter out existing leaders
            const existingIds = myLeaders.value.map(l => l.id)
            allTeachers.value = res.data.records.filter((t: any) => !existingIds.includes(t.id))
        }
    } finally { loading.value = false }
}

const handleApply = (teacher: any) => {
    ElMessageBox.confirm(`确定向 ${teacher.name || teacher.username} 老师申请作为您的领队吗？`, '申请确认').then(async () => {
        const res = await request.post('/student-leader/apply', {
            studentId: user.id,
            leaderId: teacher.id
        }) as any
        if (res.code === 200) {
            ElMessage.success('申请已提交，请等待老师确认')
            loadMyLeaders()
            loadTeachers()
        }
    })
}

onMounted(() => {
    loadMyLeaders()
    loadTeachers()
})
</script>

<template>
    <div class="leader-mgmt-container">
        <div class="page-header">
            <h2 class="title">我的领队管理</h2>
            <p class="subtitle">在这里您可以申请指导老师作为您的竞赛领队</p>
        </div>

        <el-row :gutter="20">
            <!-- Current Leaders -->
            <el-col :span="12">
                <el-card class="premium-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title"><el-icon><UserFilled /></el-icon> 已关联领队</span>
                        </div>
                    </template>
                    <el-empty v-if="myLeaders.length === 0" description="暂无关联领队，请在右侧搜索并申请" />
                    <div v-else class="leader-list">
                        <div v-for="leader in myLeaders" :key="leader.id" class="leader-item">
                            <el-avatar :size="40" :src="leader.avatar">{{ leader.name?.[0] }}</el-avatar>
                            <div class="leader-info">
                                <div class="leader-name">{{ leader.name || leader.username }}</div>
                                <div class="leader-dept">{{ leader.collegeName || '计算机工程学院' }}</div>
                            </div>
                            <el-tag type="success" size="small">已生效</el-tag>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- Search and Apply -->
            <el-col :span="12">
                <el-card class="premium-card" shadow="never">
                    <template #header>
                        <div class="card-header">
                            <span class="card-title">寻找并申请领队</span>
                            <div class="search-box">
                                <el-input v-model="searchTeacher" placeholder="搜索老师姓名..." @keyup.enter="loadTeachers" clearable>
                                    <template #append>
                                        <el-button :icon="Search" @click="loadTeachers" />
                                    </template>
                                </el-input>
                            </div>
                        </div>
                    </template>
                    <div v-loading="loading" class="teacher-search-list">
                        <div v-for="teacher in allTeachers" :key="teacher.id" class="teacher-item">
                            <div class="teacher-main">
                                <div class="teacher-name">{{ teacher.name || teacher.username }}</div>
                                <div class="teacher-field">{{ teacher.specialtyField || '领队老师' }}</div>
                            </div>
                            <el-button type="primary" size="small" :icon="Plus" @click="handleApply(teacher)">申请关联</el-button>
                        </div>
                        <el-empty v-if="allTeachers.length === 0" description="未找到符合条件的老师" />
                    </div>
                </el-card>
            </el-col>
        </el-row>
    </div>
</template>

<style scoped>
.leader-mgmt-container { padding: 20px; }
.page-header { margin-bottom: 24px; }
.title { font-size: 24px; font-weight: 800; color: #1e293b; margin: 0; }
.subtitle { font-size: 14px; color: #64748b; margin: 4px 0 0; }
.premium-card { border-radius: 16px; min-height: 400px; border: none; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); }
.card-header { display: flex; align-items: center; justify-content: space-between; }
.card-title { display: flex; align-items: center; gap: 8px; font-weight: 700; color: #1e293b; }
.leader-list, .teacher-search-list { display: flex; flex-direction: column; gap: 12px; }
.leader-item, .teacher-item {
    display: flex; align-items: center; padding: 12px; border-radius: 12px; border: 1px solid #f1f5f9; transition: all 0.3s;
}
.leader-item:hover, .teacher-item:hover { border-color: #3b82f6; background: #f8fafc; }
.leader-info { flex: 1; margin-left: 12px; }
.leader-name, .teacher-name { font-weight: 600; color: #1e293b; }
.leader-dept, .teacher-field { font-size: 12px; color: #94a3b8; }
.teacher-main { flex: 1; }
</style>
