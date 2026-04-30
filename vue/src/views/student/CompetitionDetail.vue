<template>
    <div class="page-container">
        <div class="page-header">
            <el-button link @click="$router.back()" :icon="ArrowLeft" class="back-link">返回列表</el-button>
        </div>

        <div v-if="loading" v-loading="loading" style="height: 50vh"></div>
        <div v-else-if="competition" class="det-layout">
            <el-row :gutter="24">
                <!-- Main Info Column -->
                <el-col :lg="16">
                    <el-card class="premium-card det-main" shadow="never">
                        <div class="det-title-box">
                            <h1 class="det-title">{{ competition.name }}</h1>
                            <el-tag :type="statusType(competition.status)" effect="dark" class="det-status">
                                {{ statusText(competition.status) }}
                            </el-tag>
                        </div>

                        <div class="det-stats">
                            <div class="det-stat-item">
                                <el-icon>
                                    <Timer />
                                </el-icon>
                                <span class="label">日期：</span>
                                <span class="value">{{ competition.competitionDate }}</span>
                            </div>
                            <div class="det-stat-item">
                                <el-icon>
                                    <Location />
                                </el-icon>
                                <span class="label">地点：</span>
                                <span class="value">{{ competition.location }}</span>
                            </div>
                            <div class="det-stat-item">
                                <el-icon>
                                    <Trophy />
                                </el-icon>
                                <span class="label">级别：</span>
                                <span class="value">{{ competition.level }}</span>
                            </div>
                            <div class="det-stat-item">
                                <el-icon>
                                    <UserIcon />
                                </el-icon>
                                <span class="label">已预约：</span>
                                <span class="value">{{ registrationCount }} 人</span>
                            </div>
                        </div>

                        <div class="det-content">
                            <el-tabs v-model="activeTab" class="premium-tabs">
                                <el-tab-pane label="竞赛章程" name="content">
                                    <div class="det-section">
                                        <p class="section-text">{{ competition.content }}</p>
                                    </div>
                                    <div class="det-section" v-if="competition.purpose">
                                        <h3 class="section-title">设立目的</h3>
                                        <p class="section-text">{{ competition.purpose }}</p>
                                    </div>
                                </el-tab-pane>

                                <el-tab-pane label="参赛名单" name="participants">
                                    <div class="participant-list">
                                        <el-table :data="participants" stripe style="width: 100%" class="mini-table">
                                            <el-table-column type="index" label="席位" width="60" />
                                            <el-table-column prop="studentId" label="选手">
                                                <template #default="{ row }">{{ studentMap[row.studentId] || '加载中...'
                                                }}</template>
                                            </el-table-column>
                                            <el-table-column prop="createTime" label="报名时间" width="120">
                                                <template #default="{ row }">{{ row.createTime?.substring(0, 10)
                                                }}</template>
                                            </el-table-column>
                                        </el-table>
                                        <div v-if="participants.length === 0" class="empty-mini">暂无正式名单</div>
                                    </div>
                                </el-tab-pane>

                                <el-tab-pane label="获奖荣誉" name="awards">
                                    <div class="award-showcase">
                                        <div v-if="awards.length > 0" class="award-grid">
                                            <div v-for="a in awards" :key="a.id" class="award-mini-card">
                                                <el-icon class="medal-icon">
                                                    <Medal />
                                                </el-icon>
                                                <div class="aw-info">
                                                    <div class="aw-level">{{ a.awardLevel }}</div>
                                                    <div class="aw-student">{{ studentMap[a.studentId] || '加载中...' }}
                                                    </div>
                                                </div>
                                            </div>
                                        </div>

                                        <!-- Gallery of works moved here -->
                                        <div class="det-section" v-if="submittedWorks.length > 0">
                                            <h3 class="section-title">成果展示 (共{{ submittedWorks.length }}件)</h3>
                                            <div class="gallery-grid">
                                                <div v-for="w in submittedWorks" :key="w.id" class="gallery-item"
                                                    @click="downloadFile(w.workFile)">
                                                    <div class="gallery-icon">
                                                        <Document />
                                                    </div>
                                                    <div class="gallery-info">
                                                        <div class="gallery-name">参赛者成果 #{{ w.id }}</div>
                                                        <div class="gallery-date">{{ w.createTime?.substring(0, 10) }}
                                                        </div>
                                                    </div>
                                                    <el-icon class="gallery-arrow">
                                                        <Right />
                                                    </el-icon>
                                                </div>
                                            </div>
                                        </div>
                                        <div v-if="awards.length === 0 && submittedWorks.length === 0"
                                            class="empty-mini">荣誉榜单同步中...</div>
                                    </div>
                                </el-tab-pane>
                            </el-tabs>
                        </div>
                    </el-card>
                </el-col>

                <!-- Sidebar Info Column -->
                <el-col :lg="8">
                    <el-card class="premium-card det-side" shadow="never">
                        <template #header>
                            <span class="side-title">基本信息</span>
                        </template>
                        <div class="side-info-list">
                            <div class="side-info-item">
                                <span class="label">主办方</span>
                                <span class="value">{{ competition.organizer }}</span>
                            </div>
                            <div class="side-info-item">
                                <span class="label">承办方</span>
                                <span class="value">{{ competition.undertaker }}</span>
                            </div>
                            <div class="side-info-item">
                                <span class="label">竞赛类型</span>
                                <span class="value">{{ compTypeName || '未分类' }}</span>
                            </div>
                            <div class="side-info-item">
                                <span class="label">组队要求</span>
                                <span class="value">
                                    {{ (competition.minPlayers || 1) > 1 ?
                                        `${competition.minPlayers}-${competition.maxPlayers}人/队` : '独立参赛' }}
                                </span>
                            </div>
                            <div class="side-info-item">
                                <span class="label">报名截止</span>
                                <span class="value deadline">{{ competition.competitionDate }}</span>
                            </div>
                        </div>

                        <div class="side-actions">
                            <el-button type="primary" size="large" class="reg-btn" @click="handleRegisterClick"
                                :disabled="competition.status !== 1">
                                {{ competition.status === 1 ? '立即报名参赛' : (competition.status === 0 ? '竞赛已结束' : '敬请期待')
                                }}
                            </el-button>
                            <el-button v-if="competition.file" size="large" class="download-btn"
                                @click="downloadFile(competition.file)" :icon="Download">
                                下载附件指南
                            </el-button>
                        </div>
                    </el-card>

                    <div class="side-notice">
                        <el-icon>
                            <InfoFilled />
                        </el-icon>
                        <span>请仔细阅读竞赛章程，报名后进度可在“我的报名”中查看。</span>
                    </div>
                </el-col>
            </el-row>
        </div>
        <div v-else class="empty-state-full">
            <el-empty description="未找到该竞赛信息或已下架">
                <el-button type="primary" @click="$router.push('/student/competitions')">返回竞赛大厅</el-button>
            </el-empty>
        </div>

        <!-- Registration Dialog -->
        <el-dialog v-model="regDialogVisible" :title="'竞赛报名 - ' + (competition?.name || '')" width="500px" custom-class="premium-dialog">
            <el-form :model="regForm" label-position="top">
                <div v-if="user.role === 'STUDENT'">
                    <el-form-item label="领队/指导教师" required>
                        <el-select v-model="regForm.leaderId" placeholder="请选择您的指导教师" style="width: 100%">
                            <el-option v-for="t in teachers" :key="t.id" :label="t.name || t.username" :value="t.id">
                                <span style="float: left">{{ t.name || t.username }}</span>
                                <span style="float: right; color: var(--el-text-color-secondary); font-size: 11px">
                                    {{ t.specialtyField || '领队老师' }}
                                </span>
                            </el-option>
                        </el-select>
                        <div class="form-tip">学生报名前需在“我的领队”中关联并经老师确认</div>
                    </el-form-item>
                </div>
                <div v-else-if="user.role === 'TEACHER'">
                    <el-form-item label="带领学生信息" required>
                        <el-input v-model="regForm.ledStudents" type="textarea" :rows="3" placeholder="请输入您带领的学生姓名、学号等信息" />
                        <div class="form-tip">作为领队报名，请在此登记学生名单</div>
                    </el-form-item>
                </div>
            </el-form>
            <template #footer>
                <div class="dialog-footer">
                    <el-button @click="regDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="confirmRegistration" :loading="submitLoading">提交报名申请</el-button>
                </div>
            </template>
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Timer, Location, User as UserIcon, Trophy, ArrowLeft, Document, Right, InfoFilled, Medal } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const loading = ref(false)
const competition = ref<any>(null)
const registrationCount = ref(0)
const types = ref<any[]>([])
const submittedWorks = ref<any[]>([])
const activeTab = ref('content')
const participants = ref<any[]>([])
const awards = ref<any[]>([])
const teachers = ref<any[]>([])
const regDialogVisible = ref(false)
const submitLoading = ref(false)
const regForm = reactive({
    leaderId: undefined as number | undefined,
    ledStudents: ''
})

const user = computed(() => {
    const u = localStorage.getItem('user')
    return u ? JSON.parse(u) : null
})

const compTypeName = computed(() => {
    return types.value.find(t => t.id === competition.value?.typeId)?.name
})

const studentMap = ref<Record<number, string>>({})

const statusText = (s: number) => ['已结束', '进行中', '未开始'][s] ?? '未知'
const statusType = (s: number) => (['info', 'success', 'warning'] as const)[s] ?? 'info'

const fetchDetail = async () => {
    loading.value = true
    try {
        const res = await request.get(`/competition/${id}`) as any
        competition.value = res.data

        const regRes = await request.get('/registration/page', {
            params: { competitionId: id, pageSize: 1 }
        }) as any
        registrationCount.value = regRes.data.total

        fetchParticipants()
        fetchAwards()
        if (competition.value?.status !== 2) { fetchWorks() }
    } catch (e) {
        console.error('Fetch detail error:', e)
    } finally { loading.value = false }
}

const resolveStudentNames = async (list: any[]) => {
    const ids = [...new Set(list.map(i => i.studentId))]
    for (const sid of ids) {
        if (!studentMap.value[sid]) {
            const res = await request.get(`/user/${sid}`) as any
            if (res.code === 200) studentMap.value[sid] = res.data.name || res.data.username
        }
    }
}

const fetchParticipants = async () => {
    const res = await request.get('/registration/page', {
        params: { competitionId: route.params.id, status: 1, pageSize: 50 }
    }) as any
    if (res.code === 200) {
        participants.value = res.data.records
        resolveStudentNames(participants.value)
    }
}

const fetchAwards = async () => {
    const res = await request.get('/award/page', {
        params: { competitionId: route.params.id, pageSize: 50 }
    }) as any
    if (res.code === 200) {
        awards.value = res.data.records
        resolveStudentNames(awards.value)
    }
}

const fetchWorks = async () => {
    const res = await request.get('/work-submission/page', {
        params: { competitionId: id, pageSize: 6 }
    }) as any
    if (res.code === 200) submittedWorks.value = res.data.records
}

const fetchTypes = async () => {
    const res = await request.get('/competition-type/list') as any
    if (res.code === 200) types.value = res.data
}

const handleRegisterClick = async () => {
    if (!user.value) return router.push('/login')
    if (user.value.role !== 'STUDENT' && user.value.role !== 'TEACHER') return ElMessage.warning('仅学生或教师可报名')
    if (competition.value?.status !== 1) return ElMessage.warning('当前非报名阶段')

    if (user.value.role === 'STUDENT') {
        const res = await request.get('/student-leader/my-leaders', { params: { studentId: user.value.id } }) as any
        if (res.code === 200) {
            teachers.value = res.data
            if (teachers.value.length === 0) {
                return ElMessageBox.confirm('您尚未关联任何领队老师，请先申请。')
            }
        }
    }
    
    regForm.leaderId = undefined
    regForm.ledStudents = ''
    regDialogVisible.value = true
}

const confirmRegistration = async () => {
    if (user.value.role === 'STUDENT' && !regForm.leaderId) return ElMessage.warning('请选择领队教师')
    if (user.value.role === 'TEACHER' && !regForm.ledStudents) return ElMessage.warning('请输入学生信息')

    submitLoading.value = true
    try {
        await request.post('/registration', {
            competitionId: competition.value.id,
            studentId: user.value.id,
            leaderId: regForm.leaderId,
            ledStudents: regForm.ledStudents
        })
        ElMessage.success('报名申请已提交，请等待审核')
        regDialogVisible.value = false
        fetchDetail()
    } catch (e) {
        console.error(e)
    } finally {
        submitLoading.value = false
    }
}

const downloadFile = (url: string) => window.open(url, '_blank')

onMounted(() => { fetchTypes(); fetchDetail() })

watch(() => route.params.id, (newId) => {
    if (newId) { fetchDetail() }
})
</script>

<style scoped>
.page-container {
    padding: 0;
}

.back-link {
    font-size: 14px;
    color: #64748b;
    margin-bottom: 20px;
}

.back-link:hover {
    color: #3b82f6;
}

.premium-card {
    border-radius: 20px;
    border: none;
    overflow: hidden;
}

.det-main {
    padding: 32px;
    min-height: 80vh;
}

.det-title-box {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    gap: 20px;
}

.det-title {
    font-size: 32px;
    font-weight: 800;
    color: #1e293b;
    margin: 0;
    line-height: 1.2;
}

.det-status {
    font-size: 14px;
    padding: 6px 16px;
    border-radius: 8px;
}

.det-stats {
    display: flex;
    gap: 32px;
    margin-bottom: 32px;
    flex-wrap: wrap;
}

.det-stat-item {
    display: flex;
    align-items: center;
    gap: 8px;
    color: #64748b;
    font-size: 14px;
}

.det-stat-item .el-icon {
    font-size: 18px;
    color: #3b82f6;
}

.det-stat-item .value {
    font-weight: 700;
    color: #334155;
}

.det-section {
    margin-top: 40px;
}

.section-title {
    font-size: 18px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 16px;
    border-left: 4px solid #3b82f6;
    padding-left: 12px;
}

.section-text {
    font-size: 15px;
    color: #475569;
    line-height: 1.8;
    white-space: pre-wrap;
    margin: 0;
}

.gallery-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
    margin-top: 20px;
}

.gallery-item {
    background: #f8fafc;
    border: 1px solid #f1f5f9;
    border-radius: 12px;
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: all 0.2s;
}

.gallery-item:hover {
    background: #eff6ff;
    border-color: #3b82f6;
}

.gallery-icon {
    font-size: 24px;
    color: #3b82f6;
    background: #fff;
    padding: 8px;
    border-radius: 10px;
}

.gallery-name {
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;
}

.gallery-date {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 2px;
}

.gallery-arrow {
    margin-left: auto;
    color: #94a3b8;
}

.det-side {
    padding: 10px;
    position: sticky;
    top: 20px;
}

.side-title {
    font-weight: 800;
    font-size: 16px;
    color: #1e293b;
}

.side-info-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
    padding: 10px 0;
}

.side-info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.side-info-item .label {
    color: #94a3b8;
    font-size: 13px;
    font-weight: 600;
}

.side-info-item .value {
    color: #1e293b;
    font-size: 14px;
    font-weight: 700;
    text-align: right;
}

.side-info-item .deadline {
    color: #ef4444;
}

.side-actions {
    margin-top: 32px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.reg-btn {
    height: 50px;
    font-weight: 800;
    font-size: 16px;
    letter-spacing: 1px;
    border-radius: 12px;
}

.download-btn {
    height: 50px;
    font-weight: 600;
    border-radius: 12px;
}

.side-notice {
    margin-top: 20px;
    padding: 16px;
    background: rgba(59, 130, 246, 0.05);
    border-radius: 12px;
    display: flex;
    gap: 8px;
    color: #3b82f6;
    font-size: 12px;
    line-height: 1.5;
}

@media (max-width: 991px) {
    .det-side {
        margin-top: 24px;
        position: static;
    }
}

/* Premium Tabs and Lists */
.premium-tabs :deep(.el-tabs__nav-wrap::after) {
    display: none;
}

.premium-tabs :deep(.el-tabs__active-bar) {
    height: 4px;
    border-radius: 2px;
}

.premium-tabs :deep(.el-tabs__item) {
    font-size: 16px;
    font-weight: 700;
    color: #94a3b8;
    height: 50px;
}

.premium-tabs :deep(.el-tabs__item.is-active) {
    color: #3b82f6;
}

.participant-list {
    padding: 10px 0;
}

.mini-table {
    --el-table-header-bg-color: #f8fafc;
    --el-table-header-text-color: #64748b;
    border-radius: 12px;
    overflow: hidden;
}

.award-showcase {
    padding: 10px 0;
}

.award-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
    margin-bottom: 32px;
}

.award-mini-card {
    background: linear-gradient(135deg, #fff 0%, #f8fafc 100%);
    border: 1px solid #f1f5f9;
    border-radius: 16px;
    padding: 16px;
    display: flex;
    align-items: center;
    gap: 12px;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.medal-icon {
    font-size: 32px;
    color: #fbbf24;
}

.aw-level {
    font-size: 15px;
    font-weight: 800;
    color: #1e293b;
}

.aw-student {
    font-size: 12px;
    color: #64748b;
    margin-top: 2px;
}

.empty-mini {
    text-align: center;
    padding: 40px;
    color: #94a3b8;
    font-size: 14px;
    background: #f8fafc;
    border-radius: 12px;
    border: 1px dashed #e2e8f0;
}

.form-tip {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 8px;
    line-height: 1.4;
}

:deep(.premium-dialog) {
    border-radius: 20px;
}
</style>
