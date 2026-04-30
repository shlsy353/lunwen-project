<template>
    <div class="page-container">
        <div class="page-header">
            <div class="page-title">
                <el-icon class="title-icon">
                    <Edit />
                </el-icon>
                <span>竞赛成绩与评分</span>
            </div>
        </div>

        <el-card class="premium-card" shadow="never">
            <div v-if="loading" v-loading="loading" style="height: 300px"></div>
            <div v-else-if="tableData.length === 0" class="empty-state">
                <el-empty description="暂无评分记录，请先提交作品或等待老师阅卷" />
            </div>
            <div v-else class="score-list">
                <div v-for="item in tableData" :key="item.id" class="score-card">
                    <div class="score-header">
                        <span class="score-comp-name">{{ competitionMap[item.competitionId] || ('加载中... #' +
                            item.competitionId) }}</span>
                        <el-tag :type="getStatusType(item)" effect="plain">
                            {{ getStatusText(item) }}
                        </el-tag>
                    </div>
                    <el-divider border-style="dashed" />
                    <div class="score-body">
                        <div class="score-value-box">
                            <div class="score-label">最终得分</div>
                            <div class="score-num" :class="{ 'no-score': !item.score }">
                                {{ item.score || '--' }}
                            </div>
                        </div>
                        <div class="score-feedback">
                            <div class="feedback-label"><el-icon>
                                    <ChatLineRound />
                                </el-icon> 教师评价及反馈：</div>
                            <p class="feedback-text">{{ item.comment || '暂无详细评价，请耐心等待或联系老师咨询。' }}</p>
                        </div>
                    </div>
                    <div class="score-footer">
                        <el-button link type="primary" @click="$router.push('/student/appeals')">成绩申诉</el-button>
                        <el-button link type="info" @click="viewWork(item)">查看我的作品</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-bar" v-if="total > pageSize">
                <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
                    layout="total, prev, pager, next" @change="load" />
            </div>
        </el-card>

        <el-dialog v-model="dialogVisible" title="作品详情" width="500px">
            <div v-if="currentWork && currentWork.workFile" class="work-viewer">
                <el-link :href="currentWork.workFile" target="_blank" type="primary"
                    :icon="Download">点击下载/查看已提交的作品文件</el-link>
                <p class="work-date">提交于：{{ currentWork.createTime }}</p>
            </div>
            <el-empty v-else description="您尚未提交作品文件" />
        </el-dialog>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import request from '@/utils/request'
import { Edit, ChatLineRound, Download } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const currentWork = ref<any>({})

const competitionMap = ref<Record<number, string>>({})
const competitionStatusMap = ref<Record<number, number>>({})

const load = async () => {
    loading.value = true
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    try {
        // Fetch registrations to see all signed up competitions
        const res = await request.get('/registration/page', {
            params: { pageNum: pageNum.value, pageSize: pageSize.value, studentId: user.id, status: 1 } // Only approved ones
        }) as any

        if (res.code === 200) {
            const registrations = res.data.records
            total.value = res.data.total

            // Map registrations to their scores and submissions
            const recordsWithDetails = await Promise.all(registrations.map(async (reg: any) => {
                // Get submission
                const sRes = await request.get('/work-submission/page', {
                    params: { studentId: user.id, competitionId: reg.competitionId, pageSize: 1 }
                }) as any
                const submission = sRes.data?.records?.[0] || null

                // Get score
                const scRes = await request.get('/score/page', {
                    params: { studentId: user.id, competitionId: reg.competitionId, pageSize: 1 }
                }) as any
                const scoreObj = scRes.data?.records?.[0] || null

                // Resolve competition info
                if (!competitionMap.value[reg.competitionId]) {
                    const cRes = await request.get(`/competition/${reg.competitionId}`) as any
                    if (cRes.code === 200) {
                        competitionMap.value[reg.competitionId] = cRes.data.name
                        competitionStatusMap.value[reg.competitionId] = cRes.data.status
                    }
                }

                return {
                    ...reg,
                    submission,
                    score: scoreObj?.score,
                    comment: scoreObj?.comment,
                    workFile: submission?.workFile,
                    workCreateTime: submission?.createTime
                }
            }))

            tableData.value = recordsWithDetails as any
        }
    } finally { loading.value = false }
}

const getStatusText = (item: any) => {
    if (item.score) return '已评分'
    const compStatus = competitionStatusMap.value[item.competitionId]
    if (compStatus === 0) return '阅卷中' // Closed
    if (item.submission) return '已提交'
    return '未提交'
}

const getStatusType = (item: any) => {
    if (item.score) return 'success'
    if (item.submission) return 'primary'
    return 'warning'
}

const viewWork = (item: any) => { currentWork.value = item; dialogVisible.value = true }

onMounted(load)
</script>

<style scoped>
.page-container {
    padding: 0;
}

.page-header {
    margin-bottom: 24px;
}

.page-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    font-weight: 700;
    color: #1e293b;
}

.title-icon {
    font-size: 24px;
    color: #3b82f6;
}

.premium-card {
    border-radius: 16px;
    border: none;
    min-height: 50vh;
}

.score-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    gap: 24px;
}

.score-card {
    border-radius: 16px;
    border: 1px solid #f1f5f9;
    padding: 24px;
    background: #fff;
    transition: all 0.3s;
}

.score-card:hover {
    transform: translateY(-5px);
    border-color: #3b82f6;
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
}

.score-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.score-comp-name {
    font-weight: 700;
    color: #1e293b;
    font-size: 16px;
}

.score-body {
    display: flex;
    gap: 24px;
    margin: 20px 0;
    align-items: flex-start;
}

.score-value-box {
    background: #f8fafc;
    padding: 16px;
    border-radius: 12px;
    text-align: center;
    min-width: 100px;
    border: 1px solid #e2e8f0;
}

.score-label {
    font-size: 12px;
    color: #64748b;
    margin-bottom: 8px;
    font-weight: 600;
}

.score-num {
    font-size: 32px;
    font-weight: 800;
    color: #16a34a;
}

.score-num.no-score {
    color: #94a3b8;
}

.score-feedback {
    flex: 1;
}

.feedback-label {
    font-size: 13px;
    font-weight: 700;
    color: #334155;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 4px;
}

.feedback-text {
    font-size: 13px;
    color: #64748b;
    line-height: 1.6;
    margin: 0;
    font-style: italic;
}

.score-footer {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    border-top: 1px dashed #f1f5f9;
    padding-top: 16px;
}

.work-viewer {
    text-align: center;
    padding: 20px 0;
}

.work-date {
    font-size: 12px;
    color: #94a3b8;
    margin-top: 10px;
}

.pagination-bar {
    margin-top: 32px;
    display: flex;
    justify-content: center;
}

@media (max-width: 480px) {
    .score-list {
        grid-template-columns: 1fr;
    }

    .score-body {
        flex-direction: column;
        align-items: center;
    }

    .score-value-box {
        width: 100%;
    }
}
</style>
