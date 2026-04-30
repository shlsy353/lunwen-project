<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import { Search, InfoFilled, User, Check, Close, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
// 从本地存储获取用户信息，如果不存在则赋空对象
const user = ref(JSON.parse(localStorage.getItem('user') || '{}'))

/**
 * 重新获取当前登录用户的详细信息并更新本地存储
 */
const fetchUserInfo = async () => {
    const res = await request.get('/user/info') as any
    if (res.code === 200) {
        user.value = res.data
        localStorage.setItem('user', JSON.stringify(res.data))
    }
}

// 存储各种状态的列表数据
const pendingApplications = ref<any[]>([]) // 待审批的关联申请
const myStudents = ref<any[]>([])         // 已建立关联的学生名单
const searchStudent = ref('')             // 添加学生时的搜索关键词
const allStudents = ref<any[]>([])        // 搜索出的所有学生结果
const showAddDialog = ref(false)          // 是否显示添加学生对话框

// 当前选中的标签页 (students: 已关联, pending: 待审批)
const activeTab = ref('students')

// 待审批列表的分页配置
const appPage = ref(1)
const appSize = ref(10)
const appTotal = ref(0)
const paginatedPending = computed(() => {
    const start = (appPage.value - 1) * appSize.value
    return pendingApplications.value.slice(start, start + appSize.value)
})

// 已关联学生列表的分页配置
const stuPage = ref(1)
const stuSize = ref(10)
const stuTotal = ref(0)
const paginatedStudents = computed(() => {
    const start = (stuPage.value - 1) * stuSize.value
    return myStudents.value.slice(start, start + stuSize.value)
})

/**
 * 初始化加载数据：包括已关联学生和待处理申请
 */
const load = async () => {
    loading.value = true
    try {
        // 1. 获取已关联的学生列表数据
        const assocRes = await request.get('/student-leader/my-students', { params: { leaderId: user.value.id } }) as any
        if (assocRes.code === 200) {
            myStudents.value = assocRes.data
            stuTotal.value = myStudents.value.length
        }

        // 2. 获取待审批的申请列表数据
        const pendingRes = await request.get('/student-leader/pending', { params: { leaderId: user.value.id } }) as any
        if (pendingRes.code === 200) {
            pendingApplications.value = pendingRes.data
            appTotal.value = pendingApplications.value.length
            // 补充学生基本信息（将 ID 转换为可读姓名）
            for (const app of pendingApplications.value) {
                const stuRes = await request.get(`/user/${app.studentId}`) as any
                if (stuRes.code === 200) app.studentName = stuRes.data.name || stuRes.data.username
            }
        }
    } finally { loading.value = false }
}

/**
 * 处理学生的关联申请：同意(1) 或 拒绝(2)
 */
const handleApprove = async (app: any, status: number) => {
    await request.put('/student-leader/status', { id: app.id, status })
    ElMessage.success(status === 1 ? '已确认关联' : '已拒绝')
    load() // 操作完成后刷新列表
}

/**
 * 解除已有的师生关联关系
 */
const handleRemoveStudent = (stu: any) => {
    ElMessageBox.confirm(`确定取消与学生 ${stu.name || stu.username} 的领队关联吗？`, '操作确认', {
        type: 'warning', confirmButtonText: '确定移除', cancelButtonText: '取消'
    }).then(async () => {
        await request.delete(`/student-leader/remove?leaderId=${user.value.id}&studentId=${stu.id}`)
        ElMessage.success('已移除关联')
        load()
    }).catch(() => { })
}

/**
 * 弹窗内搜索逻辑：获取全校学生分页列表并过滤掉已关联的
 */
const loadAllStudents = async () => {
    const res = await request.get('/user/page', { params: { pageNum: 1, pageSize: 200, role: 'STUDENT', name: searchStudent.value } }) as any
    if (res.code === 200) {
        // 过滤：只显示目前还没被该老师领队的学生
        const associatedIds = myStudents.value.map(s => s.id)
        allStudents.value = res.data.records.filter((s: any) => !associatedIds.includes(s.id))
    }
}

/**
 * 教师主动从弹窗搜素并添加学生（直接创建已确认的关联）
 */
const handleAddStudent = async (student: any) => {
    const res = await request.post('/student-leader/add', { studentId: student.id, leaderId: user.value.id }) as any
    if (res.code === 200) {
        ElMessage.success('已直接添加学生')
        showAddDialog.value = false
        load()
    }
}

// 计算属性：判断当前教师是否拥有正式领队资格
const isLeader = computed(() => user.value.role === 'LEADER' || user.value.leaderStatus === 2)

// 生命周期初始化：首先同步最新用户信息，再根据资格加载管理数据
onMounted(async () => {
    await fetchUserInfo()
    if (isLeader.value) {
        load()
    }
})
</script>

<template>
    <div class="lead-container">
        <!-- 无权访问提示：引导教师去申请资格 -->
        <div v-if="!isLeader" class="apply-prompt-container">
            <el-card class="apply-card">
                <div class="apply-content">
                    <el-icon class="apply-icon">
                        <User />
                    </el-icon>
                    <h3>{{ user.leaderStatus === 1 ? '领队申请审核中' : '尚未获得领队资格' }}</h3>
                    <p>{{ user.leaderStatus === 1 ? '您的领队教师资格申请已提交，正在等待管理员审核。' : '您需要申请领队教师资格后才能管理关联学生。' }}</p>
                    <div class="status-info" v-if="user.leaderStatus === 1">
                        <el-tag type="warning" size="large" effect="plain" round>
                            <el-icon>
                                <InfoFilled />
                            </el-icon> 审核中
                        </el-tag>
                    </div>
                    <el-button v-else type="primary" size="large" round
                        @click="$router.push('/teacher/profile')">前往个人中心申请</el-button>
                    <el-button v-if="user.leaderStatus === 1" plain size="large" round
                        @click="$router.push('/teacher/profile')" style="margin-top: 15px">查看申请进度</el-button>
                </div>
            </el-card>
        </div>

        <!-- 领队管理主界面 -->
        <template v-else>
            <div class="page-header">
                <h2 class="title">带领学生管理</h2>
                <p class="subtitle">管理由您指导或带领的参赛学生及项目进度</p>
            </div>

            <el-tabs v-model="activeTab" class="premium-tabs">
                <!-- 已关联学生选项卡 -->
                <el-tab-pane label="关联学生" name="students">
                    <el-card class="premium-card">
                        <template #header>
                            <div class="card-header">
                                <span class="card-title">我的关联学生 ({{ stuTotal }})</span>
                                <el-button type="primary" size="small"
                                    @click="showAddDialog = true; loadAllStudents()">添加学生</el-button>
                            </div>
                        </template>
                        <div class="stu-list">
                            <div v-for="stu in paginatedStudents" :key="stu.id" class="stu-item">
                                <el-avatar :size="32" class="stu-avatar">{{ stu.name?.[0] || 'S' }}</el-avatar>
                                <div class="stu-info">
                                    <div class="stu-name">{{ stu.name || stu.username }}</div>
                                    <div class="stu-detail">{{ stu.studentId || '无学号' }} | {{ stu.email || '无邮箱' }}
                                    </div>
                                </div>
                                <el-button type="danger" link :icon="Delete"
                                    @click="handleRemoveStudent(stu)">移除</el-button>
                            </div>
                            <el-empty v-if="myStudents.length === 0" description="暂无关联学生" :image-size="60" />
                        </div>
                        <!-- 分页器 -->
                        <div class="pagination-container" v-if="stuTotal > stuSize">
                            <el-pagination v-model:current-page="stuPage" :page-size="stuSize" :total="stuTotal"
                                layout="total, prev, pager, next" />
                        </div>
                    </el-card>
                </el-tab-pane>

                <!-- 待审批关联申请选项卡 -->
                <el-tab-pane label="申请审批" name="pending">
                    <el-card class="premium-card">
                        <template #header>
                            <span class="card-title">关联申请 ({{ pendingApplications.length }})</span>
                        </template>
                        <div class="stu-list">
                            <div v-for="app in paginatedPending" :key="app.id" class="app-item">
                                <div class="app-info">
                                    <span class="app-name">{{ app.studentName }}</span>
                                    <span class="app-time">申请时间：{{ app.createTime?.substring(0, 16).replace('T', ' ')
                                    }}</span>
                                </div>
                                <div class="app-btns">
                                    <el-button type="success" size="small" plain
                                        @click="handleApprove(app, 1)">同意关联</el-button>
                                    <el-button type="danger" size="small" plain
                                        @click="handleApprove(app, 2)">拒绝</el-button>
                                </div>
                            </div>
                            <el-empty v-if="pendingApplications.length === 0" description="没有待处理申请" :image-size="60" />
                        </div>
                        <div class="pagination-container" v-if="appTotal > appSize">
                            <el-pagination v-model:current-page="appPage" :page-size="appSize" :total="appTotal"
                                layout="total, prev, pager, next" />
                        </div>
                    </el-card>
                </el-tab-pane>
            </el-tabs>

            <!-- 搜索添加学生弹窗 -->
            <el-dialog v-model="showAddDialog" title="添加关联学生" width="450px" class="premium-dialog">
                <el-input v-model="searchStudent" placeholder="输入学生姓名、学号搜索..." clearable @input="loadAllStudents">
                    <template #prefix><el-icon>
                            <Search />
                        </el-icon></template>
                </el-input>
                <div class="search-res-list">
                    <div v-for="s in allStudents" :key="s.id" class="search-res-item">
                        <div class="res-info">
                            <div class="res-name">{{ s.name }}</div>
                            <div class="res-sub">{{ s.username }} | {{ s.studentId || '无学号' }}</div>
                        </div>
                        <el-button type="primary" size="small" round @click="handleAddStudent(s)">添加</el-button>
                    </div>
                    <el-empty v-if="allStudents.length === 0" description="未找到可添加的学生" :image-size="60" />
                </div>
            </el-dialog>

            <!-- 操作指引提示 -->
            <div class="tip-box">
                <el-icon>
                    <InfoFilled />
                </el-icon>
                <span>提示：此处集中管理您的学生关联。点击不同选项卡切换查看「学生名单」或「待审批申请」。</span>
            </div>
        </template>
    </div>
</template>

<style scoped>
/* 样式保持原样 */
.lead-container {
    padding: 24px;
    background: #f8fafc;
    min-height: calc(100vh - 84px);
}

.page-header {
    margin-bottom: 24px;
}

.title {
    font-size: 26px;
    font-weight: 800;
    color: #0f172a;
    margin: 0;
}

.subtitle {
    font-size: 14px;
    color: #64748b;
    margin-top: 4px;
}

.premium-tabs :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background-color: #e2e8f0;
}

.premium-tabs :deep(.el-tabs__item) {
    font-size: 15px;
    font-weight: 500;
    color: #64748b;
    padding: 0 24px;
}

.premium-tabs :deep(.el-tabs__item.is-active) {
    color: #3b82f6;
    font-weight: 700;
}

.premium-tabs :deep(.el-tabs__active-bar) {
    height: 3px;
    border-radius: 3px;
    background-color: #3b82f6;
}

.premium-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    background: #fff;
}

.card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.card-title {
    font-weight: 700;
    color: #1e293b;
    font-size: 16px;
}

.stu-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.stu-item {
    display: flex;
    align-items: center;
    padding: 12px;
    border-radius: 12px;
    border: 1px solid #f1f5f9;
    transition: all 0.2s;
    background: #fff;
}

.stu-item:hover {
    border-color: #3b82f6;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stu-avatar {
    background: linear-gradient(135deg, #3b82f6, #60a5fa);
    flex-shrink: 0;
}

.stu-info {
    flex: 1;
    margin-left: 12px;
    min-width: 0;
}

.stu-name {
    font-weight: 600;
    color: #1e293b;
    font-size: 14px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.stu-detail {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 2px;
}

.app-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    background: #fff;
    border-radius: 12px;
    border: 1px solid #f1f5f9;
    transition: all 0.2s;
}

.app-item:hover {
    border-color: #10b981;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.app-info {
    display: flex;
    flex-direction: column;
}

.app-name {
    font-weight: 700;
    font-size: 15px;
    color: #1e293b;
}

.app-time {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 4px;
}

.self-tag {
    color: #3b82f6;
    font-weight: 600;
    font-size: 12px;
    background: #eff6ff;
    padding: 2px 8px;
    border-radius: 4px;
}

.pagination-container {
    margin-top: 24px;
    display: flex;
    justify-content: flex-end;
}

.search-res-list {
    margin-top: 16px;
    max-height: 350px;
    overflow-y: auto;
    padding-right: 4px;
}

.search-res-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px;
    border-radius: 8px;
    border-bottom: 1px solid #f1f5f9;
}

.search-res-item:hover {
    background: #f1f5f9;
}

.res-info {
    display: flex;
    flex-direction: column;
}

.res-name {
    font-weight: 600;
    font-size: 14px;
    color: #1e293b;
}

.res-sub {
    font-size: 12px;
    color: #94a3b8;
}

.tip-box {
    margin-top: 24px;
    padding: 16px;
    background: #fffbeb;
    border: 1px solid #fef3c7;
    border-radius: 12px;
    color: #92400e;
    font-size: 13px;
    display: flex;
    align-items: center;
    gap: 10px;
}

.tip-box .el-icon {
    font-size: 18px;
    color: #d97706;
}

.apply-prompt-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 50vh;
}

.apply-card {
    width: 100%;
    max-width: 500px;
    border-radius: 20px;
    text-align: center;
    padding: 40px 20px;
    border: none;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.05);
}

.apply-icon {
    font-size: 64px;
    color: #3b82f6;
    margin-bottom: 20px;
    opacity: 0.8;
}

.apply-content h3 {
    font-size: 22px;
    color: #1e293b;
    margin-bottom: 12px;
}

.apply-content p {
    color: #64748b;
    margin-bottom: 30px;
    line-height: 1.6;
}

.status-info {
    margin-top: 20px;
}
</style>
